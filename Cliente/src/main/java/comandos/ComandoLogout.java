package comandos;

import common.Respuesta;
import sistema.Sistema;

public class ComandoLogout implements Comando {
    @Override
    public void ejecutar(Respuesta respuesta, Sistema sistema) {
        sistema.cerrarSesion();
    }
}
