package encriptacion;

import common.Mensaje;

public class CifradoCaesarClave implements Strategy {

    @Override
    public Mensaje encriptar(Mensaje mensaje, String claveEncriptacion) {
        if (claveEncriptacion == null || claveEncriptacion.isEmpty()) {
            throw new IllegalArgumentException("La clave de encriptación no puede ser nula o vacía.");
        }

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

        return new Mensaje(textoEncriptado.toString(), mensaje.getEmisor());
    }

    @Override
    public Mensaje desencriptar(Mensaje mensaje, String claveEncriptacion) {
        if (claveEncriptacion == null || claveEncriptacion.isEmpty()) {
            throw new IllegalArgumentException("La clave de encriptación no puede ser nula o vacía.");
        }

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

        return new Mensaje(textoDesencriptado.toString(), mensaje.getEmisor());
    }

    /**
     * Convierte la clave en un desplazamiento entre 0 y 25.
     * Se suman los valores ASCII de los caracteres y se toma módulo 26.
     */
    private int obtenerSaltoDesdeClave(String claveEncriptacion) {
        int salto = 0;
        for (char c : claveEncriptacion.toCharArray()) {
            salto += c;
        }
        return salto % 26;
    }
}
