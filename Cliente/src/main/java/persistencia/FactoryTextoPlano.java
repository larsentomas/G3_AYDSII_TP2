package persistencia;

import modelo.UsuarioLogueado;

public class FactoryTextoPlano implements PersistenciaFactory{
    @Override
    public Saver crearSaver() {
        return new SaverTextoPlano();
    }

    @Override
    public Loader crearLoader() {
        return new LoaderTextoPlano();
    }
}
