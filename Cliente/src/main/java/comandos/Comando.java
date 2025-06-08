package comandos;

import common.Respuesta;
import sistema.Sistema;

public interface Comando {
    void ejecutar(Respuesta respuesta, Sistema sistema);
}
