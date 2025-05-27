package persistencia;

import modelo.UsuarioLogueado;

public class Persistencia {
    private TipoPersistencia tipoPersistencia;

    public Persistencia(TipoPersistencia tipoPersistencia) {
        this.tipoPersistencia = tipoPersistencia;
    }

    public void persistir(UsuarioLogueado usuario) {
        tipoPersistencia.persistir(usuario);
    }

    public void cargar(UsuarioLogueado usuario) {
        tipoPersistencia.cargar(usuario);
    }
}
