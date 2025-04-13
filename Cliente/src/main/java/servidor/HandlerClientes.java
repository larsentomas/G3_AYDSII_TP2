package servidor;

import modelo.Conversacion;
import modelo.Mensaje;
import modelo.Respuesta;
import modelo.Solicitud;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
                            handleLogout();
                        }
                        case Solicitud.DIRECTORIO -> {
                            System.out.println("Peticion de directorio");
                            handleDirectorio();
                        }
                        case Solicitud.ENVIAR_MENSAJE -> {
                            System.out.println("Peticion de mensaje enviado");
                            handleEnviarMensaje(request);
                        }
                        default -> enviarRespuesta("UNKNOWN_REQUEST", Map.of(), true, "No se reconoce la solicitud");
                    }
                } else {
                    System.out.println("⚠️ Unknown object received: " + obj.getClass().getName());
                    enviarRespuesta("UNKNOWN_OBJECT", Map.of(), true, "Objeto desconocido");
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
    public void enviarRespuesta(String tipo, Map<String, Object> datos, boolean error, String mensaje) {
        System.out.println("Intentado enviar respuesta tipo " + tipo + " a " + username + " por " + socketEnvio.getInetAddress() + ":" + socketEnvio.getPort());
        try {
            Respuesta response = new Respuesta(tipo, datos, error, mensaje);
            ObjectOutputStream outputStream = new ObjectOutputStream(socketEnvio.getOutputStream());
            outputStream.writeObject(response);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Failed to send message to " + username);
        }
    }

    private void cerrarSocket() {
        try {
            socketRecepcion.close();
        } catch (IOException ignored) {}
    }

    //HANDLERS EVENTOS

    private void handleLogin(Solicitud request) throws IOException {
        String name = (String) request.getDatos().get("usuario");

        if (name == null || name.isBlank()) {
            enviarRespuesta(Solicitud.LOGIN,Map.of(), true, "Nombre de usuario no valido");
            cerrarSocket();
        }

        if(server.logearCliente(name, this)){
            this.username = name;
            this.conectado = true;
            int puertoCliente = (int) request.getDatos().get("puertoCliente");
            String ipCliente = (String) request.getDatos().get("ipCliente");
            this.socketEnvio = new Socket(ipCliente, puertoCliente);
            handleColaMensajes(name);
            enviarRespuesta(Solicitud.LOGIN, Map.of("username", name), false, null);
        }else{
            enviarRespuesta(Solicitud.LOGIN, Map.of(), true, "Error al iniciar sesion");
            cerrarSocket();
        }
    }

    private void handleLogout(){
        this.conectado = false;
        enviarRespuesta(Respuesta.LOGOUT, Map.of(), false, null);
        cerrarSocket();
    }

    private void handleDirectorio(){
        Set<String> usernames = server.getDatosDirectorio();
        if(usernames == null || usernames.isEmpty()){
            enviarRespuesta(Respuesta.DIRECTORIO, Map.of(), true, "No hay usuarios en el directorio.");
            return;
        }else{
            enviarRespuesta(Respuesta.DIRECTORIO, Map.of("usernames", usernames), false, null);
        }
        System.out.println("📒 Sent directory to " + username);
    }

    private void handleEnviarMensaje(Solicitud request){
        Mensaje msj = (Mensaje) request.getDatos().get("message");
        Conversacion c = (Conversacion) request.getDatos().get("conversacion");

        if (!(msj instanceof Mensaje mensaje)) {
            enviarRespuesta(Respuesta.ENVIAR_MENSAJE, Map.of(), true, "Formato de mensaje no valido.");
            return;
        }

        if (!username.equals(mensaje.getEmisor())) {
            enviarRespuesta(Respuesta.ENVIAR_MENSAJE, Map.of(), true, "El emisor no es correcto.");
            return;
        }
        server.routeMensaje(mensaje, c);
        enviarRespuesta(Respuesta.ENVIAR_MENSAJE, Map.of(), false, null);
    }

    private void handleColaMensajes(String name){
        Queue<Mensaje> colaMensajes = server.getMensajesOffline(name);

        if (colaMensajes != null) {
            for (Mensaje msg : colaMensajes) {
                enviarRespuesta(Respuesta.MENSAJE_RECIBIDO, Map.of("mensaje", msg), false, null);
            }
        }
    }
}
