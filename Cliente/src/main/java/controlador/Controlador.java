package controlador;

import sistema.Sistema;
import vista.VistaInicio;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Controlador implements ActionListener {

    private VistaInicio vistaInicio;

    public Controlador(VistaInicio vistaInicio) {
        this.vistaInicio = vistaInicio;
        initController();
    }

    private void initController() {
        this.vistaInicio.getBtnNuevaConversacion().setActionCommand("NUEVA_CONVERSACION");
        this.vistaInicio.getBtnNuevaConversacion().addActionListener(this);
        this.vistaInicio.getBtnNuevoContacto().setActionCommand("AGREGAR_CONTACTO");
        this.vistaInicio.getBtnNuevoContacto().addActionListener(this);
        this.vistaInicio.getEnviarMensaje().setActionCommand("ENVIAR_MENSAJE");
        this.vistaInicio.getEnviarMensaje().addActionListener(this);
        this.vistaInicio.setPanelchat(false);
        this.vistaInicio.getBtnLoguout().setActionCommand("LOGOUT");
        this.vistaInicio.getBtnLoguout().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Sistema s = Sistema.getInstance();

        if (e.getActionCommand().equalsIgnoreCase("NUEVA_CONVERSACION")) {

        } else if (e.getActionCommand().equalsIgnoreCase("AGERGAR_CONTACTO")) {

        } else if (e.getActionCommand().equalsIgnoreCase("ENVIAR_MENSAJE")) {

        } else if (e.getActionCommand().equalsIgnoreCase("LOGOUT")) {

        }
    }


}
