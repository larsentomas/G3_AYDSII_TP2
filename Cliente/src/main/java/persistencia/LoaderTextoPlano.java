package persistencia;

import modelo.UsuarioLogueado;
import common.Conversacion;
import common.Mensaje;
import excepciones.ContactoRepetidoException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

public class LoaderTextoPlano implements Loader {


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

        } catch (FileNotFoundException e) {
            System.out.println("Archivo no encontrado: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
