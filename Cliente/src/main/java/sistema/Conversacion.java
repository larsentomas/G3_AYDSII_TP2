package sistema;

import java.util.ArrayList;

public class Conversacion {
    private String integrante;
    private ArrayList<Mensaje> mensajes;

    public Conversacion(String persona) {
        this.integrante = persona;
        this.mensajes = new ArrayList<>();
    }

    public void agregarMensaje(Mensaje mensaje) {
        this.mensajes.add(mensaje);
    }

    public ArrayList<Mensaje> getMensajes() {
        return mensajes;
    }

    public String getIntegrante() {
        return integrante;
    }

    @Override
    public String toString(){
        return integrante;
    }
}
