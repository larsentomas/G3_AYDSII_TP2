package common;

import java.io.Serializable;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
