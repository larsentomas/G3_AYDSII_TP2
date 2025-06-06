package servidor;

import common.Respuesta;
import common.Solicitud;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

public class Monitor implements Runnable {
    private Socket socketRecepcion;
    private Servidor servidor;
    private ObjectInputStream inputStream;

    public Monitor(Socket socketRecepcion, Servidor servidor) throws IOException {
        this.socketRecepcion = socketRecepcion;
        this.servidor = servidor;
        this.inputStream = new ObjectInputStream(socketRecepcion.getInputStream());
    }

    @Override
    public void run() {
        try {
            Object obj = inputStream.readObject();
            if (obj instanceof Solicitud request) {
                if (!request.getTipo().equals(Solicitud.PING)) {
                    System.out.println("Solicitud recibida: " + request.getTipo());
                }
                switch (request.getTipo()) {
                    case Solicitud.PING -> {
                        enviarRespuesta(InetAddress.getLocalHost().getHostAddress(), servidor.getPuertoSecundario(), Respuesta.ECHO, Map.of(), false, null);
                    }
                    case Solicitud.RESINCRONIZACION -> {
                        Map<String, Object> datos = Map.of("usuarios", servidor.getDirectorio(), "mensajesOffline", servidor.getMensajesOffline());
                        enviarRespuesta(InetAddress.getLocalHost().getHostAddress(), servidor.getPuertoSecundario(), Respuesta.RESINCRONIZACION, datos, false, null);
                    }
                    default -> enviarRespuesta(InetAddress.getLocalHost().getHostAddress(), servidor.getPuertoSecundario(), "UNKNOWN_REQUEST", Map.of("solicitud", request), true, "No se reconoce la solicitud");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection lost with client.");
        }
    }

    public void enviarRespuesta(String ip, int puerto, String tipo, Map<String, Object> datos, boolean error, String mensaje) {
        try (Socket socketEnvio = new Socket(ip, puerto)) {
            Respuesta response = new Respuesta(tipo, datos, error, mensaje);
            ObjectOutputStream outputStream = new ObjectOutputStream(socketEnvio.getOutputStream());
            outputStream.writeObject(response);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Failed to send " + tipo + " to " + ip + ":" + puerto);
        }
    }

}
