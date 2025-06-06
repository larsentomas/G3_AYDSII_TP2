package persistencia;

import common.Conversacion;
import common.Mensaje;
import modelo.UsuarioLogueado;

import java.beans.XMLDecoder;
import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class LoaderXML implements Loader {

    public void cargar(UsuarioLogueado usuario) {
        // Implementación de la carga de datos desde XML
        System.out.println("Cargando datos desde formato XML.");

        String fil_name = usuario.getNombre() + ".xml";

        XMLDecoder decoder = null;
        try {
            decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(fil_name)));
            UsuarioLogueado usuarioCargado = (UsuarioLogueado) decoder.readObject();
            decoder.close();

            usuario.setContactos(usuarioCargado.getContactos());
            agregarConversaciones(usuario, usuarioCargado.getConversaciones());
        } catch (FileNotFoundException e) {
            System.out.println("Archivo " + fil_name + " no encontrado.");
        }
    }

    private void agregarConversaciones(UsuarioLogueado usuario, CopyOnWriteArrayList<Conversacion> conversacionesPersistencia) {
        for (Conversacion conversacion : conversacionesPersistencia) {
            if (usuario.getConversacionCon(conversacion.getIntegrante()) == null) {
                usuario.getConversaciones().add(conversacion);
            } else {
                // tengo que agregar los mensajes
                Conversacion conversacionExistente = usuario.getConversacionCon(conversacion.getIntegrante());
                for (Mensaje mensaje : conversacionExistente.getMensajes()) {
                    conversacion.ponerMensaje(mensaje);
                }
                usuario.setConversaciones(conversacionesPersistencia);
            }
        }
        System.out.println("Conversaciones cargadas correctamente.");
    }
}
