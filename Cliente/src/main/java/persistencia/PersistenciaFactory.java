package persistencia;

import modelo.UsuarioLogueado;

public interface PersistenciaFactory {
    Saver crearSaver();
    Loader crearLoader();
}
