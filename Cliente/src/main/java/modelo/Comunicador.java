package modelo;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class Comunicador implements Runnable {
    private Usuario usuario;
    private Mensaje mensaje;
    private boolean estado;
    private ObjectOutputStream out;

    // Constructor para mandar un mensaje
    public Comunicador(Usuario u, Mensaje mensaje, String nickname) {
        this.mensaje = mensaje;
        this.usuario = u;
        this.estado = true;
    }

    // Constructor para informar nuevo usuario o usuario desconectado
    public Comunicador(Usuario u, boolean estado) {
        this.usuario = u;
        this.estado = estado;
    }

    @Override
    public void run() {
        // TODO: Cambiar dependiendo de como se implemente el servidor y la conexion

        // Enviar mensaje
        if (mensaje != null) {
            try {
                out.writeObject(mensaje);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (estado) { // Nuevo usuario
            try {
                out.writeObject("NUEVO_USUARIO");
                out.writeObject(usuario);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else { // Usuario desconectado
            try {
                out.writeObject("USUARIO_DESCONECTADO");
                out.writeObject(usuario);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
