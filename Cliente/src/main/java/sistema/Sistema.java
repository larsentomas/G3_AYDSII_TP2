package sistema;

import controlador.Controlador;
import controlador.ControladorLogin;
import modelo.UsuarioLogueado;
import vista.VistaInicio;
import vista.VistaLogin;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Sistema {

    // Info del servidor
    private int port;
    private String ip;

    private static Sistema instance = null;
    private UsuarioLogueado usuarioLogueado = null;

    static VistaInicio vistaInicio = new VistaInicio();
    static VistaLogin vistaLogin = new VistaLogin();

    static Controlador controlador;
    static ControladorLogin controladorLogin;


    public static void main(String[] args) {
        instance = Sistema.getInstance();

        controlador = new Controlador(vistaInicio);
        controladorLogin = new ControladorLogin(vistaLogin, vistaInicio);

        vistaInicio.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Lo que pasa cuando se cierra la ventana
            }
        });
    }

    public static Sistema getInstance() {
        if (instance == null) {
            instance = new Sistema();
        }
        return instance;
    }
}