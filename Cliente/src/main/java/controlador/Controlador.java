package controlador;

import modelo.Conversacion;
import modelo.Usuario;
import sistema.Sistema;
import vista.VistaInicio;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

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
        Sistema sistema = Sistema.getInstance();

        if (e.getActionCommand().equalsIgnoreCase("NUEVA_CONVERSACION")) {
            ArrayList<String> opciones = sistema.getUsuarioLogueado().getContactosSinConversacion();
            // Si no hay contacto para iniciar conversacion --> error
            if (opciones.isEmpty()) {
                vistaInicio.mostrarModalError("No hay contactos disponibles para iniciar una conversación.");
                return;
            }

            // Si hay contacto para iniciar conversacion --> mostrar modal
            String usuario_conversacion = vistaInicio.mostrarModalNuevaConversacion(opciones);

            // Si el usuario no es null --> crear conversacion
            if (usuario_conversacion != null) {
                Conversacion c = sistema.crearConversacion(usuario_conversacion);
                vistaInicio.actualizarPanelChat(c);
                vistaInicio.actualizarListaConversaciones();
            }
        } else if (e.getActionCommand().equalsIgnoreCase("AGREGAR_CONTACTO")) {
            try {
                sistema.getPosiblesContactos();
            } catch (IOException ex) {
                vistaInicio.mostrarModalError("Error al obtener los contactos.");
            }
        } else if (e.getActionCommand().equalsIgnoreCase("ENVIAR_MENSAJE")) {
            String mensaje = vistaInicio.getMensaje();
            vistaInicio.limpiarcampos();
            // Validar que el mensaje no esté vacío
            if (mensaje.isEmpty()) {
                vistaInicio.mostrarModalError("El mensaje no puede estar vacío.");
                return;
            }
            try {
                sistema.enviarMensaje(mensaje, vistaInicio.getConversacionActiva());
            } catch (IOException ex) {
                vistaInicio.mostrarModalError("Error al enviar el mensaje.");
            }
        } else if (e.getActionCommand().equalsIgnoreCase("LOGOUT")) {
            // Cerrar la sesión del usuario
            Sistema.cerrarSesion();
            vistaInicio.dispose();
            vistaInicio.setVisible(false);
        }
    }


}
