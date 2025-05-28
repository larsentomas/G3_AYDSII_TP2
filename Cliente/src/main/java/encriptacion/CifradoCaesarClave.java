package encriptacion;

import common.Mensaje;

public class CifradoCaesarClave implements Strategy {

    public Mensaje encriptar(Mensaje mensaje, String claveEncriptacion) {
    String textoOriginal = mensaje.getContenido();
    int salto = obtenerSaltoDesdeClave(claveEncriptacion);
    StringBuilder textoEncriptado = new StringBuilder();

    for (char c : textoOriginal.toCharArray()) {
        if (Character.isLetter(c)) {
            char base = Character.isLowerCase(c) ? 'a' : 'A';
            textoEncriptado.append((char) ((c - base + salto) % 26 + base));
        } else {
            textoEncriptado.append(c);
        }
    }

    mensaje.setContenido(textoEncriptado.toString());
    return mensaje;
}

public Mensaje desencriptar(Mensaje mensaje, String claveEncriptacion) {
    String textoEncriptado = mensaje.getContenido();
    int salto = obtenerSaltoDesdeClave(claveEncriptacion);
    StringBuilder textoDesencriptado = new StringBuilder();

    for (char c : textoEncriptado.toCharArray()) {
        if (Character.isLetter(c)) {
            char base = Character.isLowerCase(c) ? 'a' : 'A';
            textoDesencriptado.append((char) ((c - base - salto + 26) % 26 + base));
        } else {
            textoDesencriptado.append(c);
        }
    }

    mensaje.setContenido(textoDesencriptado.toString());
    return mensaje;
}


private int obtenerSaltoDesdeClave(String claveEncriptacion) {
    int salto = 0;
    for (char c : claveEncriptacion.toCharArray()) {
        salto += c;
    }
    return salto % 26;
}
}
