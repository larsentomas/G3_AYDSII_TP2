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
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class HandlerClientes implements Runnable {
    private Socket socketRecepcion;
    private Socket socketEnvio;
    private Servidor server;
    private ObjectInputStream inputStream;
    private String username;
    private volatile boolean conectado = true;

    public HandlerClientes(Socket socketRecepcion, Servidor server) throws IOException {
        this.socketRecepcion = socketRecepcion;
        this.server = server;
        this.inputStream = new ObjectInputStream(socketRecepcion.getInputStream());
    }

    public boolean isConectado() {
        return conectado;
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocketRecepcion() {
        return socketRecepcion;
    }

    @Override
    public void run() {
        try {
            Object obj;
            System.out.println("Esperando a leer obj");
            while ((obj = inputStream.readObject()) != null) {
                System.out.println("Recibido objeto: " + obj.getClass().getName());
                if (obj instanceof Solicitud request) {
                    switch (request.getTipo()) {
                        case Solicitud.LOGIN -> {
                            System.out.println("Peticion de login");
                            handleLogin(request);
                        }
                        case Solicitud.LOGOUT -> {
                            System.out.println("Peticion de logout");
                            handleLogout(request.getDatos().get("usuario").toString());
                        }
                        case Solicitud.DIRECTORIO -> {
                            System.out.println("Peticion de directorio");
                            handleDirectorio(request.getDatos().get("usuario").toString());
                        }
                        case Solicitud.ENVIAR_MENSAJE -> {
                            System.out.println("Peticion de mensaje enviado");
                            handleEnviarMensaje(request);
                        }
                        default ->
                                enviarRespuesta(request.getDatos().get("ipCliente").toString(), (int) request.getDatos().get("puertoCliente"), "UNKNOWN_REQUEST", Map.of(), true, "No se reconoce la solicitud");
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection lost with " + username);
        } finally {
            if (username != null) {
                conectado = false;
                System.out.println(username + " disconnected.");
            }

            try {
                socketRecepcion.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void enviarRespuesta(String ip, int puerto, String tipo, Map<String, Object> datos, boolean error, String mensaje) {
        try (Socket socketEnvio = new Socket(ip, puerto)) {
            Respuesta response = new Respuesta(tipo, datos, error, mensaje);
            ObjectOutputStream outputStream = new ObjectOutputStream(socketEnvio.getOutputStream());
            outputStream.writeObject(response);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Failed to send message to " + username);
        }
    }

    public void enviarRespuestaCliente(String usuario, String tipo, Map<String, Object> datos, boolean error, String mensaje) {
        System.out.println("Intentado enviar respuesta tipo " + tipo);
        UsuarioServidor usuarioServidor = server.getUsuario(usuario);
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
            server.logearCliente(name, ipCliente, puertoCliente);
            handleColaMensajes(name);
            enviarRespuestaCliente(name, Respuesta.LOGIN, Map.of(), false, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (UsuarioExistenteException e) {
            enviarRespuesta(ipCliente, puertoCliente, Respuesta.LOGIN, Map.of(), true, "Error al logear el cliente");
        }

    }

    private void handleLogout(String usuario){
        this.conectado = false;
        enviarRespuestaCliente(usuario, Respuesta.LOGOUT, Map.of(), false, null);
    }

    private void handleDirectorio(String usuario){
        ArrayList<String> usernames = server.getDatosDirectorio();
        if(usernames == null || usernames.isEmpty()){
            enviarRespuestaCliente(usuario, Respuesta.DIRECTORIO, Map.of(), true, "No hay usuarios en el directorio.");
            return;
        }else{
            enviarRespuestaCliente(usuario, Respuesta.DIRECTORIO, Map.of("usernames", usernames), false, null);
        }
        System.out.println("📒 Sent directory to " + username);
    }

    private void handleEnviarMensaje(Solicitud request){
        String usuario = (String) request.getDatos().get("usuario");
        Mensaje msj = (Mensaje) request.getDatos().get("message");
        Conversacion c = (Conversacion) request.getDatos().get("conversacion");

        if (!(msj instanceof Mensaje mensaje)) {
            enviarRespuestaCliente(usuario, Respuesta.ENVIAR_MENSAJE, Map.of(), true, "Formato de mensaje no valido.");
            return;
        }

        if (!username.equals(mensaje.getEmisor())) {
            enviarRespuestaCliente(usuario, Respuesta.ENVIAR_MENSAJE, Map.of(), true, "El emisor no es correcto.");
            return;
        }
        server.routeMensaje(mensaje, c);
        enviarRespuestaCliente(usuario, Respuesta.ENVIAR_MENSAJE, Map.of(), false, null);
    }

    private void handleColaMensajes(String usuario){
        Queue<Mensaje> colaMensajes = server.getMensajesOffline(usuario);

        if (colaMensajes != null) {
            for (Mensaje msg : colaMensajes) {
                enviarRespuestaCliente(usuario, Respuesta.MENSAJE_RECIBIDO, Map.of("mensaje", msg), false, null);
            }
        }
    }
}
