package modelo;

import sistema.Sistema;

import java.util.*;
import java.io.Serializable;

public class Solicitud implements Serializable {
    public final static String LOGIN = "LOGIN";
    public final static String DIRECTORIO = "DIRECTORIO";
    public final static String LOGOUT = "LOGOUT";
    public final static String ENVIAR_MENSAJE = "ENVIAR_MENSAJE";


    private final String tipo;
    private final Map<String, Object> datos;


    public Solicitud(String tipo) {
        this.tipo = tipo;
        this.datos = Map.of("usuario", Sistema.getInstance().getUsuarioLogueado().getNombre());
    }

    public Solicitud(String tipo, Map<String, Object> datos) {
        this.tipo = tipo;
        this.datos = datos;
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

    @Override
    public String toString() {
        return "Solicitud tipo " + tipo + " con datos: " + datos;
    }
}
