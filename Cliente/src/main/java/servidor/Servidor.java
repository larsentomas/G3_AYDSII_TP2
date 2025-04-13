package servidor;

import excepciones.UsuarioExistenteException;
import modelo.Conversacion;
import modelo.Mensaje;
import modelo.Respuesta;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Servidor {

    private ServerSocket serverSocket;
    private HashMap<String, UsuarioServidor> directorio = new HashMap<>();
    private final Map<String, Queue<Mensaje>> colaMensajes = new ConcurrentHashMap<>();


    public static void main(String[] args) throws IOException {
        new Servidor().start(6000);
    }

    public void start(int port) {
        try {
            //Por ahora el servidor corre en la maquina local
            serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado en el puerto " + port);

            while (true) {
                System.out.println("Esperando conexiones...");
                Socket socket = serverSocket.accept();
                System.out.println("Conexión aceptada, iniciando HandlerClientes...");
                new Thread(new HandlerClientes(socket, this)).start();
                System.out.println("Usuarios hasta ahora");
                for (Map.Entry<String, UsuarioServidor> entry : directorio.entrySet()) {
                    System.out.println("Usuario: " + entry.getKey() + ", IP: " + entry.getValue().getIp() + ", Puerto: " + entry.getValue().getPuerto());
                }
                System.out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void routeMensaje(Mensaje mensaje, Conversacion c) {
        UsuarioServidor receptor = directorio.get(c.getIntegrante());
        if (receptor != null) {
            if (receptor.isConectado()) {
                Respuesta respuesta = new Respuesta(Respuesta.MENSAJE_RECIBIDO, Map.of("mensaje", mensaje), false, null);
                new Thread(() -> {
                    try (Socket socket = new Socket(receptor.getIp(), receptor.getPuerto())) {
                        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                        outputStream.writeObject(respuesta);
                        outputStream.flush();
                        System.out.println("Mensaje enviado a " + c.getIntegrante());
                    } catch (IOException e) {
                        System.out.println("Error al enviar el mensaje a " + c.getIntegrante());
                    }
                }).start();
            } else {
                colaMensajes.computeIfAbsent(c.getIntegrante(), k -> new LinkedList<>()).add(mensaje);
                System.out.println("Stored message for " + c.getIntegrante());
            }
        } else {
            System.out.println("Usuario " + c.getIntegrante() + " no encontrado.");
        }
    }

    public Queue<Mensaje> getMensajesOffline(String username) {
        return colaMensajes.remove(username);
    }

    public void logearCliente(String usuario, String ip, int puerto) throws IOException, UsuarioExistenteException {
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