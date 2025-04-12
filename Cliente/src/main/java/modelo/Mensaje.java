package modelo;

import java.sql.Timestamp;

public class Mensaje {
    private String contenido;
    private String remitente;
    private Timestamp timestampCreado;

    public Mensaje(String contenido, String remitente) {
        this.contenido = contenido;
        this.remitente = remitente;
        this.timestampCreado = new Timestamp(System.currentTimeMillis());
    }

    public String getContenido() {
        return contenido;
    }

    public String getRemitente() {
        return remitente;
    }

    public Timestamp getTimestampCreado() {
        return timestampCreado;
    }

    @Override
    public String toString() {
        return "[" + this.timestampCreado.getTime() + "] " + this.remitente + ":" + this.contenido;
    }

}
