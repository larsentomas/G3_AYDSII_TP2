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
import java.util.Set;

public class Sistema {

    // Info del servidor
    private static int puertoServidor;
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
        puertoServidor = 6000;
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

    public Conversacion crearConversacion(String apodo) {
        // Crear una nueva conversacion
        String usuario = usuarioLogueado.getContacto(apodo);
        Conversacion c = usuarioLogueado.crearConversacion(usuario);
        Solicitud sol = new Solicitud(Solicitud.NUEVA_CONVERSACION, Map.of("usuario", usuarioLogueado.getNombre(), "usuarioConversacion", usuario));
        try {
            new Thread(new Comunicador(sol, puertoServidor, ipServidor)).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return c;
    }

    // comunicacion con servidor

    public void enviarMensaje(String contenido, Conversacion conversacion) throws IOException {
        Solicitud solicitud = new Solicitud(Solicitud.ENVIAR_MENSAJE, Map.of("mensaje", new Mensaje(contenido, usuarioLogueado.getNombre()), "receptor", conversacion.getIntegrante()));
        new Thread(new Comunicador(solicitud, puertoServidor, ipServidor)).start();
    }

    public void recibirObj(Object obj) {
        if (obj instanceof Respuesta respuesta) {
            switch(respuesta.getTipo()) {
                case Respuesta.MENSAJE_RECIBIDO -> {
                    Mensaje mensaje = (Mensaje) respuesta.getDatos().get("mensaje");
                    String emisor = mensaje.getEmisor();

                    System.out.println("Recibo " + mensaje + " de " + emisor);

                    Conversacion conversacion;
                    if (!usuarioLogueado.getContactos().containsKey(emisor)) {
                        try {
                            usuarioLogueado.agregarContacto(emisor, emisor);
                            conversacion = usuarioLogueado.crearConversacion(emisor);
                            System.out.println("No existia conversacion con " + emisor + ", la creo");
                        } catch (ContactoRepetidoException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        conversacion = usuarioLogueado.getConversacionCon(emisor);
                    }
                    System.out.println("Conversacion es " + conversacion);
                    agregarMensajeConversacion(mensaje, conversacion);

                    if (conversacion == vistaInicio.getConversacionActiva()) {
                        vistaInicio.actualizarPanelChat(conversacion);
                    } else {
                        conversacion.setNotificado(true);
                        vistaInicio.actualizarListaConversaciones();
                    }

                }
                case Respuesta.DIRECTORIO -> recibirListaUsuarios(respuesta);
                case Respuesta.LOGIN -> {
                    System.out.println("Respuesta de login");
                    if (respuesta.getError()) {
                        // Si el usuario no es valido, se le muestra un mensaje de error
                        usuarioLogueado = null;
                        vistaLogin.mostrarModalError("El usuario ya existe.");
                    } else {
                        vistaLogin.setVisible(false);
                        vistaInicio.setVisible(true);
                        vistaInicio.setBienvenida(usuarioLogueado.getNombre());
                    }
                }
                case Respuesta.ENVIAR_MENSAJE -> {
                    if (respuesta.getError()) {
                        // Si el mensaje no se pudo enviar, se le muestra un mensaje de error
                        vistaInicio.mostrarModalError("El mensaje no se pudo enviar.");
                    } else {
                        // Si el mensaje se envio correctamente, se le muestra un mensaje de exito
                        Mensaje mensaje = (Mensaje) respuesta.getDatos().get("mensaje");
                        String receptor = (String) respuesta.getDatos().get("receptor");
                        Conversacion c = usuarioLogueado.getConversacionCon(receptor);
                        usuarioLogueado.agregarMensajeaConversacion(mensaje, c);
                        vistaInicio.actualizarPanelChat(c);
                    }
                }
                case Respuesta.NUEVA_CONVERSACION -> {
                    String usuarioConversacion = (String) respuesta.getDatos().get("usuarioConversacion");
                    if (!usuarioLogueado.getContactos().containsKey(usuarioConversacion)) {
                        try {
                            usuarioLogueado.agregarContacto(usuarioConversacion, usuarioConversacion);
                        } catch (ContactoRepetidoException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    usuarioLogueado.crearConversacion(usuarioConversacion);
                    vistaInicio.actualizarListaConversaciones();
                }
                case Respuesta.MENSAJES_OFFLINE -> recibirMensajesOffline(respuesta);
            }
        }
    }

    public void recibirMensajesOffline(Respuesta respuesta) {
        Set<Map.Entry<String, Object>> keys = respuesta.getDatos().entrySet();

        for (Map.Entry<String, Object> entry : keys) {
            String usuario = entry.getKey();
            ArrayList<Mensaje> mensajes = (ArrayList<Mensaje>) entry.getValue();
            try {
                usuarioLogueado.agregarContacto(usuario, usuario);
                Conversacion conversacion = usuarioLogueado.crearConversacion(usuario);
                for (Mensaje mensaje : mensajes) {
                    usuarioLogueado.agregarMensajeaConversacion(mensaje, conversacion);
                }
            } catch (ContactoRepetidoException e) {
                throw new RuntimeException(e);
            }
        }
        vistaInicio.actualizarListaConversaciones();

    }

    public void recibirListaUsuarios(Respuesta respuesta) {
        ArrayList<String> listaUsuarios = (ArrayList<String>) respuesta.getDatos().get("usuarios");
        listaUsuarios.remove(usuarioLogueado.getNombre()); // Me saco a mi mismo

        ArrayList<String> noAgendados = new ArrayList<>();
        for (String contacto : listaUsuarios) {
            if (!usuarioLogueado.getContactos().containsKey(contacto)) {
                noAgendados.add(contacto);
            }
        }


        if (!noAgendados.isEmpty()) {
            // Mostrar el modal para agregar contacto con las opciones de listaUsuarios
            ArrayList<String> nuevoContacto = vistaInicio.mostrarModalAgregarContacto(noAgendados);
            if (nuevoContacto != null) {
                try {
                    getUsuarioLogueado().agregarContacto(nuevoContacto.getFirst(), nuevoContacto.get(1));
                    vistaInicio.mostrarModalExito("Contacto agregado exitosamente.");
                } catch (ContactoRepetidoException e) {
                    vistaInicio.mostrarModalError("El contacto ya existe.");
                }
            }
        } else {
            vistaInicio.mostrarModalError("Ya se tienen todos los usuario agendados.");
        }
    }


    public void agregarMensajeConversacion(Mensaje mensaje, Conversacion conversacion) {
        usuarioLogueado.agregarMensajeaConversacion(mensaje, conversacion);

        if (vistaInicio.getConversacionActiva() == conversacion) {
            vistaInicio.actualizarPanelChat(conversacion);
        }
    }

    public void getPosiblesContactos() throws IOException {
        if (usuarioLogueado != null) {
            // Enviar solicitud al servidor para obtener la lista de posibles contactos
            new Thread(new Comunicador(new Solicitud(Solicitud.DIRECTORIO), puertoServidor, ipServidor)).start();
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
            usuarioLogueado = new UsuarioLogueado(nickname, InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(puerto));

            // Iniciar el servidor para recibir mensajes
            new Thread(new HandlerMensajes(usuarioLogueado)).start();

            // Iniciar el hilo para enviar mensajes
            Solicitud solicitud = new Solicitud(Solicitud.LOGIN, Map.of("usuario", usuarioLogueado.getNombre(), "ipCliente", InetAddress.getLocalHost().getHostAddress(), "puertoCliente", Integer.parseInt(puerto)));
            new Thread(new Comunicador(solicitud, puertoServidor, ipServidor)).start();

            // El handler de mensajes esta a la espera de mensajes
            // El comunicador va avisarle al servidor que quiere hacer login con un nickname
            // caso 1: El servidor le dice que el nickname ya existe, le envia un mensaje de error al handler
            // caso 2: El servidor le dice que el nickname no existe, le envia un mensaje de exito al handler

        } catch (IOException e) {
            usuarioLogueado = null;
            vistaLogin.mostrarModalError("Error al obtener la dirección IP local.");
        }
    }

    public static void cerrarSesion() {
        try {
            new Thread(new Comunicador(new Solicitud(Solicitud.LOGOUT), puertoServidor, ipServidor)).start();
            usuarioLogueado = null;
            vistaInicio.setVisible(false);
            System.exit(0);
        } catch (IOException e) {
            vistaLogin.mostrarModalError("Error al cerrar la sesión.");
        }
    }

}