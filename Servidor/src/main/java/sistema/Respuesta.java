package sistema;

import java.util.Map;

public class Respuesta {
    private final String tipo;
    private final Map<String, Object> datos;
    private final int error;
    private final String errorMensaje;

    public Respuesta(String tipo, Map<String, Object> datos, int error, String errorMensaje) {
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

    public int getError() {
        return error;
    }

    public String getErrorMensaje() {
        return errorMensaje;
    }

    
}
