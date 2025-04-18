package modelo;

import java.sql.Timestamp;

public class Mensaje {
    private String contenido;
    private String emisor;
    private Timestamp timestampCreado;

    public Mensaje(String contenido, String emisor) {
        this.contenido = contenido;
        this.emisor = emisor;
        this.timestampCreado = new Timestamp(System.currentTimeMillis());
    }

    public String getContenido() {
        return contenido;
    }

    public String getEmisor() {
        return emisor;
    }

    public Timestamp getTimestampCreado() {
        return timestampCreado;
    }

    @Override
    public String toString() {
        return "[" + this.timestampCreado.getTime() + "] " + this.emisor + ":" + this.contenido;
    }

}
