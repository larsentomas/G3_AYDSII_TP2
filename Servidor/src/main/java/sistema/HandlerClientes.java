package sistema;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class HandlerClientes implements Runnable {
    private Socket socket;
    private Servidor server;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String username;
    private volatile boolean conectado = true;

    public HandlerClientes(Socket socket, Servidor server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.outputStream.flush();
        this.inputStream = new ObjectInputStream(socket.getInputStream());
    }

    public boolean isConectado() {
        return conectado;
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        try {
            Object obj;

            while ((obj = inputStream.readObject()) != null) {
                if (obj instanceof Solicitud request) {
                    switch (request.getTipo()) {
                        case "LOGIN" -> {
                            handleLogin(request);
                        }
                        case "LOGOUT" -> {
                            handleLogout();
                        }
                        case "DIRECTORIO" -> {
                            handleDirectorio();
                        }
                        case "MENSAJE_ENVIADO" -> {
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
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
    public void enviarRespuesta(String tipo, Map<String, Object> datos, boolean error, String mensaje) {
        try {
            Respuesta response = new Respuesta(tipo, datos, error, mensaje);
            outputStream.writeObject(response);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Failed to send message to " + username);
        }
    }

    private void cerrarSocket() {
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    //HANDLERS EVENTOS

    private void handleLogin(Solicitud request){
        String name = (String) request.getDatos().get("username");

        if (name == null || name.isBlank()) {
            enviarRespuesta("LOGIN",Map.of(), true, "Nombre de usuario no valido");
            cerrarSocket();
        }

        if(server.logearCliente(name, this)){
            this.username = name;
            this.conectado = true;
            handleColaMensajes(name);
            enviarRespuesta("LOGIN", Map.of("username", name), false, null);
        }else{
            enviarRespuesta("LOGIN", Map.of(), true, "Error al iniciar sesion");
            cerrarSocket();
        }
    }

    private void handleLogout(){
        this.conectado = false;
        enviarRespuesta("LOGOUT", Map.of(), false, null);
        cerrarSocket();
    }

    private void handleDirectorio(){
        Set<String> usernames = server.getDatosDirectorio();
        if(usernames == null || usernames.isEmpty()){
            enviarRespuesta("DIRECTORIO", Map.of(), true, "No hay usuarios en el directorio.");
            return;
        }else{
            enviarRespuesta("DIRECTORIO", Map.of("usernames", usernames), false, null);
        }
        System.out.println("📒 Sent directory to " + username);
    }

    private void handleEnviarMensaje(Solicitud request){
        Mensaje msj = (Mensaje) request.getDatos().get("message");

        if (!(msj instanceof Mensaje mensaje)) {
            enviarRespuesta("SEND_MESSAGE", Map.of(), true, "Formato de mensaje no valido.");
            return;
        }

        if (!username.equals(mensaje.getEmisor())) {
            enviarRespuesta("SEND_MESSAGE", Map.of(), true, "El emisor no es correcto.");
            return;
        }
        server.routeMensaje(mensaje);
        enviarRespuesta("SEND_MESSAGE", Map.of(), false, null);
    }

    private void handleColaMensajes(String name){
        Queue<Mensaje> colaMensajes = server.getMensajesOffline(name);

        if (colaMensajes != null) {
            for (Mensaje msg : colaMensajes) {
                enviarRespuesta("MENSAJE_RECIBIDO", Map.of("mensaje", msg), false, null);
            }
        }
    }
}
