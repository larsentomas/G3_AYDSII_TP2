package servidor;

import excepciones.UsuarioExistenteException;


import common.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Servidor {

    private ServerSocket serverSocket;
    private HashMap<String, UsuarioServidor> directorio = new HashMap<>();
    private final Map<String, Queue<Mensaje>> colaMensajes = new ConcurrentHashMap<>();

    private final static int puertoServidor = 6000;


    public static void main(String[] args) {
        new Servidor().start(puertoServidor);
    }

    public void start(int port) {
        try {
            //Por ahora el servidor corre en la maquina local
            serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado en el puerto " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new HandlerClientes(socket, this)).start();
                System.out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void routeMensaje(Mensaje mensaje, String recep) {
        UsuarioServidor receptor = getUsuario(recep);
        if (receptor != null) {
            if (receptor.isConectado()) {
                Respuesta respuesta = new Respuesta(Respuesta.MENSAJE_RECIBIDO, Map.of("mensaje", mensaje), false, null);
                new Thread(() -> {
                    try (Socket socket = new Socket(receptor.getIp(), receptor.getPuerto())) {
                        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                        outputStream.writeObject(respuesta);
                        outputStream.flush();
                    } catch (IOException e) {
                        System.out.println("Error al enviar el mensaje a " + receptor);
                    }
                }).start();
            } else {
                if (!colaMensajes.containsKey(receptor.getNombre())) {
                    System.out.println("No existia cola de mensajes para el usuario, le creo una");
                    colaMensajes.put(receptor.getNombre(), new LinkedList<>());
                }
                Queue<Mensaje> mensajes = colaMensajes.get(receptor.getNombre());
                mensajes.add(mensaje);
                System.out.println("Agregue " + mensaje + " a la cola de mensajes de " + receptor.getNombre());
            }
        } else {
            System.out.println("Usuario no encontrado.");
        }
    }

    public Queue<Mensaje> getMensajesOffline(String username) {
        return colaMensajes.remove(username);
    }

    public void logearCliente(String usuario, String ip, int puerto) throws IOException, UsuarioExistenteException {
        if (validarDireccion(ip, puerto, usuario))
            if (!directorio.containsKey(usuario)) {
                UsuarioServidor nuevoUsuario = new UsuarioServidor(usuario, ip, puerto);
                directorio.put(usuario, nuevoUsuario);
                return;
            } else {
                UsuarioServidor usuarioExistente = directorio.get(usuario);
                if (!usuarioExistente.isConectado()) {
                    usuarioExistente.setConectado(true);
                    usuarioExistente.setIp(ip);
                    usuarioExistente.setPuerto(puerto);
                    return;
                }
            }
        throw new UsuarioExistenteException(usuario);
    }

    private boolean validarDireccion(String ip, int puerto, String name) {
        try {
            String ipServdidor = InetAddress.getLocalHost().getHostAddress();
            if (puerto == puertoServidor && ip.equalsIgnoreCase(ipServdidor)) {
                return false;
            }
        } catch (UnknownHostException e) {
            System.err.println("Error al obtener la dirección IP del servidor: " + e.getMessage());
        }
        for (UsuarioServidor usuario : directorio.values()) {
            if (usuario.getIp().equals(ip) && usuario.getPuerto() == puerto && !usuario.getNombre().equals(name)) {
                return false;
            }
        }
        return true;
    }

    public void eliminarCliente(String username){
        directorio.remove(username);
    }

    public ArrayList<String> getDatosDirectorio() {
        return new ArrayList<>(directorio.keySet());
    }

    public UsuarioServidor getUsuario(String username) {
        return directorio.get(username);
    }
}