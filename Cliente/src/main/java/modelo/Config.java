package modelo;
import java.io.InputStream;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;

public class Config {

    private static Properties props = new Properties();

    static {
        try {
            System.out.println("Current working directory: " + System.getProperty("user.dir"));
            InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties");
            props.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("No se pudo leer el archivo de configuración.");
        }
    }

    public static String get(String clave) {
        return props.getProperty(clave);
    }
    public static int getInt(String clave) {
        return Integer.parseInt(get(clave));
    }
    public static boolean getBoolean(String clave) {
        return Boolean.parseBoolean(get(clave));
    }
}