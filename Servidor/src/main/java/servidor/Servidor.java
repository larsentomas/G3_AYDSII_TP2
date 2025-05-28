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
    private volatile boolean monitoreo = false;
    private final int puertoPrincipal = 6000;
    private final int puertoSecundario = 6001;

    public Servidor() {
        if (noExisteServidor(puertoPrincipal)) {
            start();
        } else {
            this.monitoreo = true;
            iniciarMonitoreo();
        }
    }

    public boolean noExisteServidor(int puerto) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            InetSocketAddress direccion = new InetSocketAddress(InetAddress.getLocalHost().getHostName(), puerto);
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
            System.out.println("Servidor PRINCIPAL iniciado");

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new HandlerSolicitudes(socket, this)).start();
                System.out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    public Map<String, Queue<Mensaje>> getMensajesOffline() {
        return colaMensajes;
    }

    public Queue<Mensaje> getMensajesOffline(String username) {
        return colaMensajes.remove(username);
    }

    public boolean logearCliente(String usuario, String ip, int puerto) throws IOException, UsuarioExistenteException {
        if (validarDireccion(ip, puerto, usuario)) {
            if (!directorio.containsKey(usuario)) { // usuario por primera vez
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

    public HashMap<String, UsuarioServidor> getDirectorio() {
        return directorio;
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
            System.out.println("Servidor SECUNDARIO iniciado");
            ServerSocket socket = new ServerSocket(puertoSecundario);

            // RESINCRONIZACION
            resincronizacion();

            escuchar(socket);
            monitorear(socket);
        } catch (Exception e) {
            System.out.println("Problemitas");
        }
    }

    public void resincronizacion() {
        new Thread(() -> {
            try (Socket socketPrincipal = new Socket(InetAddress.getLocalHost().getHostAddress(), puertoPrincipal)) {
                    Solicitud aviso = new Solicitud(Solicitud.RESINCRONIZACION);
                    ObjectOutputStream outputStream = new ObjectOutputStream(socketPrincipal.getOutputStream());
                    outputStream.writeObject(aviso);
                    outputStream.flush();
                } catch (IOException e) {
                    System.out.println("Error al solicitar resincronizacion al servidor principal");
                }
        }).start();
    }

    public void escuchar(ServerSocket serverSocket) {
        System.out.println("Escuchando");

        new Thread(() -> {
            try {
                while (this.monitoreo) {
                    //System.out.println("Esperando conexiones... monitoreo=" + this.monitoreo);

                    try {
                        Socket socket = serverSocket.accept();
                        new Thread(() -> {
                            try {
                                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                                Object obj = inputStream.readObject();

                                if (obj instanceof Respuesta respuesta) {
                                    if (!respuesta.getTipo().equals(Respuesta.ECHO)) System.out.println("Actualizacion tipo " + respuesta.getTipo());
                                    switch (respuesta.getTipo()) {
                                        case Respuesta.ECHO -> setRecibioEcho(true);

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

                                        case Respuesta.RESINCRONIZACION -> {
                                            this.directorio = (HashMap<String, UsuarioServidor>) respuesta.getDatos().get("usuarios");
                                            this.colaMensajes.clear();
                                            this.colaMensajes.putAll((Map<String, Queue<Mensaje>>) respuesta.getDatos().get("mensajesOffline"));
                                            System.out.println("-------------------- ESTADO DEL SISTEMA --------------------");
                                            System.out.println("Directorio: " + directorio);
                                            System.out.println("Mensajes Offline: " + colaMensajes);
                                        }

                                        default -> System.out.println("Solicitud desconocida");
                                    }
                                }

                            } catch (Exception e) {
                                System.out.println("Error procesando solicitud:");
                            } finally {
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    System.out.println("Error cerrando socket:");
                                }
                            }
                        }).start();
                    } catch (SocketException se) {
                        System.out.println("Socket cerrado, terminando escucha.");
                        break;
                    } catch (Exception e) {
                        System.out.println("Error aceptando conexión:");
                    }
                }
            } finally {
                System.out.println("Fin monitoreo de conexiones.");
            }
        }).start();
    }

    public void monitorear(ServerSocket serverSocket) {
        new Thread(() -> {
            while (this.monitoreo) {
                try {
                    recibioEcho = false;
                    // Enviar Ping
                    new Thread(() -> {
                        try (Socket socketPrincipal = new Socket(InetAddress.getLocalHost().getHostAddress(), puertoPrincipal)) {
                            HashMap<String, Object> datos = new HashMap<>();
                            datos.put("origen", "backup");
                            Solicitud ping = new Solicitud(Solicitud.PING, datos);
                            ObjectOutputStream outputStream = new ObjectOutputStream(socketPrincipal.getOutputStream());
                            outputStream.writeObject(ping);
                            outputStream.flush();
                        } catch (IOException e) {
                            System.out.println("Error al enviar el ping");
                        }
                    }).start();
                    Thread.sleep(intervaloPing);
                } catch (Exception e) {

                }
                if (!recibioEcho) {
                    try {
                        iniciarBackUp(serverSocket);
                    } catch (IOException e) {
                        System.out.println("No se pudo levantar el servidor de backup");
                    }
                    break;
                }
            }
        }).start();
    }

    public void setRecibioEcho(boolean b) {
        this.recibioEcho = b;
    }

    //Marca el monitoreo en false, cierra el socket de escucha y pone al servidor como principal
    public void iniciarBackUp(ServerSocket serverSocket) throws IOException {
        this.monitoreo = false;
        serverSocket.close();
        start();
    }
}