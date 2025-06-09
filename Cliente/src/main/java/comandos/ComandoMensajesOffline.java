package comandos;

import common.Respuesta;
import sistema.Sistema;

public class ComandoMensajesOffline implements Comando{
    @Override
    public void ejecutar(Respuesta respuesta, Sistema sistema) {
        try {
            Thread.sleep(500); // pause for 500 milliseconds (half a second)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore interrupt status
        }

        sistema.recibirMensajesOffline(respuesta);
    }
}
