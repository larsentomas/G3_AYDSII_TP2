package common;


import java.util.*;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class Solicitud implements Serializable {
    private static final long serialVersionUID = 1L;

    static AtomicInteger nextId = new AtomicInteger();

    public final static String NUEVA_CONVERSACION = "NUEVA_CONVERSACION";
    public final static String LOGIN = "LOGIN";
    public final static String DIRECTORIO = "DIRECTORIO";
    public final static String LOGOUT = "LOGOUT";
    public final static String ENVIAR_MENSAJE = "ENVIAR_MENSAJE";
    public final static String PING = "PING";
    public final static String RESINCRONIZACION = "RESINCRONIZACION";

    private int id;
    private final String tipo;
    private final Map<String, Object> datos;

    // Para echos
    public Solicitud(String tipo) {
        id = nextId.incrementAndGet();
        this.tipo = tipo;
        this.datos = new HashMap<>();
    }

    public Solicitud(String tipo, String usuario) {
        id = nextId.incrementAndGet();
        this.tipo = tipo;
        this.datos = Map.of("usuario", usuario);
    }

    public Solicitud(String tipo, Map<String, Object> datos) {
        id = nextId.incrementAndGet();
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

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Solicitud tipo " + tipo + " con datos: " + datos;
    }
}
