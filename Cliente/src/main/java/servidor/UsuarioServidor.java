package servidor;

import modelo.Usuario;

import java.io.IOException;
import java.net.Socket;

public class UsuarioServidor extends Usuario {
    private boolean conectado;

    public UsuarioServidor(String nombre, String ip, int puerto) {
        super(nombre, ip, puerto);
        this.conectado = true;
    }

    public boolean isConectado() {
        return conectado;
    }

    public void setConectado(boolean conectado) {
        this.conectado = conectado;
    }
}
