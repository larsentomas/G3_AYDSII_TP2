package sistema;

import java.util.Map;

public class Solicitud {
    private final String tipo;
    private final Map<String, String> datos;


    public Solicitud(String tipo, Map<String, String> datos) {
        this.tipo = tipo;
        this.datos = datos;
    }

    public String getTipo() {
        return tipo;
    }

    public Map<String, String> getDatos() {
        return datos;
    }
}
