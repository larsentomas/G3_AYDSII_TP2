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
    private int intervaloPing = 5000;
    private boolean recibioEcho;

    private final int puertoPrincipal = 6000;
    private final int puertoSecundario = 6001;

    public Servidor() {
        if (noExistePrincipal()) {
            start();
        } else {
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

    public void start() {
        try {
            //Por ahora el servidor corre en la maquina local
            serverSocket = new ServerSocket(puertoPrincipal);
            System.out.println("Servidor iniciado en el puerto " + puertoPrincipal);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new HandlerSolicitudes(socket, this)).start();
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

    public boolean logearCliente(String usuario, String ip, int puerto) throws IOException, UsuarioExistenteException {
        if (validarDireccion(ip, puerto, usuario)) {
            if (!directorio.containsKey(usuario)) {
                UsuarioServidor nuevoUsuario = new UsuarioServidor(usuario, ip, puerto);
                directorio.put(usuario, nuevoUsuario);
                return true;
            } else {
                UsuarioServidor usuarioExistente = directorio.get(usuario);
                if (!usuarioExistente.isConectado()) {
                    usuarioExistente.setConectado(true);
                    usuarioExistente.setIp(ip);
                    usuarioExistente.setPuerto(puerto);
                    return true;
                }
                return false;
            }
        }
        throw new UsuarioExistenteException(usuario);
    }

    private boolean validarDireccion(String ip, int puerto, String name) {
        try {
            String ipServidor = InetAddress.getLocalHost().getHostAddress();
            if (puerto == puertoPrincipal && ip.equalsIgnoreCase(ipServidor)) {
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

    public void eliminarCliente(String username) {
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
            monitorear();
        } catch (Exception e) {
            System.out.println("Problemitas");
        }
    }

    public void escuchar(ServerSocket serverSocket) {
        System.out.println("Esuchando");
        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> {
                        try {
                            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                            Object obj = inputStream.readObject();
                            if (obj instanceof Respuesta respuesta) {
                                switch (respuesta.getTipo()) {
                                    case Respuesta.ECHO -> {
                                        setRecibioEcho(true);
                                    }
                                    case Respuesta.LOGIN -> {
                                        String usuario = (String) respuesta.getDatos().get("usuario");
                                        String ip = (String) respuesta.getDatos().get("ip");
                                        int puerto = (int) respuesta.getDatos().get("puerto");
                                        agregarDirectorio(usuario, new UsuarioServidor(usuario, ip, puerto));
                                    }
                                    case Respuesta.LOGOUT -> {
                                        String usuario = (String) respuesta.getDatos().get("usuario");
                                        directorio.get(usuario).setConectado(false);
                                    }
                                    case Respuesta.MENSAJES_OFFLINE -> {
                                        String usuario = (String) respuesta.getDatos().get("usuario");
                                        colaMensajes.remove(usuario);
                                    }
                                    default -> System.out.println("Solicitud desconocida");
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Problemitas escuchar");
                        }
                    }).start();
                } catch (Exception e) {
                    System.out.println("Problemitas2");
                }
                System.out.println("SISTEMA SECUNDARIO");
                System.out.println("Directorio = " + directorio);
                System.out.println("Cola de mensajes = " + colaMensajes);
            }
        }).start();
    }

    public void monitorear() {
        new Thread(() -> {
            System.out.println("Inicio monitoreo");
            while (true) {
                try {
                    recibioEcho = false;
                    // Enviar Ping
                    new Thread(() -> {
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
                } catch (Exception e) {
                }
                if (!recibioEcho) {
                    System.out.println("No recibi eco");
                    break; // Empieza a actuar como primario
                }
            }
        }).start();
        iniciarBackUp();
    }


    public void setRecibioEcho(boolean b) {
        this.recibioEcho = b;
    }


    public void iniciarBackUp() {
        System.out.println("Inicio backup");
    }

}