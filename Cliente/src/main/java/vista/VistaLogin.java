package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * VistaLogin representa la ventana de inicio de sesión de la aplicación Chatty.
 * Permite al usuario ingresar un nombre de usuario y puerto, y ofrece un botón para iniciar sesión.
 * 
 */
public class VistaLogin extends JFrame implements ILogin {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtf_user;
    private JTextField txtf_port;
    private JButton btn_inicio;

    // Colores
    Color colorOriginal = Color.decode("#FF767B");
    Color colorHover = Color.decode("#FF9297");
    Color colorDeshabilitado = Color.decode("#FFB6B9");

    /**
     * Constructor de la ventana de inicio de sesión.
     * Configura la interfaz gráfica, los paneles, los campos de texto y el botón de inicio.
     */
    public VistaLogin() {
        // Configuración básica de la ventana
        setIconImage(Toolkit.getDefaultToolkit().getImage(VistaLogin.class.getResource("/vista/Logo_chatty_sf.png")));
        setResizable(false);
        setTitle("Chatty");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(282, 220);
        setLocationRelativeTo(null);

        // Content Pane principal
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.setBackground(Color.WHITE);
        setContentPane(contentPane);

        // Panel superior (barra de título)
        JPanel panel_norte = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel_norte.setPreferredSize(new Dimension(120, 30));
        panel_norte.setBackground(Color.decode("#050A2A"));
        JLabel lbl_title = new JLabel("Bienvenido/a!");
        lbl_title.setForeground(Color.WHITE);
        panel_norte.add(lbl_title);
        contentPane.add(panel_norte, BorderLayout.NORTH);

        // Panel imagen lateral
        JPanel panel_oeste = new JPanel();
        panel_oeste.setPreferredSize(new Dimension(100, 90));
        panel_oeste.setBackground(Color.WHITE);
        ImageIcon imagenOriginal = new ImageIcon(VistaLogin.class.getResource("/vista/Logo_login_sf.png"));
        Image imagenEscalada = imagenOriginal.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel etiquetaImagen = new JLabel(new ImageIcon(imagenEscalada));
        panel_oeste.add(etiquetaImagen);
        contentPane.add(panel_oeste, BorderLayout.WEST);

        // Panel derecho con campos y botón
        JPanel panel_este = new JPanel(new GridLayout(3, 1, 0, 0));
        panel_este.setPreferredSize(new Dimension(100, 90));
        panel_este.setBackground(Color.WHITE);
        contentPane.add(panel_este, BorderLayout.CENTER);

        // Campo usuario
        JPanel panel_usuario = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel_usuario.setBackground(Color.WHITE);
        JLabel lblUser = new JLabel("Usuario:");
        txtf_user = new JTextField(8);
        panel_usuario.add(lblUser);
        panel_usuario.add(txtf_user);
        panel_este.add(panel_usuario);

        // Campo puerto
        JPanel panel_puerto = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel_puerto.setBackground(Color.WHITE);
        JLabel lblPuerto = new JLabel(" Puerto:");
        txtf_port = new JTextField(8);
        panel_puerto.add(lblPuerto);
        panel_puerto.add(txtf_port);
        panel_este.add(panel_puerto);

        // Botón iniciar sesión
        JPanel panel_boton = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel_boton.setBackground(Color.WHITE);
        btn_inicio = new JButton("Iniciar");
        btn_inicio.setFocusPainted(false);
        btn_inicio.setBorderPainted(false);
        btn_inicio.setBackground(colorDeshabilitado);  // Deshabilitado de inicio
        btn_inicio.setForeground(Color.WHITE);
       // btn_inicio.setEnabled(false);
        panel_boton.add(btn_inicio);
        panel_este.add(panel_boton);

        // Hover efecto
        btn_inicio.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn_inicio.isEnabled()) btn_inicio.setBackground(colorHover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn_inicio.setBackground(btn_inicio.isEnabled() ? colorOriginal : colorDeshabilitado);
            }
        });
        btn_inicio.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // DocumentListener para habilitar el botón cuando ambos campos tengan texto
        DocumentListener listener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { verificarCampos(); }
            public void removeUpdate(DocumentEvent e) { verificarCampos(); }
            public void insertUpdate(DocumentEvent e) { verificarCampos(); }
        };
        txtf_user.getDocument().addDocumentListener(listener);
        txtf_port.getDocument().addDocumentListener(listener);

        setVisible(true);
    }

    /**
     * Verifica si ambos campos de texto contienen valores no vacíos.
     * Habilita o deshabilita el botón de inicio en consecuencia.
     */
    private void verificarCampos() {
        boolean habilitar = !txtf_user.getText().trim().isEmpty() && !txtf_port.getText().trim().isEmpty();
        btn_inicio.setEnabled(habilitar);
        btn_inicio.setBackground(habilitar ? colorOriginal : colorDeshabilitado);
    }

    // Métodos de la interfaz ILogin
    
    /**
     * Obtiene el nombre de usuario ingresado en el campo correspondiente.
     * 
     * @return nombre de usuario como String
     */
    @Override
    public String getUser() { return this.txtf_user.getText(); }

    /**
     * Obtiene el valor del puerto ingresado en el campo correspondiente.
     * 
     * @return número de puerto como String
     */
    @Override
    public String getPuerto() { return this.txtf_port.getText(); }

    /**
     * Asigna un ActionListener al botón de inicio de sesión.
     * 
     * @param actionListener ActionListener a asociar
     */
    @Override
    public void setActionListener(ActionListener actionListener) {
        this.btn_inicio.addActionListener(actionListener);
    }

    /**
     * Controla la visibilidad de la ventana.
     * 
     * @param estado true para mostrar, false para ocultar
     */
    @Override
    public void setVisibleVentana(boolean estado) {
        this.setVisible(estado);
    }

    /**
     * Devuelve el botón de inicio de sesión.
     * 
     * @return JButton correspondiente al botón de inicio
     */
    public JButton getBotonInicio() { return this.btn_inicio; }

    /**
     * Limpia los campos de usuario y puerto, dejándolos vacíos.
     */
    public void limpiarcampos() {
        this.txtf_user.setText("");
        this.txtf_port.setText("");
    }

    /**
     * Muestra un mensaje de error en una ventana modal.
     * 
     * @param mensaje texto del error a mostrar
     */
    public void mostrarModalError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void setBtnInicio(boolean estado) {
    	this.btn_inicio.setEnabled(estado);
    }


}
