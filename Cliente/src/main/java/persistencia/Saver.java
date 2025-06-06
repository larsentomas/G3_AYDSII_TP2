package persistencia;

import modelo.UsuarioLogueado;

public interface Saver {
    public void persistir(UsuarioLogueado usuario);
}
