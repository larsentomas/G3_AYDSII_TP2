package persistencia;

import com.fasterxml.jackson.databind.ObjectMapper;
import modelo.UsuarioLogueado;

import java.io.File;
import java.io.IOException;

public class SaverJSON implements Saver {

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

}
