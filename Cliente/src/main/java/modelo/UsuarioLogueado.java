package modelo;

import excepciones.ContactoRepetidoException;

import java.net.Socket;
import java.util.ArrayList;

public class UsuarioLogueado extends Usuario {

    private Socket socket;
    private ArrayList<String> contactos;
    private ArrayList<Conversacion> conversaciones;

    public UsuarioLogueado(String nickname, String ip, int puerto, Socket s) {
        super(nickname, ip, puerto);
        this.contactos = new ArrayList<>();
        this.conversaciones = new ArrayList<>();
        this.socket = s;
    }

    // Getters y Setters y agregar

    public void agregarContacto(String contacto) throws ContactoRepetidoException {
        if (!contactos.contains(contacto)) {
            contactos.add(contacto);
        }
        else throw new ContactoRepetidoException("El contacto ya existe", contacto);
    }

    public void eliminarContacto(String contacto) {
        contactos.remove(contacto);
    }

    public ArrayList<String> getContactos() {
        return contactos;
    }

    public void agregarConversacion(Conversacion conversacion) {
        this.conversaciones.add(conversacion);
    }


}
