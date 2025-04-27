package common;

import java.io.Serializable;
import java.sql.Timestamp;

public class Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public void setEmisor(String emisor) {
        this.emisor = emisor;
    }

    public void setTimestampCreado(Timestamp timestampCreado) {
        this.timestampCreado = timestampCreado;
    }

    @Override
    public String toString() {
        return this.contenido;
    }

}
