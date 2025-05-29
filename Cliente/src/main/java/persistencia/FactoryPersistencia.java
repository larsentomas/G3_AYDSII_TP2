package persistencia;

public class FactoryPersistencia {

    public static TipoPersistencia crearPersistencia(int tipo) {
        return switch (tipo) {
            case 0 -> new PersistenciaJSON();
            case 1 -> new PersistenciaXML();
            case 2 -> new PersistenciaTextoPlano(); // Si tenés esta clase
            default -> new PersistenciaJSON(); // fallback
        };
    }
}
