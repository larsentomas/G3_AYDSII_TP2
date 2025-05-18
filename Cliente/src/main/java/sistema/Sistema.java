package sistema;

import common.*;
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
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sistema {

    // Info del servidor
    private static int puertoServidor;
    private static String ipServidor;

    private static Sistema instance = null;
    private static UsuarioLogueado usuarioLogueado = null;
    private volatile boolean serverOnline = false;

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

    private Sistema() {
        // Constructor privado para evitar instanciación externa

        // Abrir y leer el archivo de configuracion
        //ipServidor = InetAddress.getLocalHost().getHostAddress();
        ipServidor = Config.get("servidor.ip");
        //puertoServidor = 6000;
        puertoServidor = Config.getInt("servidor.puerto");
        selfTest(ipServidor, puertoServidor);
    }

    public static Sistema getInstance() {
        if (instance == null) {
            instance = new Sistema();
        }
        return instance;
    }

    // Getters y Setters

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
            System.out.println("A la espera de confirmacion");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return c;
    }

    // comunicacion con servidor

    public void enviarMensaje(String contenido, Conversacion conversacion) throws IOException {
        Solicitud solicitud = new Solicitud(Solicitud.ENVIAR_MENSAJE, Map.of("mensaje", new Mensaje(contenido, usuarioLogueado.getNombre()), "receptor", conversacion.getIntegrante()));
        new Thread(new Comunicador(solicitud, puertoServidor, ipServidor)).start();
        System.out.println("A la espera de confirmacion");
    }

    public void recibirObj(Object obj) {
        if (obj instanceof Respuesta respuesta) {
            System.out.println(respuesta);
            switch(respuesta.getTipo()) {
                case Respuesta.MENSAJE_RECIBIDO -> {

                    Mensaje mensaje = (Mensaje) respuesta.getDatos().get("mensaje");
                    String emisor = mensaje.getEmisor();

                    Conversacion conversacion;
                    if (!usuarioLogueado.getContactos().containsKey(emisor)) {
                        try {
                            usuarioLogueado.agregarContacto(emisor, emisor);
                            conversacion = usuarioLogueado.crearConversacion(emisor);
                        } catch (ContactoRepetidoException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        conversacion = usuarioLogueado.getConversacionCon(emisor);
                    }
                    agregarMensajeConversacion(mensaje, conversacion);

                    if (conversacion == controlador.getConversacionActiva()) {
                        controlador.actualizarPanelChat(conversacion);
                    } else {
                        conversacion.setNotificado(true);
                    }
                    controlador.actualizarListaConversaciones();

                }
                case Respuesta.DIRECTORIO -> recibirListaUsuarios(respuesta);
                case Respuesta.LOGIN -> {
                    if (respuesta.getError()) {
                        // Si el usuario no es valido, se le muestra un mensaje de error
                        usuarioLogueado = null;
                        vistaLogin.mostrarModalError("El usuario ya existe.");
                    } else {
                        controladorLogin.setVisible(false);
                        controlador.setVisible(true);
                        controlador.setBienvenida(usuarioLogueado.getNombre());
                    }
                }
                case Respuesta.ENVIAR_MENSAJE -> {
                    if (respuesta.getError()) {
                        // Si el mensaje no se pudo enviar, se le muestra un mensaje de error
                        controlador.mostrarModalError("El mensaje no se pudo enviar.");
                    } else {
                        // Si el mensaje se envio correctamente, se le muestra un mensaje de exito
                        Mensaje mensaje = (Mensaje) respuesta.getDatos().get("mensaje");
                        String receptor = (String) respuesta.getDatos().get("receptor");
                        Conversacion c = usuarioLogueado.getConversacionCon(receptor);
                        usuarioLogueado.agregarMensajeaConversacion(mensaje, c);
                        controlador.actualizarPanelChat(c);
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
                    controlador.actualizarListaConversaciones();
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
        controlador.actualizarListaConversaciones();

    }

    public void recibirListaUsuarios(Respuesta respuesta) {
        ArrayList<String> listaUsuarios = (ArrayList<String>) respuesta.getDatos().get("usuarios");
        listaUsuarios.remove(usuarioLogueado.getNombre());

        ArrayList<String> noAgendados = new ArrayList<>();
        for (String contacto : listaUsuarios) {
            if (!usuarioLogueado.getContactos().containsKey(contacto)) {
                noAgendados.add(contacto);
            }
        }


        if (!noAgendados.isEmpty()) {
            // Mostrar el modal para agregar contacto con las opciones de listaUsuarios
            ArrayList<String> nuevoContacto = controlador.mostrarModalAgregarContacto(noAgendados);
            if (nuevoContacto != null) {
                try {
                    getUsuarioLogueado().agregarContacto(nuevoContacto.getFirst(), nuevoContacto.get(1));
                    controlador.mostrarModalExito("Contacto agregado exitosamente.");
                } catch (ContactoRepetidoException e) {
                    controlador.mostrarModalError("El contacto ya existe.");
                }
            }
        } else {
            controlador.mostrarModalError("Ya se tienen todos los usuario agendados.");
        }
    }


    public void agregarMensajeConversacion(Mensaje mensaje, Conversacion conversacion) {
        usuarioLogueado.agregarMensajeaConversacion(mensaje, conversacion);

        if (controlador.getConversacionActiva() == conversacion) {
            controlador.actualizarPanelChat(conversacion);
        }
    }

    public void getPosiblesContactos() throws IOException {
        if (usuarioLogueado != null) {
            // Enviar solicitud al servidor para obtener la lista de posibles contactos
            Solicitud s = new Solicitud(Solicitud.DIRECTORIO, Sistema.getInstance().getUsuarioLogueado().getNombre());
            new Thread(new Comunicador(s, puertoServidor, ipServidor)).start();
            System.out.println("A la espera de confirmacion");
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

    public boolean servidorActivo(String ip, int puerto) {
        try (Socket socket = new Socket(ip, puerto)) {
            // Si o si hay que escribir algo porque sino el servidor intenta leer y no hay nada y falla
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject("PROBANDO");
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

            if (!servidorActivo(ipServidor, puertoServidor)) {
                controladorLogin.mostrarModalError("No se pudo conectar al servidor");
                return;
            }
            // Iniciar el hilo para enviar mensajes
            Solicitud solicitud = new Solicitud(Solicitud.LOGIN, Map.of("usuario", usuarioLogueado.getNombre(), "ipCliente", InetAddress.getLocalHost().getHostAddress(), "puertoCliente", Integer.parseInt(puerto)));
            new Thread(new Comunicador(solicitud, puertoServidor, ipServidor)).start();

            System.out.println("A la espera de confirmacion");

        } catch (IOException e) {
            usuarioLogueado = null;
            controladorLogin.mostrarModalError("Error al obtener la dirección IP local.");
        }
    }

    public static void cerrarSesion() {
        try {
            new Thread(new Comunicador(new Solicitud(Solicitud.LOGOUT, Sistema.getInstance().getUsuarioLogueado().getNombre()), puertoServidor, ipServidor)).start();
            usuarioLogueado = null;
            controlador.setVisible(false);
           // controladorLogin.setVisible(true);
        } catch (IOException e) {
            controladorLogin.mostrarModalError("Error al cerrar la sesión.");
        }
    }

    //SELF-TEST

    private void selfTest(String ip, int puerto) {
        ScheduledExecutorService pingScheduler = Executors.newSingleThreadScheduledExecutor();

        pingScheduler.scheduleAtFixedRate(() -> {
            try {
                Socket socket = new Socket(ip, puerto);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                Map<String, Object> datos = new HashMap<>();
                datos.put("origin", "client");
                Solicitud ping = new Solicitud(Solicitud.PING, datos);

                synchronized (out) {
                    out.writeObject(ping);
                    out.flush();
                }
                System.out.println("Ping sent");

            } catch (IOException e) {
                System.err.println("Ping failed: " + e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

}