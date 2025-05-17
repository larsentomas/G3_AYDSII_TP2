package modelo;

import common.Usuario;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HandlerMensajes implements Runnable {

    private Usuario usuario;

    public HandlerMensajes(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(usuario.getPuerto())) {
            System.out.println("Escuchando en el puerto " + usuario.getPuerto() + "...");
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ReceptorRespuestas(socket)).start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
