package controlador;

import sistema.Sistema;
import vista.VistaLogin;
import vista.VistaInicio;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginControlador implements ActionListener {
    private VistaLogin vistaLogin;
    private VistaInicio vistaInicio;

    public LoginControlador(VistaLogin vistaLogin, VistaInicio vistaInicio) {
        this.vistaLogin = vistaLogin;
        this.vistaInicio = vistaInicio;
        initController();
    }

    private void initController() {
        vistaLogin.getBotonInicio().setActionCommand("INICIAR_SESION");
        vistaLogin.getBotonInicio().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("INICIAR_SESION")) {
            Sistema sistema = Sistema.getInstance();

            String usuario = vistaLogin.getUser();
            String puerto = vistaLogin.getPuerto();

            if (sistema.iniciarUsuario(usuario, puerto))  {
                vistaInicio.setVisible(true);
                vistaLogin.setVisible(false);
                this.vistaInicio.setBienvenida(usuario);
            } else {
                vistaLogin.mostrarModalError("Error al iniciar sesion");
            }
        }
    }
}

