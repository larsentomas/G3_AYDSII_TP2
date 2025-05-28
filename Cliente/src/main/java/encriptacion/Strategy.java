package encriptacion;

import common.Mensaje;
import modelo.UsuarioLogueado;

public interface Strategy {
    Mensaje encriptar(Mensaje mensaje, String claveEncriptacion);
    Mensaje desencriptar(Mensaje mensaje, String claveEncriptacion);
}
