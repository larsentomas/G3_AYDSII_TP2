package comandos;

import common.Respuesta;
import sistema.Sistema;

public class ComandoDirectorio implements Comando{
    @Override
    public void ejecutar(Respuesta respuesta, Sistema sistema) {
        sistema.recibirListaUsuarios(respuesta);
    }
}
