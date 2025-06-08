package servidor;

import common.Mensaje;
import common.Respuesta;
import common.Solicitud;
import excepciones.ServidorPrincipalCaidoException;
import excepciones.UsuarioExistenteException;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class PasivoState extends ServidorState {
    private int intervaloPing = 5000;
    private volatile boolean monitoreo = true;

    public PasivoState(Servidor servidor) {
        super(servidor);
    }

    @Override
    public void start() throws ServidorPrincipalCaidoException {
        try {
            ServerSocket socket = new ServerSocket(servidor.getPuertoSecundario());
            System.out.println("Servidor SECUNDARIO iniciado en " + InetAddress.getLocalHost().getHostAddress() + ":" + socket.getLocalPort());

            // RESINCRONIZACION
            resincronizacion();

            escuchar(socket);
            monitorear();

            while(true) {
                if (!this.monitoreo) {
                    socket.close();
                    servidor.cambiarState(new ActivoState(servidor));
                    throw new ServidorPrincipalCaidoException();
                }
            }

        } catch (IOException e) {
            System.out.println("Problemitas");
        }
    }

    @Override
    public void resincronizacion() {
        new Thread(() -> {
            try (Socket socketPrincipal = new Socket(InetAddress.getLocalHost().getHostAddress(), getPuertoMonitor())) {
                Solicitud aviso = new Solicitud(Solicitud.RESINCRONIZACION);
                ObjectOutputStream outputStream = new ObjectOutputStream(socketPrincipal.getOutputStream());
                outputStream.writeObject(aviso);
                outputStream.flush();
            } catch (IOException e) {
                System.out.println("Error al solicitar resincronizacion al servidor principal");
            }
        }).start();
    }

    @Override
    public void escuchar(ServerSocket serverSocketEscucha) {

        new Thread(() -> {
            try {
                while (this.monitoreo) {
                    //System.out.println("Esperando conexiones... monitoreo=" + this.monitoreo);

                    try {
                        Socket socket = serverSocketEscucha.accept();
                        new Thread(() -> {
                            try {
                                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                                Object obj = inputStream.readObject();

                                if (obj instanceof Respuesta respuesta) {
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
                                            getUsuarioDirectorio(usuario).setConectado(false);
                                        }

                                        case Respuesta.MENSAJES_OFFLINE -> {
                                            String usuario = (String) respuesta.getDatos().get("usuario");
                                            servidor.sacarDeCola(usuario);
                                        }

                                        case Respuesta.RESINCRONIZACION -> {
                                            setDirectorio((HashMap<String, UsuarioServidor>) respuesta.getDatos().get("usuarios"));
                                            setColaMensajes((Map<String, Queue<Mensaje>>) respuesta.getDatos().get("mensajesOffline"));
                                            System.out.println("-------------------- ESTADO DEL SISTEMA --------------------");
                                            System.out.println("Directorio: " + getDirectorio());
                                            System.out.println("Mensajes Offline: " + getColaMensajes());
                                            System.out.println("-------------------------------------------------------------");
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
                try {
                    serverSocketEscucha.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    @Override
    public void monitorear() {
        new Thread(() -> {
            while (this.monitoreo) {
                try {
                    setRecibioEcho(false);
                    // Enviar Ping
                    new Thread(() -> {
                        try (Socket socketPrincipal = new Socket(InetAddress.getLocalHost().getHostAddress(), getPuertoMonitor())){
                            HashMap<String, Object> datos = new HashMap<>();
                            Solicitud ping = new Solicitud(Solicitud.PING, datos);
                            ObjectOutputStream outputStream = new ObjectOutputStream(socketPrincipal.getOutputStream());
                            outputStream.writeObject(ping);
                            outputStream.flush();
                        } catch (IOException e) {
                            System.out.println("Error al enviar el ping");
                        }
                    }).start();
                    Thread.sleep(intervaloPing);

                    if (!servidor.isRecibioEcho()) {
                        this.monitoreo = false;
                    }

                } catch (Exception e) {

                }
            }
        }).start();
    }

    // NO SON DE ESTE
    @Override
    public boolean logearCliente(String usuario, String ip, int puerto) throws IOException, UsuarioExistenteException {
        System.out.println("El servidor secundario no deberia estar llamando a logearCliente");
        return false;
    }

    @Override
    public boolean validarDireccion(String ip, int puerto, String name) {
        System.out.println("El servidor secundario no deberia estar llamando a validarDireccion");
        return false;
    }

    @Override
    public ArrayList<String> getDatosDirectorio() {
        System.out.println("El servidor secundario no deberia estar llamando a getDatosDirectorio");
        return null;
    }

    @Override
    public UsuarioServidor getUsuario(String usuario) {
        System.out.println("El servidor secundario no deberia estar llamando a getUsuario");
        return null;
    }
}
