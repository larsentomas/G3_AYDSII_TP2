package servidor;

import excepciones.UsuarioExistenteException;
import modelo.Conversacion;
import modelo.Mensaje;
import modelo.Respuesta;
import modelo.Solicitud;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class HandlerClientes implements Runnable {
    private Socket socketRecepcion;
    private Servidor servidor;
    private ObjectInputStream inputStream;
    private volatile boolean conectado = true;

    public HandlerClientes(Socket socketRecepcion, Servidor servidor) throws IOException {
        this.socketRecepcion = socketRecepcion;
        this.servidor = servidor;
        this.inputStream = new ObjectInputStream(socketRecepcion.getInputStream());
    }

    public boolean isConectado() {
        return conectado;
    }

    public Socket getSocketRecepcion() {
        return socketRecepcion;
    }

    @Override
    public void run() {
        try {
            Object obj = inputStream.readObject();
            if (obj instanceof Solicitud request) {
                switch (request.getTipo()) {
                    case Solicitud.LOGIN -> {
                        handleLogin(request);
                    }
                    case Solicitud.LOGOUT -> {
                        handleLogout(request.getDatos().get("usuario").toString());
                    }
                    case Solicitud.DIRECTORIO -> {
                        handleDirectorio(request.getDatos().get("usuario").toString());
                    }
                    case Solicitud.ENVIAR_MENSAJE -> {
                        handleEnviarMensaje(request);
                    }
                    case Solicitud.NUEVA_CONVERSACION -> {
                        String usuario = (String) request.getDatos().get("usuario");
                        String usuarioConversacion = (String) request.getDatos().get("usuarioConversacion");
                        enviarRespuestaCliente(usuarioConversacion, Respuesta.NUEVA_CONVERSACION, Map.of("usuarioConversacion", usuario), false, null);
                    }
                    default ->
                            enviarRespuesta(request.getDatos().get("ipCliente").toString(), (int) request.getDatos().get("puertoCliente"), "UNKNOWN_REQUEST", Map.of(), true, "No se reconoce la solicitud");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection lost with client.");
        }
    }

    public void enviarRespuesta(String ip, int puerto, String tipo, Map<String, Object> datos, boolean error, String mensaje) {
        try (Socket socketEnvio = new Socket(ip, puerto)) {
            Respuesta response = new Respuesta(tipo, datos, error, mensaje);
            ObjectOutputStream outputStream = new ObjectOutputStream(socketEnvio.getOutputStream());
            outputStream.writeObject(response);
            outputStream.flush();
            System.out.println("Respuesta enviada a " + ip + ":" + puerto);
        } catch (IOException e) {
            System.out.println("Failed to send message to " + ip + ":" + puerto);
        }
    }

    public void enviarRespuestaCliente(String usuario, String tipo, Map<String, Object> datos, boolean error, String mensaje) {
        UsuarioServidor usuarioServidor = servidor.getUsuario(usuario);
        enviarRespuesta(usuarioServidor.getIp(), usuarioServidor.getPuerto(), tipo, datos, error, mensaje);
    }

    //HANDLERS EVENTOS

    private void handleLogin(Solicitud request) {
        String name = (String) request.getDatos().get("usuario");
        String ipCliente = (String) request.getDatos().get("ipCliente");
        int puertoCliente = (int) request.getDatos().get("puertoCliente");

        if (name == null || name.isBlank()) {
            enviarRespuesta(ipCliente, puertoCliente, Respuesta.LOGIN,Map.of(), true, "Nombre de usuario no valido");
        }

        try {
            servidor.logearCliente(name, ipCliente, puertoCliente);
            handleColaMensajes(name);
            enviarRespuestaCliente(name, Respuesta.LOGIN, Map.of(), false, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (UsuarioExistenteException e) {
            enviarRespuesta(ipCliente, puertoCliente, Respuesta.LOGIN, Map.of(), true, "Error al logear el cliente");
        }

    }

    private void handleLogout(String usuario){
        servidor.getUsuario(usuario).setConectado(false);
        enviarRespuestaCliente(usuario, Respuesta.LOGOUT, Map.of(), false, null);
    }

    private void handleDirectorio(String usuario){
        ArrayList<String> usuarios = servidor.getDatosDirectorio();
        if(usuarios == null || usuarios.isEmpty()){
            enviarRespuestaCliente(usuario, Respuesta.DIRECTORIO, Map.of(), true, "No hay usuarios en el directorio.");
            return;
        }else{
            enviarRespuestaCliente(usuario, Respuesta.DIRECTORIO, Map.of("usuarios", usuarios), false, null);
        }
        System.out.println("📒 Sent directory to " + usuario);
    }

    private void handleEnviarMensaje(Solicitud request){
        Mensaje msj = (Mensaje) request.getDatos().get("mensaje");
        String usuarioEmisor = msj.getEmisor();
        String usuarioReceptor = (String) request.getDatos().get("receptor");

        if (!(msj instanceof Mensaje mensaje)) {
            enviarRespuestaCliente(usuarioEmisor, Respuesta.ENVIAR_MENSAJE, Map.of(), true, "Formato de mensaje no valido.");
            return;
        }

        servidor.routeMensaje(mensaje, usuarioReceptor);
        enviarRespuestaCliente(usuarioEmisor, Respuesta.ENVIAR_MENSAJE, Map.of("mensaje", mensaje, "receptor", usuarioReceptor), false, null);
    }

    private void handleColaMensajes(String usuario){
        HashMap<String, ArrayList<Mensaje>> mensajes = new HashMap<>();
        Queue<Mensaje> colaMensajes = servidor.getMensajesOffline(usuario);

        if (colaMensajes != null) {
            while (colaMensajes.peek() != null) {
                Mensaje mensaje = colaMensajes.poll();
                ArrayList<Mensaje> mensajesUsuario = mensajes.computeIfAbsent(mensaje.getEmisor(), k -> new ArrayList<>());
                mensajesUsuario.add(mensaje);
            }
        }

        // Crea el map para enviar los mensajes por usuario
        Map<String, Object> map = new HashMap<>(mensajes);
        enviarRespuestaCliente(usuario, Respuesta.MENSAJES_OFFLINE, map, false, null);

    }
}
