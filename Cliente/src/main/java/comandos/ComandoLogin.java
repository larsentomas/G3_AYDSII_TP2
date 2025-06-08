package comandos;

import common.Respuesta;
import controlador.Controlador;
import controlador.ControladorLogin;
import persistencia.FactoryJSON;
import persistencia.FactoryTextoPlano;
import persistencia.FactoryXML;
import persistencia.PersistenciaFactory;
import sistema.Sistema;

public class ComandoLogin implements Comando {
    public void ejecutar(Respuesta respuesta, Sistema sistema) {
        if (respuesta.getError()) {
            // Si el usuario no es valido, se le muestra un mensaje de error
            sistema.setUsuarioLogueado(null);
            Sistema.getVistaLogin().mostrarModalError("El usuario ya existe.");
        } else {

            // PERSISTENCIA
            PersistenciaFactory p;
            switch (sistema.getTipoPersistencia()) {
                case(0):
                    p = new FactoryJSON();
                    break;
                case(1):
                    p = new FactoryXML();
                case(2):
                    p = new FactoryTextoPlano();
                default:
                    p = new FactoryJSON();
            }
            p.crearLoader().cargar(sistema.getUsuarioLogueado());

            sistema.getControladorLogin().setVisible(false);
            sistema.getControlador().setVisible(true);
            sistema.getControlador().setBienvenida(sistema.getUsuarioLogueado().getNombre());
            sistema.getControlador().actualizarListaConversaciones();
        }
    }
}
