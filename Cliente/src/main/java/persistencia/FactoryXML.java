package persistencia;

import modelo.UsuarioLogueado;

public class FactoryXML implements PersistenciaFactory{
    @Override
    public Saver crearSaver() {
        return new SaverXML();
    }

    @Override
    public Loader crearLoader() {
        return new LoaderXML();
    }
}
