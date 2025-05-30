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
            e.printStackTrace();
            return mensaje;
        }
    }

    @Override
    public Mensaje desencriptar(Mensaje mensaje, String claveEncriptacion) {
        try {
            SecretKeySpec key = generarClave(claveEncriptacion);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] bytesEncriptados = Base64.getDecoder().decode(mensaje.getContenido());
            byte[] textoDesencriptado = cipher.doFinal(bytesEncriptados);
            String contenido = new String(textoDesencriptado, StandardCharsets.UTF_8);

            return new Mensaje(contenido, mensaje.getEmisor());
        } catch (Exception e) {
            e.printStackTrace();
            return mensaje;
        }
    }

    private SecretKeySpec generarClave(String clave) {
        // Asegura una clave de 16 bytes para AES-128
        byte[] claveBytes = new byte[16];
        byte[] claveOriginal = clave.getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < claveOriginal.length && i < claveBytes.length; i++) {
            claveBytes[i] = claveOriginal[i];
        }

        return new SecretKeySpec(claveBytes, ALGORITMO);
    }
}
