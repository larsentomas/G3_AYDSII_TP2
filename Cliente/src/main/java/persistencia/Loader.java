package persistencia;

import common.Usuario;
import modelo.UsuarioLogueado;

public interface Loader {
    public void cargar(UsuarioLogueado usuario);
}
