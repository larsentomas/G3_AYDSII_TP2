package servidor;

import excepciones.UsuarioExistenteException;


import common.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Servidor {

    private ServerSocket serverSocket;
    private HashMap<String, UsuarioServidor> directorio = new HashMap<>();
    private final Map<String, Queue<Mensaje>> colaMensajes = new ConcurrentHashMap<>();
    private HandlerSolicitudes handler;
    private int intervaloPing = 5000;
    private boolean recibioEcho;

    private final int puertoPrincipal = 6000;
    private final int puertoSecundario = 6001;

    public Servidor() {
        if (noExistePrincipal()) {
            System.out.println("Hola1");
            start();
        } else {
            System.out.println("Hola2");
            iniciarMonitoreo();
        }
    }

    public boolean noExistePrincipal() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            InetSocketAddress direccion = new InetSocketAddress(InetAddress.getLocalHost().getHostName(), puertoPrincipal);
            serverSocket.setReuseAddress(true);
            serverSocket.bind(direccion);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        new Servidor();
    }

    private void start() {
        try {
            serverSocket = new ServerSocket(puertoPrincipal);

            while (true) {
                System.out.println("Inicio servidor principal");
                Socket socket = serverSocket.accept();
                handler = new HandlerSolicitudes(socket, this);
                new Thread(handler).start();
                System.out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
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

    protected boolean validarDireccion(String ip, int puerto, String name) {
        try {
            String ipServdidor = InetAddress.getLocalHost().getHostAddress();
            if (puerto == puerto && ip.equalsIgnoreCase(ipServdidor)) {
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

    public int getPuerto() {
        return this.puertoPrincipal;
    }

    public int getPuertoSecundario() {
        return this.puertoSecundario;
    }

    public void agregarDirectorio(String usuario, UsuarioServidor usuarioServidor) {
        this.directorio.put(usuario, usuarioServidor);
    }

    public void agregarMensajeACola(String usuario, Mensaje mensaje) {
        if (!colaMensajes.containsKey(usuario)) {
            colaMensajes.put(usuario, new LinkedList<>());
        }
        Queue<Mensaje> mensajes = colaMensajes.get(usuario);
        mensajes.add(mensaje);
    }


    public void iniciarMonitoreo() {

        try {
            ServerSocket socket = new ServerSocket(puertoSecundario);
            escuchar(socket);
            monitorear(socket);
        } catch (Exception e) {
            System.out.println("Problemitas");
        }
    }

    public void escuchar(ServerSocket serverSocket) {
        while(true) {
            try {
                Socket socket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                        Object obj = inputStream.readObject();
                        if (obj instanceof Solicitud request && request.getTipo().equalsIgnoreCase(Solicitud.ECHO)) {
                            setRecibioEcho(true);
                        }
                    } catch(Exception e) {
                        System.out.println("Problemitas escuchar");
                    }
                }).start();
            } catch (Exception e) {
                System.out.println("Problemitas2");
            }
        }
    }

    public void monitorear(ServerSocket socketSecundario) {
        new Thread(() -> {
            System.out.println("Inicio monitoreo");
            while (true) {
                try {
                    recibioEcho = false;
                    // Enviar Ping
                    new Thread(() -> {
                        System.out.println("Envio Ping");
                        try (Socket socketPrincipal = new Socket(InetAddress.getLocalHost().getHostAddress(), puertoPrincipal)) {
                            Solicitud ping = new Solicitud(Solicitud.PING);
                            ObjectOutputStream outputStream = new ObjectOutputStream(socketPrincipal.getOutputStream());
                            outputStream.writeObject(ping);
                            outputStream.flush();
                        } catch (IOException e) {
                            System.out.println("Failed to send ping");
                        }
                    }).start();

                    Thread.sleep(intervaloPing);
                } catch (Exception e) {}
                if (!recibioEcho) {
                    System.out.println("No recibi eco");
                    break; // Empieza a actuar como primario
                }
            }
        }).start();
        iniciarBackUp();
    }

    public void setRecibioEcho(boolean b) {
        System.out.println("Recibi eco");
        this.recibioEcho = b;
    }

    public void iniciarBackUp() {
        System.out.println("Inicio backup");
    }
}