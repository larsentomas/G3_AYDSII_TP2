package persistencia;

import modelo.UsuarioLogueado;

public interface TipoPersistencia {
    void persistir(UsuarioLogueado usuario);
    void cargar(UsuarioLogueado usuario);
}
