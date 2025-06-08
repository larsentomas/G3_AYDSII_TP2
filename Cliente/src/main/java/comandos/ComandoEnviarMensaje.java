package comandos;

import common.Conversacion;
import common.Mensaje;
import common.Respuesta;
import encriptacion.CifradoCaesarClave;
import encriptacion.EncriptarAES;
import sistema.Sistema;

public class ComandoEnviarMensaje implements Comando{

    @Override
    public void ejecutar(Respuesta respuesta, Sistema sistema) {
        if (respuesta.getError()) {
            // Si el mensaje no se pudo enviar, se le muestra un mensaje de error
            sistema.getControlador().mostrarModalError("El mensaje no se pudo enviar.");
        } else {
            // Si el mensaje se envio correctamente, se le muestra un mensaje de exito
            Mensaje mensaje = (Mensaje) respuesta.getDatos().get("mensaje");
            String receptor = (String) respuesta.getDatos().get("receptor");
            Conversacion c = sistema.getUsuarioLogueado().getConversacionCon(receptor);

            switch (sistema.getTipoCifrado()) {
                case "0":
                    sistema.getContexto().setEstrategia(new CifradoCaesarClave());
                    break;
                case "1":
                    sistema.getContexto().setEstrategia(new EncriptarAES());
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de cifrado inválido: " + sistema.getTipoCifrado());
            }
            mensaje = sistema.getContexto().desencriptar(mensaje, sistema.getClave_encriptacion()); // ACA DESENCRIPTA
            sistema.getUsuarioLogueado().agregarMensajeaConversacion(mensaje, c);
            sistema.getControlador().actualizarPanelChat(c);
        }
    }
}
