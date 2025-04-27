package excepciones;

public class UsuarioExistenteException extends Exception {
    private String usuario;

    public UsuarioExistenteException(String usuario) {
        super("El usuario ya existe");
        this.usuario = usuario;
    }

    public String getUsuario() {
        return usuario;
    }
}
