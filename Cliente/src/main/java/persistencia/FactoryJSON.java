package persistencia;

import modelo.UsuarioLogueado;

public class FactoryJSON implements PersistenciaFactory{

    @Override
    public Saver crearSaver() {
        return new SaverJSON();
    }

    @Override
    public Loader crearLoader() {
        return new LoaderJSON();
    }
}
