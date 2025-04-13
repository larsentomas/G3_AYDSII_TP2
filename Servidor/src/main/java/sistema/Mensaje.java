package sistema;

import java.util.Date;

public class Mensaje {
    private String emisor;
    private String receptor;
    private String contenido;
    private Date timestamp;

    public Mensaje(String emisor, String receptor, String contenido) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenido = contenido;
        this.timestamp = new Date();
    }

    public String getEmisor() {
        return emisor;
    }

    public String getReceptor() {
        return receptor;
    }

    public String getContenido() {
        return contenido;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
