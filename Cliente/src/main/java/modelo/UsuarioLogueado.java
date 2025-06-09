package modelo;


import excepciones.ContactoRepetidoException;

import common.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class UsuarioLogueado extends Usuario {

    private HashMap<String, String> contactos; // usuario, apodo
    private CopyOnWriteArrayList<Conversacion> conversaciones; // nombre : nickname nuestro
    // Campo persistente auxiliar
    private ArrayList<Conversacion> conversacionesXML = new ArrayList<>();

    public UsuarioLogueado() {
    }

    public UsuarioLogueado(String nickname, String ip, int puerto) {
        super(nickname, ip, puerto);
        this.contactos = new HashMap<>();
        this.conversaciones = new CopyOnWriteArrayList<>();
    }

    public UsuarioLogueado(Usuario u) {
        super(u.getNombre(), u.getIp(), u.getPuerto());
        this.contactos = new HashMap<>();
        this.conversaciones = new CopyOnWriteArrayList<>();
    }

    public Conversacion crearConversacion(String usuario) {
        Conversacion conversacion = new Conversacion(usuario);
        this.agregarConversacion(conversacion);
        conversacion.setNotificado(true);
        return conversacion;
    }

    public String getContacto(String username) {
        for (String u : contactos.keySet()) {
            if (contactos.get(u).equals(username)) {
                return u;
            }
        }
        return null;
    }

    public ArrayList<String> getContactosSinConversacion() {
        ArrayList<String> contactosSinConversacion = new ArrayList<>();
        for (String contacto : contactos.keySet()) {
            boolean tieneConversacion = false;
            for (Conversacion conversacion : conversaciones) {
                if (conversacion.getIntegrante().equals(contacto)) {
                    tieneConversacion = true;
                    break;
                }
            }
            if (!tieneConversacion) {
                contactosSinConversacion.add(getContactos().get(contacto));
            }
            System.out.print("Los contactos sin conversacion son " + contactosSinConversacion);
        }
        return contactosSinConversacion;
    }

    public Conversacion getConversacionCon(String contacto) {
        if (conversaciones != null) {
            for (Conversacion conversacion : conversaciones) {
                if (conversacion.getIntegrante().equals(contacto)) {
                    return conversacion;
                }
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
        conversacion.agregarMensaje(mensaje);
    }

    public HashMap<String, String> getContactos() {
        return contactos;
    }

    public String getApodo(String username) {
        return contactos.get(username);
    }

    public void agregarConversacion(Conversacion conversacion) {
        this.conversaciones.add(conversacion);
    }

    public CopyOnWriteArrayList<Conversacion> getConversaciones() {
        return conversaciones;
    }

    // Extras para persistencia

    public CopyOnWriteArrayList<Conversacion> getSerializableConversaciones() {
        return new CopyOnWriteArrayList<>(conversaciones);
    }

    public void setContactos(HashMap<String, String> contactos) {
        this.contactos = contactos;
    }

    public void setConversaciones(CopyOnWriteArrayList<Conversacion> conversaciones) {
        this.conversaciones = conversaciones;
    }

    // Getter/Setter para XMLEncoder
    public ArrayList<Conversacion> getConversacionesXML() {
        return new ArrayList<>(conversaciones);
    }
    public void setConversacionesXML(ArrayList<Conversacion> convs) {
        this.conversaciones = new CopyOnWriteArrayList<>(convs);
    }

}
