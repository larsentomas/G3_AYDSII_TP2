package encriptacion;
import common.Mensaje;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncriptarAES implements Strategy {

    private static final String ALGORITMO = "AES";

    public Mensaje encriptar(Mensaje mensaje, String claveEncriptacion) {
        try {
            SecretKeySpec key = generarClave(claveEncriptacion);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] textoEncriptado = cipher.doFinal(mensaje.getContenido().getBytes());
            String textoBase64 = Base64.getEncoder().encodeToString(textoEncriptado);
            mensaje.setContenido(textoBase64);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mensaje;
    }

    public Mensaje desencriptar(Mensaje mensaje, String claveEncriptacion) {
        try {
            SecretKeySpec key = generarClave(claveEncriptacion);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] bytesEncriptados = Base64.getDecoder().decode(mensaje.getContenido());
            byte[] textoDesencriptado = cipher.doFinal(bytesEncriptados);
            mensaje.setContenido(new String(textoDesencriptado));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mensaje;
    }

    private SecretKeySpec generarClave(String clave) throws Exception {
        // Reducción de clave a 16 bytes (128 bits)
        byte[] claveBytes = new byte[16];
        byte[] claveOriginal = clave.getBytes("UTF-8");

        for (int i = 0; i < claveOriginal.length && i < claveBytes.length; i++) {
            claveBytes[i] = claveOriginal[i];
        }

        return new SecretKeySpec(claveBytes, ALGORITMO);
    }
}

