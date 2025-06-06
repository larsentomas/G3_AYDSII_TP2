package persistencia;

import common.Conversacion;
import common.Mensaje;
import modelo.UsuarioLogueado;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class SaverTextoPlano implements Saver {

    public void persistir(UsuarioLogueado usuario) {
        System.out.println("Persistiendo datos en formato Texto plano.");
        String filename = usuario.getNombre() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), StandardCharsets.UTF_8))) {

            // Datos del usuario
            writer.write("@USUARIO\n");
            writer.write(usuario.getNombre() + "," + usuario.getIp() + "," + usuario.getPuerto() + "\n");

            // Contactos
            writer.write("@CONTACTOS\n");
            for (String username : usuario.getContactos().keySet()) {
                String apodo = usuario.getContactos().get(username);
                writer.write(username + "," + apodo + "\n");
            }

            // Conversaciones
            writer.write("@CONVERSACIONES\n");
            for (Conversacion conv : usuario.getConversaciones()) {
                for (Mensaje mensaje : conv.getMensajes()) {
                    // Formato: contacto|emisor|contenido|timestamp
                    writer.write(conv.getIntegrante() + "|" +
                            mensaje.getEmisor() + "|" +
                            mensaje.getContenido().replace("\n", "\\n") + "|" +
                            mensaje.getTimestampCreado().getTime() + "\n");
                }
            }

            System.out.println("Persistencia completada en " + filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
