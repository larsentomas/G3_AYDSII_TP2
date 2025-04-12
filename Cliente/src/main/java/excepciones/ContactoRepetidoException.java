package excepciones;

public class ContactoRepetidoException extends Exception {
    private String contacto;

    public ContactoRepetidoException(String mensaje, String contacto) {
        super(mensaje);
        this.contacto = contacto;
    }

    public String getContacto() {
        return contacto;
    }
}
