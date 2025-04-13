package sistema;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class Servidor {

    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, HandlerClientes> directorio = new ConcurrentHashMap<>();
    private final Map<String, Queue<Mensaje>> colaMensajes = new ConcurrentHashMap<>();


    public static void main(String[] args) throws IOException {
        new Servidor().start(8080);
    }

    public void start(int port) {
        try {
            //Por ahora el servidor corre en la maquina local
            serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado en el puerto " + port);

            while (true) {
                // Accept incoming connections
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress());

                // Create a new handler for the client
                HandlerClientes handler = new HandlerClientes(socket, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void routeMensaje(Mensaje mensaje) {
        HandlerClientes receptor = directorio.get(mensaje.getReceptor());
        if (receptor != null) {
            if (receptor.isConectado()) {
                receptor.enviarRespuesta("MENSAJE_RECIBIDO", Map.of("mensaje", mensaje), false, null);
            } else {
                colaMensajes.computeIfAbsent(mensaje.getReceptor(), k -> new LinkedList<>()).add(mensaje);
                System.out.println("Stored message for " + mensaje.getReceptor());
            }
        } else {
            System.out.println("Usuario " + mensaje.getReceptor() + " no encontrado.");
        }
    }

    public Queue<Mensaje> getMensajesOffline(String username) {
        return colaMensajes.remove(username);
    }

    public boolean registerClient(String username, HandlerClientes handler) {
        HandlerClientes existe = directorio.get(username);
        if (existe != null && existe.isConectado()) {
            return false;
        }

        directorio.put(username, handler);
        return true;
    }

    public void removeClient(String username){
        directorio.remove(username);
    }

    public Set<String> getAllUsernames() {
        return directorio.keySet();
    }
}