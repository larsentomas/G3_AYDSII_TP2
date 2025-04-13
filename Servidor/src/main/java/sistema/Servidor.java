package sistema;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                System.out.println(inputStream.readObject());

                HandlerClientes handler = new HandlerClientes(socket, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
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

    public boolean logearCliente(String username, HandlerClientes handler) {
        HandlerClientes existente = directorio.get(username);

        if (existente == null) {
            directorio.put(username, handler);
            return true;
        }

        if (existente == handler) {
            return true;
        }

        if (!existente.isConectado()) {
            try {
                existente.getSocket().close();
            } catch (IOException ignored) {}

            directorio.put(username, handler);
            return true;
        }

        return false;
    }

    public void eliminarCliente(String username){
        directorio.remove(username);
    }

    public Set<String> getDatosDirectorio() {
        return directorio.keySet();
    }

    public HandlerClientes getClientHandler(String username) {
        return directorio.get(username);
    }
}