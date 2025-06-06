package persistencia;

import modelo.UsuarioLogueado;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class SaverXML implements Saver {

    public void persistir(UsuarioLogueado usuario){
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

}
