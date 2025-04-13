package modelo;

import java.util.Map;
import java.io.Serializable;

public class Respuesta implements Serializable {

    public final static String LOGIN = "LOGIN";
    public final static String LOGOUT = "LOGOUT";
    public final static String DIRECTORIO = "DIRECTORIO";
    public final static String ENVIAR_MENSAJE = "ENVIAR_MENSAJE";
    public final static String MENSAJE_RECIBIDO = "MENSAJE_RECIBIDO";

    private final String tipo;
    private final Map<String, Object> datos;
    private final boolean error;
    private final String errorMensaje;

    public Respuesta(String tipo, Map<String, Object> datos, boolean error, String errorMensaje) {
        this.tipo = tipo;
        this.datos = datos;
        this.error = error;
        this.errorMensaje = errorMensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public Map<String, Object> getDatos() {
        return datos;
    }

    public boolean getError() {
        return error;
    }

    public String getErrorMensaje() {
        return errorMensaje;
    }


}
