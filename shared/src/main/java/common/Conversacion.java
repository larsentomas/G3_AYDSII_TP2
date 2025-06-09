package common;

import common.Mensaje;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class Conversacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String integrante;
    private CopyOnWriteArrayList<Mensaje> mensajes = new CopyOnWriteArrayList<>();
    private ArrayList<Mensaje> mensajesXML = new ArrayList<>();
    private boolean notificado;

    public Conversacion(String persona) {
        this.integrante = persona;
    }

    public Conversacion() {}

    public void agregarMensaje(Mensaje mensaje) {
        System.out.println("Agregando mensaje " + mensaje);
        this.setNotificado(true);
        this.mensajes.add(mensaje);
    }

    public CopyOnWriteArrayList<Mensaje> getMensajes() {
        if (mensajes == null || mensajes.isEmpty()) {
            mensajes = new CopyOnWriteArrayList<>();
            if (mensajesXML != null && !mensajesXML.isEmpty()) {
                mensajes.addAll(mensajesXML);
            }
        }
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

    public void setMensajes(List<Mensaje> mensajes) {
        this.mensajes = new CopyOnWriteArrayList<>(mensajes);
    }

    public void setNotificado(boolean notificado) {
        this.notificado = notificado;
    }

    public void ponerMensaje(Mensaje mensaje) {
        this.mensajes.add(mensaje);
    }

    public ArrayList<Mensaje> getMensajesXML() {
        return new ArrayList<>(mensajes);
    }

    public void setMensajesXML(ArrayList<Mensaje> mensajesXML) {
        this.mensajesXML = mensajesXML;
    }


    @Override
    public String toString(){
        return "Conversacion{" +
                "integrante='" + integrante + '\'' +
                ", mensajes=" + mensajes +
                ", notificado=" + notificado +
                '}';
    }
}
