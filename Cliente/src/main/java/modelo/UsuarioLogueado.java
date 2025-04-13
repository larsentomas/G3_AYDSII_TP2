package modelo;

import excepciones.ContactoRepetidoException;
import excepciones.UsuarioExistenteException;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class UsuarioLogueado extends Usuario {

    private Socket socket;
    private HashMap<String, String> contactos;
    private ArrayList<Conversacion> conversaciones; // nombre : nickname nuestro

    public UsuarioLogueado(Usuario u, Socket s) {
        super(u.getNombre(), u.getIp(), u.getPuerto());
        this.socket = s;
    }

    public UsuarioLogueado(String nickname, String ip, int puerto, Socket s) {
        super(nickname, ip, puerto);
        this.contactos = new HashMap<>();
        this.conversaciones = new ArrayList<>();
        this.socket = s;
    }

    public Conversacion crearConversacion(Usuario usuario) {
        Conversacion conversacion = new Conversacion(usuario.getNombre());
        this.agregarConversacion(conversacion);
        conversacion.setNotificado(true);
        return conversacion;
    }

    public ArrayList<String> getContactosSinConversacion() {
        ArrayList<String> contactosSinConversacion = new ArrayList<>();
        for (String contacto : contactos.keySet()) {
            boolean tieneConversacion = false;
            for (Conversacion conversacion : conversaciones) {
                if (conversacion.getIntegrante().equals(contactos.get(contacto))) {
                    tieneConversacion = true;
                    break;
                }
            }
            if (!tieneConversacion) {
                contactosSinConversacion.add(contacto);
            }
        }
        return contactosSinConversacion;
    }

    public Conversacion getConversacionCon(String contacto) {
        for (Conversacion conversacion : conversaciones) {
            if (conversacion.getIntegrante().equals(contacto)) {
                return conversacion;
            }
        }
        return null;
    }

    // Getters y Setters y agregar

    public void agregarContacto(String username, String nickname) throws ContactoRepetidoException {
        if (!contactos.containsKey(username)) {
            contactos.put(username, nickname);
        } else throw new ContactoRepetidoException(username);
    }

    public void eliminarContacto(String contacto) {
        contactos.remove(contacto);
    }

    public void agregarMensajeaConversacion(Mensaje mensaje, Conversacion conversacion) {
        if (conversacion != null) {
            conversacion.agregarMensaje(mensaje);
        }
    }

    public HashMap<String, String> getContactos() {
        return contactos;
    }

    public void agregarConversacion(Conversacion conversacion) {
        this.conversaciones.add(conversacion);
    }

    public Socket getSocket() {
        return socket;
    }

}
