package sistema;

import java.io.*;
import java.net.*;
import java.util.Queue;

public class HandlerClientes implements Runnable {
    private Socket socket;
    private Servidor server;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String username;
    private volatile boolean conectado = true;

    public HandlerClientes(Socket socket, Servidor server){
        this.socket = socket;
        this.server = server;
        try {
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Error " + e.getMessage());
        }
    }

    public boolean isConectado() {
        return conectado;
    }

    public String getUsername() {
        return username;
    }

    public void run(){

        try{
            Object obj;

            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(socket.getInputStream());

            Object firstObj = inputStream.readObject();
            if (!(firstObj instanceof String)) {
                System.out.println("Se esperaba un nombre de usuario");
                socket.close();
                return;
            }

            username = (String) firstObj;
            if (!server.registerClient(username, this)) {
                sendMessage(new Mensaje("Server", username, "No Valido"));
                socket.close();
                return;
            }else{
                sendMessage(new Mensaje("Server", username, "Valido"));
            }

            System.out.println(username + "Conectado.");

            Queue<Mensaje> queue = server.getMensajesOffline(username);
            if (queue != null) {
                while (!queue.isEmpty()) {
                    sendMessage(queue.poll());
                }
            }

            Object obj;
            while ((obj = inputStream.readObject()) != null) {
                if (obj instanceof Mensaje mensajito) {
                    System.out.println("[" + username + " → " + mensajito.getReceptor() + "]: " + mensajito.getContenido());
                    server.routeMensaje(mensajito);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            conectado = false; // ✅ Just mark as disconnected
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    @Override
    public void run() {
        try {
            Object obj;

            // Main loop: handle incoming objects from client
            while ((obj = inputStream.readObject()) != null) {

                // Handle chat messages
                if (obj instanceof Mensaje mensaje) {
                    server.routeMensaje(mensaje);
                }

                // Handle protocol requests
                else if (obj instanceof Solicitud request) {
                    switch (request.getTipo()) {
                        case "LOGIN" -> {
                            String name = request.getDatos().get("username");

                            if (name == null || name.isBlank()) {
                                sendMessage(new Mensaje("Server", username, "No se especifico el nombre de usuario"));
                                socket.close();
                                return;
                            }

                            // Register user with server
                            if (server.registerClient(name, this)) {
                                this.username = name;
                                this.conectado = true;
                                sendMessage(new Mensaje("Server", username, "Valido"));

                                // Deliver offline messages if any
                                Queue<Mensaje> queued = server.getMensajesOffline(username);
                                if (queued != null) {
                                    queued.forEach(this::sendMessage);
                                }

                            } else {
                                sendMessage(new Mensaje("Server", username, "No Valido"));
                                socket.close();
                                return;
                            }
                        }
                        case "LOGOUT" -> {
                            this.conectado = false;
                            socket.close();
                            return; // finalizar thread
                        }

                        case "GET_DIRECTORY" -> {
                            Set<String> usernames = server.getAllUsernames();
                            out.writeObject(usernames);
                            out.flush();
                            System.out.println("📒 Sent directory to " + username);
                        }

                        default -> System.out.println("⚠️ Unknown request: " + request.getType());
                    }
                }

                else {
                    System.out.println("⚠️ Unknown object type received from client.");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("⚡ Connection lost with " + username);
        } finally {
            // Gracefully mark as disconnected
            if (username != null) {
                connected = false;
                System.out.println("🔌 " + username + " disconnected.");
            }

            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
    public void sendMessage(Mensaje mensajito) {
        try {
            outputStream.writeObject(mensajito);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Failed to send message to " + username);
        }
    }
}
