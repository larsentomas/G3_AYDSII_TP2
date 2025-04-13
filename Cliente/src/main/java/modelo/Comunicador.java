package modelo;

import sistema.Sistema;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Comunicador implements Runnable {
    private Solicitud solicitud;
    private Socket s;
    private Sistema sistema = Sistema.getInstance();

    public Comunicador(Solicitud solicitud, Socket s) {
        this.solicitud = solicitud;
        this.s = s;
    }

    @Override
    public void run() {
        try {
            System.out.println("Conectando al servidor...");
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(solicitud);
            System.out.println("Solicitud enviada: " + solicitud);
            out.flush();

        } catch (IOException e) {
            System.err.println("Error al enviar el mensaje: " + e.getMessage());
        }
    }
}
