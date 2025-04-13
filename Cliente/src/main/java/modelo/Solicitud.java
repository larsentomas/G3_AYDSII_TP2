package modelo;

import sistema.Sistema;

import java.sql.Timestamp;
import java.util.*;

public class Solicitud {
    public final static String LOGIN = "LOGIN";
    public final static String DIRECTORIO = "DIRECTORIO";
    public final static String LOGOUT = "LOGOUT";
    public final static String ENVIAR_MENSAJE = "ENVIAR_MENSAJE";


    private final String tipo;
    private final Map<String, Object> datos;


    public Solicitud(String tipo, String usuario) {
        this.tipo = tipo;

        if (tipo.equalsIgnoreCase(LOGIN)) {
            this.datos = Map.of("usuario", usuario);
        } else {
            this.datos = Map.of();
        }
    }

    public Solicitud(String tipo) {
        this.tipo = tipo;
        this.datos = Map.of();
    }

    // Para enviar mensaje
    public Solicitud(Mensaje mensaje, Conversacion c) {
        this.tipo = ENVIAR_MENSAJE;
        this.datos = new HashMap<>();

        datos.put("mensaje", mensaje);
        datos.put("conversacion", c);
    }

    public String getTipo() {
        return tipo;
    }

    public Map<String, Object> getDatos() {
        return datos;
    }
}
