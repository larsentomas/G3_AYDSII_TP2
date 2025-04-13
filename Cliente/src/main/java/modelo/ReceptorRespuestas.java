package modelo;

import sistema.Sistema;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ReceptorRespuestas implements Runnable {

    private Socket socket;

    public ReceptorRespuestas(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        Sistema s = Sistema.getInstance();
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            try {
                Object obj = in.readObject();
                System.out.println("Objeto recibido: " + obj.getClass().getName());
                s.recibirObj(obj);
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("End of stream reached: " + e.getMessage());
        }
    }

}
