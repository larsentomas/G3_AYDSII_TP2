package sistema;

import java.util.Map;

public class Respuesta {
    private static String LOGIN = "LOGIN";
    private static String DIRECTORIO = "DIRECTORIO";

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
