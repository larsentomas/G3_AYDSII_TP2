package comandos;

import common.Respuesta;
import excepciones.ContactoRepetidoException;
import sistema.Sistema;

public class ComandoNuevaConversacion implements Comando{

    @Override
    public void ejecutar(Respuesta respuesta, Sistema sistema) {
        String usuarioConversacion = (String) respuesta.getDatos().get("usuarioConversacion");
        if (!sistema.getUsuarioLogueado().getContactos().containsKey(usuarioConversacion)) {
            try {
                sistema.getUsuarioLogueado().agregarContacto(usuarioConversacion, usuarioConversacion);
            } catch (ContactoRepetidoException e) {
                throw new RuntimeException(e);
            }
        }
        sistema.getUsuarioLogueado().crearConversacion(usuarioConversacion);
        sistema.getControlador().actualizarListaConversaciones();
    }
}
