package comandos;

import common.Respuesta;
import sistema.Sistema;

public class ComandoMensajesOffline implements Comando{
    @Override
    public void ejecutar(Respuesta respuesta, Sistema sistema) {
        sistema.recibirMensajesOffline(respuesta);
    }
}
