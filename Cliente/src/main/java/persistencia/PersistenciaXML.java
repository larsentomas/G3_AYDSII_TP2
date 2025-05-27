package persistencia;

import common.Conversacion;
import common.Mensaje;
import modelo.UsuarioLogueado;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class PersistenciaXML implements TipoPersistencia {

    @Override
    public void persistir(UsuarioLogueado usuario) {
        // Implementación de la persistencia en XML
        System.out.println("Persistiendo datos en formato XML.");

        String fil_name = usuario.getNombre() + ".xml";

        try (XMLEncoder xmlEncoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(fil_name)))) {
            xmlEncoder.writeObject(usuario);
            xmlEncoder.close();
            System.out.println("Datos persistidos correctamente en " + fil_name);
        } catch (FileNotFoundException e) {
            System.err.println("Error al crear el archivo XML: " + e.getMessage());
        }

    }

    @Override
    public void cargar(UsuarioLogueado usuario) {
        // Implementación de la carga de datos desde XML
        System.out.println("Cargando datos desde formato XML.");

        String fil_name = usuario.getNombre() + ".xml";

        XMLDecoder decoder = null;
        try {
            decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(fil_name)));
        } catch (FileNotFoundException e) {
        }

        assert decoder != null;
        UsuarioLogueado usuarioCargado = (UsuarioLogueado) decoder.readObject();
        decoder.close();

        usuario.setContactos(usuarioCargado.getContactos());
        agregarConversaciones(usuario, usuarioCargado.getConversaciones());
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
