package excepciones;

public class ContactoRepetidoException extends Exception {
    private String contacto;

    public ContactoRepetidoException(String contacto) {
        super(contacto + " ya es un contacto");
        this.contacto = contacto;
    }

    public String getContacto() {
        return contacto;
    }
}
