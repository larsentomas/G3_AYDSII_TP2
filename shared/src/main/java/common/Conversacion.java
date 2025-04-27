package common;

import common.Mensaje;
import java.io.Serializable;
import java.util.ArrayList;


public class Conversacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String integrante;
    private ArrayList<Mensaje> mensajes;
    private boolean notificado;

    public Conversacion(String persona) {
        this.integrante = persona;
        this.mensajes = new ArrayList<>();
    }

    public void agregarMensaje(Mensaje mensaje) {
        System.out.println("Agregando mensaje " + mensaje);
        this.setNotificado(true);
        this.mensajes.add(mensaje);
    }

    public ArrayList<Mensaje> getMensajes() {
        return mensajes;
    }

    public String getIntegrante() {
        return integrante;
    }

    public boolean isNotificado() {
        return notificado;
    }

    public void setIntegrante(String integrante) {
        this.integrante = integrante;
    }

    public void setMensajes(ArrayList<Mensaje> mensajes) {
        this.mensajes = mensajes;
    }

    public void setNotificado(boolean notificado) {
        this.notificado = notificado;
    }

    @Override
    public String toString(){
        return integrante;
    }
}
