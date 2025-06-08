package comandos;

import common.Conversacion;
import common.Mensaje;
import common.Respuesta;
import controlador.Controlador;
import excepciones.ContactoRepetidoException;
import modelo.UsuarioLogueado;
import sistema.Sistema;

public class ComandoMensajeRecibido implements Comando {
    @Override
    public void ejecutar(Respuesta respuesta, Sistema sistema) {
        Mensaje mensaje = (Mensaje) respuesta.getDatos().get("mensaje");
        String emisor = mensaje.getEmisor();

        UsuarioLogueado usuarioLogueado = sistema.getUsuarioLogueado();
        Controlador controlador = sistema.getControlador();

        Conversacion conversacion;
        if (!usuarioLogueado.getContactos().containsKey(emisor)) {
            try {
                usuarioLogueado.agregarContacto(emisor, emisor);
                conversacion = usuarioLogueado.crearConversacion(emisor);
            } catch (ContactoRepetidoException e) {
                throw new RuntimeException(e);
            }
        } else {
            conversacion = usuarioLogueado.getConversacionCon(emisor);
        }
        sistema.agregarMensajeConversacion(mensaje, conversacion);

        if (conversacion == controlador.getConversacionActiva()) {
            controlador.actualizarPanelChat(conversacion);
        } else {
            conversacion.setNotificado(true);
        }
        controlador.actualizarListaConversaciones();

    }
}
