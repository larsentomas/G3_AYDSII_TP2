package sistema;

import java.io.*;
import java.net.*;
import java.util.HashMap;
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

    @Override
    public void run() {
        try {
            Object obj;

            while ((obj = inputStream.readObject()) != null) {

                // Handle chat messages
                if (obj instanceof Mensaje mensaje) {
                    server.routeMensaje(mensaje);
                    enviarRespuesta("MENSAJE_RECIBIDO", Map.of(), false, null);
                }

                // Handle protocol requests
                else if (obj instanceof Solicitud request) {
                    switch (request.getTipo()) {
                        case "LOGIN" -> {
                            String name = request.getDatos().get("username");

                            if (name == null || name.isBlank()) {
                                enviarRespuesta("LOGIN",Map.of(), true, "Nombre de usuario no valido");
                                socket.close();
                                return;
                            }

                            if (server.registerClient(name, this)) {
                                this.username = name;
                                this.conectado = true;
                                enviarRespuesta("LOGIN", Map.of(), false,"");

                                Queue<Mensaje> queued = server.getMensajesOffline(username);
                                if (queued != null) {

                                    for(Mensaje m:queued){
                                        enviarRespuesta("MENSAJE_ENVIADO", Map.of("mensaje", m), false, "");
                                    }
                                }

                            } else {
                                enviarRespuesta("LOGIN", Map.of(), true, "Nombre de usuario ya en uso");
                                socket.close();
                                return;
                            }
                        }
                        case "LOGOUT" -> {
                            this.conectado = false;
                            enviarRespuesta("LOGOUT", Map.of(), false, null);
                            socket.close();
                            return; // finalizar thread
                        }

                        case "DIRECTORIO" -> {
                            Set<String> usernames = server.getAllUsernames();
                            outputStream.writeObject(usernames);
                            outputStream.flush();
                            System.out.println("📒 Sent directory to " + username);
                        }

                        default -> enviarRespuesta("UNKNOWN_REQUEST", Map.of(), true, "No se reconoce la solicitud");
                    }
                }

                else {
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
}
