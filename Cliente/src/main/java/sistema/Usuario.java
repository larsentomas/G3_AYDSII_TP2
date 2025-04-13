package sistema;

import java.io.Serializable;

public class Usuario implements Serializable {

    private String nombre;
    private String ip;
    private int puerto;

    public Usuario(String nombre, String ip, int puerto) {
        this.nombre = nombre;
        this.ip = ip;
        this.puerto = puerto;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

    @Override
    public String toString() {
        return nombre;
    }
}