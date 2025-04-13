package sistema;

import java.util.Map;

public class Solicitud {
    private final String tipo;
    private final Map<String, Object> datos;


    public Solicitud(String tipo, Map<String, Object> datos) {
        this.tipo = tipo;
        this.datos = datos;
    }

    public String getTipo() {
        return tipo;
    }

    public Map<String, Object> getDatos() {
        return datos;
    }
}
