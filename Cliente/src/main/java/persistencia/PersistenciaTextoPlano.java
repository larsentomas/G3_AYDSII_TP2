package persistencia;

import modelo.UsuarioLogueado;
import common.Conversacion;
import common.Mensaje;
import excepciones.ContactoRepetidoException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.List;

public class PersistenciaTextoPlano implements TipoPersistencia {

    @Override
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

    @Override
    public void cargar(UsuarioLogueado usuario) {
        String filename = usuario.getNombre() + ".txt";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename), StandardCharsets.UTF_8))) {

            String linea;
            String seccion = "";

            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith("@")) {
                    seccion = linea;
                    continue;
                }

                switch (seccion) {
                    case "@USUARIO":
                        String[] datos = linea.split(",", 3);
                        usuario.setNombre(datos[0]);
                        usuario.setIp(datos[1]);
                        usuario.setPuerto(Integer.parseInt(datos[2]));
                        break;

                    case "@CONTACTOS":
                        String[] contacto = linea.split(",", 2);
                        try {
                            usuario.agregarContacto(contacto[0], contacto[1]);
                        } catch (ContactoRepetidoException ignored) {}
                        break;

                    case "@CONVERSACIONES":
                        String[] partes = linea.split("\\|", 4);
                        String contactoNombre = partes[0];
                        String emisor = partes[1];
                        String contenido = partes[2].replace("\\n", "\n");
                        Timestamp timestamp = new Timestamp(Long.parseLong(partes[3]));

                        Conversacion conv = usuario.getConversacionCon(contactoNombre);
                        if (conv == null) {
                            conv = usuario.crearConversacion(contactoNombre);
                        }

                        Mensaje mensaje = new Mensaje();
                        mensaje.setEmisor(emisor);
                        mensaje.setContenido(contenido);
                        mensaje.setTimestampCreado(timestamp);
                        usuario.agregarMensajeaConversacion(mensaje, conv);
                        break;
                }
            }

            System.out.println("Carga completada desde " + filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
