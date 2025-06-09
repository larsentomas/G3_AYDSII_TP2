package encriptacion;

import common.Mensaje;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class EncriptarAES implements Strategy {

    private static final String ALGORITMO = "AES";

    @Override
    public Mensaje encriptar(Mensaje mensaje, String claveEncriptacion) {
        try {
            SecretKeySpec key = generarClave(claveEncriptacion);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] textoEncriptado = cipher.doFinal(mensaje.getContenido().getBytes(StandardCharsets.UTF_8));
            String textoBase64 = Base64.getEncoder().encodeToString(textoEncriptado);

            return new Mensaje(textoBase64, mensaje.getEmisor());
        } catch (Exception e) {
            System.err.println("Error al encriptar con AES: " + e.getMessage());
            e.printStackTrace();
            return mensaje;
        }
    }

    @Override
    public Mensaje desencriptar(Mensaje mensaje, String claveEncriptacion) {
        String contenido = mensaje.getContenido();

        // Si no es base64 válido, devolvemos el mensaje original
        if (!esBase64Valido(contenido)) {
            System.err.println("Contenido no es Base64 válido: " + contenido);
            return mensaje;
        }

        try {
            SecretKeySpec key = generarClave(claveEncriptacion);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] bytesEncriptados = Base64.getDecoder().decode(contenido);
            byte[] textoDesencriptado = cipher.doFinal(bytesEncriptados);
            String textoPlano = new String(textoDesencriptado, StandardCharsets.UTF_8);

            return new Mensaje(textoPlano, mensaje.getEmisor());
        } catch (Exception e) {
            System.err.println("Error al desencriptar con AES: " + contenido);
            e.printStackTrace();
            return mensaje;
        }
    }

    private SecretKeySpec generarClave(String clave) {
        byte[] claveBytes = new byte[16];
        byte[] claveOriginal = clave.getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < claveOriginal.length && i < claveBytes.length; i++) {
            claveBytes[i] = claveOriginal[i];
        }

        return new SecretKeySpec(claveBytes, ALGORITMO);
    }

    // Valida si una cadena es Base64 válida
    private boolean esBase64Valido(String texto) {
        // Debe tener longitud múltiplo de 4 y solo caracteres válidos
        if (texto == null || texto.length() % 4 != 0) return false;
        return texto.matches("^[A-Za-z0-9+/]*={0,2}$");
    }
}