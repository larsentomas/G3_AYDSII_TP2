package persistencia;

import common.Conversacion;
import common.Mensaje;
import modelo.UsuarioLogueado;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class PersistenciaJSON implements TipoPersistencia {

    @Override
    public void persistir(UsuarioLogueado usuario) {
        // Implementacion de persistencia con JSON
        System.out.println("Persistiendo datos en formato JSON.");

        String fileName = usuario.getNombre() + ".json";
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(fileName), usuario);
            System.out.println("Datos persistidos correctamente en " + fileName);
        } catch (IOException e) {
            System.err.println("Error al persistir datos: " + e.getMessage());
        }

    }

    @Override
    public void cargar(UsuarioLogueado usuario) {
        // Implementación de la carga de datos desde JSON
        System.out.println("Cargando datos desde formato JSON.");

        String fileName = usuario.getNombre() + ".json";
        File file = new File(fileName);
        ObjectMapper mapper = new ObjectMapper();

        if (file.exists()) {
            try {
                UsuarioLogueado loaded = mapper.readValue(new File(fileName), UsuarioLogueado.class);
                usuario.setContactos(loaded.getContactos());
                agregarConversaciones(usuario, loaded.getConversaciones());
                System.out.println("Datos cargados correctamente desde " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error al cargar datos: " + e.getMessage());
            }
        } else {
            System.out.println("No se encontró el archivo " + fileName);
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
