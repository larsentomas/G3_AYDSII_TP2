package sistema;

import controlador.Controlador;
import controlador.ControladorLogin;
import excepciones.ContactoRepetidoException;
import excepciones.PuertoInvalidoException;
import modelo.*;
import vista.VistaInicio;
import vista.VistaLogin;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;

public class Sistema {

    // Info del servidor
    private static int portServidor;
    private static String ipServidor;

    private static Sistema instance = null;
    private static UsuarioLogueado usuarioLogueado = null;

    static VistaInicio vistaInicio = new VistaInicio();
    static VistaLogin vistaLogin = new VistaLogin();

    static Controlador controlador;
    static ControladorLogin controladorLogin;


    public static void main(String[] args) {
        instance = Sistema.getInstance();

        controlador = new Controlador(vistaInicio);
        controladorLogin = new ControladorLogin(vistaLogin);

        vistaInicio.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (usuarioLogueado != null) {
                    cerrarSesion();
                }
            }
        });
    }

    // Singleton

    public static Sistema getInstance() {
        if (instance == null) {
            instance = new Sistema();
            Sistema.setServidor();
        }
        return instance;
    }

    // Getters y Setters

    public static void setServidor() {
        // Abrir y leer el archivo de configuracion
        try {
            ipServidor = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        portServidor = 8080;
    }

    public UsuarioLogueado getUsuarioLogueado() {
        return usuarioLogueado;
    }

    public static VistaInicio getVistaInicio() {
        return vistaInicio;
    }

    public static VistaLogin getVistaLogin() {
        return vistaLogin;
    }

    public Conversacion crearConversacion(String usuario) {
        // Crear una nueva conversacion
        Conversacion conversacion = new Conversacion(usuario);
        usuarioLogueado.agregarConversacion(conversacion);
        return conversacion;
    }

    // comunicacion con servidor

    public void enviarMensaje(String contenido, Conversacion conversacion) {
        Solicitud solicitud = new Solicitud(new Mensaje(contenido, usuarioLogueado.getNombre()), conversacion);
        new Thread(new Comunicador(solicitud, usuarioLogueado.getSocket())).start();
    }

    public void recibirObj(Object obj) {
        System.out.println("Recibiendo objeto: " + obj.getClass().getName());
        if (obj instanceof Mensaje mensaje) {
            recibirMensaje(mensaje);
        } else if (obj instanceof Respuesta respuesta) {
            if (respuesta.getTipo().equalsIgnoreCase("LOGIN")) {
                System.out.println("Respuesta de login");
                if (respuesta.getError()) {
                    // Si el usuario no es valido, se le muestra un mensaje de error
                    vistaLogin.mostrarModalError("El usuario ya existe.");
                } else {
                    try {
                        usuarioLogueado = new UsuarioLogueado(usuarioLogueado, new Socket(ipServidor, portServidor));
                        vistaLogin.setVisible(false);
                        vistaInicio.setVisible(true);
                        vistaInicio.setBienvenida(usuarioLogueado.getNombre());
                    } catch (IOException e) {
                        vistaLogin.mostrarModalError("Error al conectar al servidor.");
                    }
                }
            } else if (respuesta.getTipo().equalsIgnoreCase("DIRECTORIO")) {
                ArrayList<String> posiblesContactos = (ArrayList<String>) respuesta.getDatos().get("posiblesContactos");

                if (!getNoAgendados(posiblesContactos).isEmpty()) {
                    // Mostrar el modal para agregar contacto con las opciones de posiblesContactos
                    ArrayList<String> nuevoContacto = vistaInicio.mostrarModalAgregarContacto(posiblesContactos);
                    try {
                        getUsuarioLogueado().agregarContacto(nuevoContacto.get(0), nuevoContacto.get(1));
                        vistaInicio.mostrarModalExito("Contacto agregado exitosamente.");
                    } catch(ContactoRepetidoException e) {
                        vistaInicio.mostrarModalError("El contacto ya existe.");
                    }
                } else {
                    vistaInicio.mostrarModalError("Ya se tienen todos los usuario agendados.");
                }
            }
        }
    }

    public ArrayList<String> getNoAgendados(ArrayList<String> posiblesContactos) {
        ArrayList<String> noAgendados = new ArrayList<>();
        for (String contacto : posiblesContactos) {
            if (!usuarioLogueado.getContactos().containsKey(contacto)) {
                noAgendados.add(contacto);
            }
        }
        return noAgendados;
    }

    public void recibirMensaje(Mensaje mensaje) {
        System.out.println("Recibiendo mensaje: " + mensaje.getContenido());
        String emisor = mensaje.getEmisor();

        if (!usuarioLogueado.getContactos().containsKey(emisor)) { // si no conozco al emisor
            try {
                usuarioLogueado.agregarContacto(mensaje.getEmisor(), mensaje.getEmisor());
            } catch (ContactoRepetidoException e) {
                throw new RuntimeException(e);
            }
        }
        agregarMensajeConversacion(mensaje, usuarioLogueado.getConversacionCon(emisor));
    }

    public void agregarMensajeConversacion(Mensaje mensaje, Conversacion conversacion) {
        if (conversacion == null) {
            String emisor = mensaje.getEmisor();
            conversacion = new Conversacion(emisor);
        }
        usuarioLogueado.agregarConversacion(conversacion);
        usuarioLogueado.agregarMensajeaConversacion(mensaje, conversacion);

        if (vistaInicio.getConversacionActiva() == conversacion) {
            vistaInicio.actualizarPanelChat(conversacion);
        }
    }

    public void getPosiblesContactos() {
        if (usuarioLogueado != null) {
            // Enviar solicitud al servidor para obtener la lista de posibles contactos
            new Thread(new Comunicador(new Solicitud(Solicitud.DIRECTORIO), usuarioLogueado.getSocket())).start();
        }
    }

    // manejo de usuario

    /**
     * Verifica si el puerto está disponible para enlazar.
     *
     * Este metodo intenta crear un `ServerSocket` en la dirección IP y puerto especificados.
     * Si la operación tiene éxito, significa que el puerto está disponible.
     * Si se lanza una excepción, significa que el puerto ya está en uso o no es enlazable.
     *
     * @param ip La dirección IP a verificar.
     * @param port El número de puerto a verificar.
     * @return true si el puerto está disponible, false en caso contrario.
     */
    public boolean verificarPuerto(String ip, int port) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            InetSocketAddress direccion = new InetSocketAddress(InetAddress.getByName(ip), port);
            serverSocket.setReuseAddress(true);
            serverSocket.bind(direccion);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void iniciarUsuario(String nickname, String puerto) throws PuertoInvalidoException {
        try {
            // Me fijo si el puerto es valido
            verificarPuerto(InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(puerto));

            // Se asume que es correcto
            usuarioLogueado = new UsuarioLogueado(nickname, InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(puerto), new Socket(ipServidor, portServidor));

            // Iniciar el servidor para recibir mensajes
            new Thread(new HandlerMensajes(usuarioLogueado)).start();

            // Iniciar el hilo para enviar mensajes
            Solicitud solicitud = new Solicitud(Solicitud.LOGIN, Map.of("usuario", usuarioLogueado.getNombre(), "ipCliente", InetAddress.getLocalHost().getHostAddress(), "puertoCliente", Integer.parseInt(puerto)));
            new Thread(new Comunicador(solicitud, usuarioLogueado.getSocket())).start();

            // El handler de mensajes esta a la espera de mensajes
            // El comunicador va avisarle al servidor que quiere hacer login con un nickname
            // caso 1: El servidor le dice que el nickname ya existe, le envia un mensaje de error al handler
            // caso 2: El servidor le dice que el nickname no existe, le envia un mensaje de exito al handler

        } catch (IOException e) {
            vistaLogin.mostrarModalError("Error al obtener la dirección IP local.");
        }
    }

    public static void cerrarSesion() {
        try {
            new Thread(new Comunicador(new Solicitud(Solicitud.LOGOUT), usuarioLogueado.getSocket())).start();

            usuarioLogueado.getSocket().close();
            usuarioLogueado = null;
        } catch (IOException e) {
            vistaLogin.mostrarModalError("Error al cerrar la sesión.");
        }
    }

}