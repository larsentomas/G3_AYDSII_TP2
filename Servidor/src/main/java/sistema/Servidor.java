package sistema;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class Servidor {

    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, HandlerClientes> directorio = new ConcurrentHashMap<>();
    private final Map<String, Queue<Mensaje>> colaMensajes = new ConcurrentHashMap<>();

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

    public void routeMensaje(Mensaje mensajito) {
        HandlerClientes receptor = directorio.get(mensajito.getReceptor());
        if (receptor != null) {
            if (receptor.isConectado()) {
                receptor.sendMessage(mensajito);
            } else {
                colaMensajes.computeIfAbsent(mensajito.getReceptor(), k -> new LinkedList<>()).add(mensajito);
                System.out.println("Stored message for " + mensajito.getReceptor());
            }
        } else {
            System.out.println("Usuario " + mensajito.getReceptor() + " no encontrado.");
        }
    }

    public Queue<Mensaje> getMensajesOffline(String username) {
        return colaMensajes.remove(username);
    }

}