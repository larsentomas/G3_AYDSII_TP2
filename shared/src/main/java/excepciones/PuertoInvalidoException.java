package excepciones;

public class PuertoInvalidoException extends Exception {
    private String puerto;

    public PuertoInvalidoException(String puerto) {
        super("El puerto " + puerto + " no es válido.");
        this.puerto = puerto;
    }

    public String getPuerto() {
        return puerto;
    }
}
