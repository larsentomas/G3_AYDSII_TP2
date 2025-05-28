package encriptacion;

import common.Mensaje;

public class ContextoEncriptacion {
    private Strategy estrategia;

    public void setEstrategia(Strategy estrategia) {
        this.estrategia = estrategia;
    }

    public Mensaje encriptar(Mensaje mensaje, String clave) {
        return estrategia.encriptar(mensaje, clave);
    }

    public Mensaje desencriptar(Mensaje mensaje, String clave) {
        return estrategia.desencriptar(mensaje, clave);
    }
}

