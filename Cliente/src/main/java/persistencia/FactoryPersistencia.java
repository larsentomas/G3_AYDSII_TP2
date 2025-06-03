package persistencia;

public class FactoryPersistencia {

    public static TipoPersistencia crearPersistencia(int tipo) {
        return switch (tipo) {
            case 1 -> new PersistenciaJSON();
            case 2 -> new PersistenciaXML();
            case 3 -> new PersistenciaTextoPlano(); // Si tenés esta clase
            default -> new PersistenciaJSON(); // fallback
        };
    }
}
