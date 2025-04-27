package controlador;

import excepciones.PuertoInvalidoException;
import excepciones.UsuarioExistenteException;
import sistema.Sistema;
import vista.VistaInicio;
import vista.VistaLogin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControladorLogin implements ActionListener {

    private VistaLogin vistaLogin;

    public ControladorLogin(VistaLogin vistaLogin) {
        this.vistaLogin = vistaLogin;
        this.vistaLogin.setBtnInicio(false);
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

            try {
                sistema.iniciarUsuario(usuario, puerto);
            } catch (PuertoInvalidoException ex) {
                vistaLogin.mostrarModalError(ex.getMessage());
            }
        }
    }
}
