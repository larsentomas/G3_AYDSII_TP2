package modelo;

import sistema.Sistema;

import common.Solicitud;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Comunicador implements Runnable {
    private Solicitud solicitud;
    private int puerto;
    private String ip;

    public Comunicador(Solicitud solicitud, int puerto, String ip) throws IOException {
        this.solicitud = solicitud;
        this.puerto = puerto;
        this.ip = ip;
    }

    @Override
    public void run() {
        try (Socket s = new Socket(ip, puerto)) {
            System.out.println("Conectando al servidor...");
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(solicitud);
            out.flush();
        } catch (IOException e) {
            if (e.getMessage().contains("Connection refused")) {
                Sistema.getInstance().getControladorLogin().mostrarModalError("Error: Conexión rechazada.");
            }
            System.err.println("Error al enviar el mensaje: " + e.getMessage());
        }
    }
}
