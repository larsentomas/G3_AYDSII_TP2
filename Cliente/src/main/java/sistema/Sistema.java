package sistema;

import comandos.*;
import common.*;
import controlador.Controlador;
import controlador.ControladorLogin;
import encriptacion.CifradoCaesarClave;
import encriptacion.ContextoEncriptacion;
import encriptacion.EncriptarAES;
import excepciones.ContactoRepetidoException;
import excepciones.PuertoInvalidoException;
import modelo.*;
import persistencia.FactoryJSON;
import persistencia.FactoryTextoPlano;
import persistencia.FactoryXML;
import persistencia.PersistenciaFactory;
import vista.VistaInicio;
import vista.VistaLogin;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Sistema {

    // Info del servidor
    private static int puertoServidor;
    private static String ipServidor;
    private final String clave_encriptacion;
    private static int tipo_persistencia = 0;

    private String tipoCifrado;

    private static Sistema instance = null;
    private static UsuarioLogueado usuarioLogueado = null;

    static VistaInicio vistaInicio = new VistaInicio();
    static VistaLogin vistaLogin = new VistaLogin();

    static Controlador controlador;
    static ControladorLogin controladorLogin;

    private final Map<String, Comando> comandos = new HashMap<>();

    private ContextoEncriptacion contexto = new ContextoEncriptacion();




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
        ipServidor = Config.get("servidor.ip");
        puertoServidor = Config.getInt("servidor.puerto");
        clave_encriptacion = Config.get("clave.encriptacion");
        tipo_persistencia = Config.getInt("persistencia.tipo");
        tipoCifrado = Config.get("encriptacion.tipo");

        comandos.put("MENSAJE_RECIBIDO", new ComandoMensajeRecibido());
        comandos.put("DIRECTORIO", new ComandoDirectorio());
        comandos.put("LOGIN", new ComandoLogin());
        comandos.put("ENVIAR_MENSAJE", new ComandoEnviarMensaje());
        comandos.put("NUEVA_CONVERSACION", new ComandoNuevaConversacion());
        comandos.put("MENSAJES_OFFLINE", new ComandoMensajesOffline());
        comandos.put("LOGOUT", new ComandoLogout());

    }

    public static Sistema getInstance() {
        if (instance == null) {
            instance = new Sistema();
        }
        return instance;
    }

    // Getters y Setters


    public static int getTipoPersistencia() {return tipo_persistencia;}

    public String getTipoCifrado() {return tipoCifrado;}

    public ContextoEncriptacion getContexto() {return contexto;}

    public String getClave_encriptacion() {return clave_encriptacion;}

    public UsuarioLogueado getUsuarioLogueado() {
        return usuarioLogueado;
    }

    public void setUsuarioLogueado(UsuarioLogueado usuarioLogueado) {
        Sistema.usuarioLogueado = usuarioLogueado;
    }

    public static VistaInicio getVistaInicio() {
        return vistaInicio;
    }

    public static VistaLogin getVistaLogin() {
        return vistaLogin;
    }



    public Controlador getControlador() {return controlador;}

    public ControladorLogin getControladorLogin() {return controladorLogin;}

    public Conversacion crearConversacion(String apodo) {
        // Crear una nueva conversacion
        String usuario = usuarioLogueado.getContacto(apodo);
        Conversacion c = usuarioLogueado.crearConversacion(usuario);
        Solicitud sol = new Solicitud(Solicitud.NUEVA_CONVERSACION, Map.of("usuario", usuarioLogueado.getNombre(), "usuarioConversacion", usuario));
        enviarAServidor(sol);
        return c;
    }

    public void enviarMensaje(String contenido, Conversacion conversacion) throws IOException {
        String tipoCifrado = this.tipoCifrado;
        switch (tipoCifrado) {
            case "0":
                contexto.setEstrategia(new CifradoCaesarClave());
                break;
            case "1":
                contexto.setEstrategia(new EncriptarAES());
                break;
            default:
                throw new IllegalArgumentException("Tipo de cifrado inválido: " + tipoCifrado);
        }
        Mensaje mensajeEncriptado = this.contexto.encriptar(new Mensaje(contenido, usuarioLogueado.getNombre()), clave_encriptacion); // ACA LLAMA A LA FUNCION ENCRIPTAR
        Solicitud solicitud = new Solicitud(Solicitud.ENVIAR_MENSAJE, Map.of("mensaje", mensajeEncriptado, "receptor", conversacion.getIntegrante()));
        enviarAServidor(solicitud);
    }

    public void recibirObj(Object obj) {
        if (obj instanceof Respuesta respuesta) {
            Comando comando = comandos.get(respuesta.getTipo());
            if (comando != null) {
                comando.ejecutar(respuesta, this);
            } else {
                System.out.println("No se encontro un comando para: " + respuesta.getTipo());
            }
        }
    }

    public void recibirMensajesOffline(Respuesta respuesta) {
        Set<Map.Entry<String, Object>> keys = respuesta.getDatos().entrySet();

        for (Map.Entry<String, Object> entry : keys) {
            String usuario = entry.getKey();
            ArrayList<Mensaje> mensajes = (ArrayList<Mensaje>) entry.getValue();
            try {
                Conversacion conversacion;
                if (!usuarioLogueado.getContactos().containsKey(usuario)) {
                    usuarioLogueado.agregarContacto(usuario, usuario);
                    conversacion = usuarioLogueado.crearConversacion(usuario);
                } else if (usuarioLogueado.getConversacionCon(usuario) == null) {
                    conversacion = usuarioLogueado.crearConversacion(usuario);
                } else {
                    conversacion = usuarioLogueado.getConversacionCon(usuario);
                }
                for (Mensaje mensaje : mensajes) {
                    switch (this.tipoCifrado) {
                        case "0":
                            contexto.setEstrategia(new CifradoCaesarClave());
                            break;
                        case "1":
                            contexto.setEstrategia(new EncriptarAES());
                            break;
                        default:
                            throw new IllegalArgumentException("Tipo de cifrado inválido: " + tipoCifrado);
                    }
                    mensaje = contexto.desencriptar(mensaje, this.clave_encriptacion);
                    usuarioLogueado.agregarMensajeaConversacion(mensaje, conversacion);
                }
            } catch (ContactoRepetidoException e) {
                e.printStackTrace();
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
        String tipoCifrado = this.tipoCifrado;
        switch (tipoCifrado) {
            case "0":
                contexto.setEstrategia(new CifradoCaesarClave());
                break;
            case "1":
                contexto.setEstrategia(new EncriptarAES());
                break;
            default:
                throw new IllegalArgumentException("Tipo de cifrado inválido: " + tipoCifrado);
        }
        usuarioLogueado.agregarMensajeaConversacion(contexto.desencriptar(mensaje, clave_encriptacion), conversacion); // ACA ENCRIPTA

        if (controlador.getConversacionActiva() == conversacion) {
            controlador.actualizarPanelChat(conversacion);
        }
    }

    public void getPosiblesContactos() throws IOException {
        if (usuarioLogueado != null) {
            // Enviar solicitud al servidor para obtener la lista de posibles contactos
            Solicitud s = new Solicitud(Solicitud.DIRECTORIO, Sistema.getInstance().getUsuarioLogueado().getNombre());
            enviarAServidor(s);
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
            enviarAServidor(solicitud);

        } catch (IOException e) {
            usuarioLogueado = null;
            controladorLogin.mostrarModalError("Error al obtener la dirección IP local.");
        }
    }

    public static void cerrarSesion() {
        try {
            enviarAServidor(new Solicitud(Solicitud.LOGOUT, Sistema.getInstance().getUsuarioLogueado().getNombre()));
            controlador.setVisible(false);

            // PERSISTENCIA
            PersistenciaFactory p;
            switch (Sistema.tipo_persistencia) {
                case(0):
                    p = new FactoryJSON();
                    break;
                case(1):
                    p = new FactoryXML();
                    break;
                case(2):
                    p = new FactoryTextoPlano();
                    break;
                default:
                    p = new FactoryJSON();
                    break;
            }
            p.crearSaver().persistir(usuarioLogueado);

            System.exit(1);
        } catch (Exception e) {
            controladorLogin.mostrarModalError("Error al cerrar la sesión.");
        }
    }

    public static void enviarAServidor(Solicitud s) {
        Callable<Void> task = () -> {
            new Comunicador(s, puertoServidor, ipServidor).run();
            return null;
        };
        FutureTask<Void> future = new FutureTask<>(task);
        new Thread(future).start();
        try {
            future.get(); // Esto lanzará la excepción si ocurre en el Comunicador
            System.out.println("A la espera de confirmacion");
        } catch (InterruptedException | ExecutionException e) {
            controlador.mostrarModalError("El servidor no esta disponible. Cerrar sesion");
            controlador.setVisible(false);
            System.exit(1);
        }
    }


}