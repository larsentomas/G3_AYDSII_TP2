package vista;

import modelo.Conversacion;
import modelo.Mensaje;
import sistema.Sistema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class VistaInicio extends JFrame implements IVistaInicio {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtf_buscar;
    private JButton btnBuscar;
    private JButton btnEnviar;
    private JButton btnAgregarChat;
    private JButton btnAgregarContacto;
    private JButton btnLoguout;
    private TextField txtf_mensaje;
    private List lista_chat;
    private JList<Conversacion> listaConversaciones;
    private DefaultListModel<Conversacion> listModelConversaciones;
    private Conversacion conversacion;
    private JPanel panel_chat;
    private JLabel lblCartelBienvenida;
    private JLabel lblContactoActivo;

    // Paleta Chatty
    private final Color AZUL_OSCURO = new Color(5, 10, 42);
    private final Color ROSA = new Color(255, 118, 123);
    private final Color GRIS_CLARO = new Color(240, 240, 240);
    private final Color BLANCO = Color.WHITE;

    public VistaInicio() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(200, 0, 1200, 800);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setBackground(BLANCO);
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));
        setTitle("Chatty");
        setIconImage(Toolkit.getDefaultToolkit().getImage(VistaLogin.class.getResource("/vista/Logo_chatty_sf.png")));

        JPanel panel_central = new JPanel();
        contentPane.add(panel_central, BorderLayout.CENTER);
        panel_central.setLayout(new BorderLayout(0, 0));

        JPanel panel_Conversaciones = new JPanel();
        panel_Conversaciones.setBackground(GRIS_CLARO);
        panel_central.add(panel_Conversaciones, BorderLayout.WEST);
        panel_Conversaciones.setLayout(new BorderLayout(0, 0));
        panel_Conversaciones.setPreferredSize(new Dimension(400, 600));

        this.listModelConversaciones = new DefaultListModel<>();
        this.listaConversaciones = new JList<>(listModelConversaciones);
        this.listaConversaciones.setVisibleRowCount(6);
        this.listaConversaciones.setCellRenderer(new CustomListCellRenderer());

        this.listaConversaciones.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Conversacion selectedConversacion = listaConversaciones.getSelectedValue();
                if (selectedConversacion != null) {
                    selectedConversacion.setNotificado(false);
                    actualizarPanelChat(selectedConversacion);
                    listaConversaciones.repaint();
                }
            }
        });

        panel_Conversaciones.add(new JScrollPane(this.listaConversaciones));

        this.panel_chat = new JPanel();
        panel_central.add(this.panel_chat, BorderLayout.CENTER);
        this.panel_chat.setLayout(new BorderLayout(0, 0));
        this.panel_chat.setPreferredSize(new Dimension(800, 500));
        this.panel_chat.setBackground(BLANCO);

        // Encabezado con nombre del contacto
        JPanel headerChat = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerChat.setBackground(AZUL_OSCURO);
        this.lblContactoActivo = new JLabel("Seleccione un chat");
        lblContactoActivo.setForeground(BLANCO);
        lblContactoActivo.setFont(new Font("Arial", Font.BOLD, 16));
        headerChat.add(lblContactoActivo);
        this.panel_chat.add(headerChat, BorderLayout.NORTH);

        JPanel chat = new JPanel();
        chat.setLayout(new BorderLayout(0, 0));
        chat.setBackground(BLANCO);
        this.lista_chat = new List();
        this.lista_chat.setBackground(BLANCO);
        chat.add(this.lista_chat);
        this.panel_chat.add(chat, BorderLayout.CENTER);

        JPanel acciones = new JPanel();
        acciones.setBackground(GRIS_CLARO);
        this.panel_chat.add(acciones, BorderLayout.SOUTH);

        this.txtf_mensaje = new TextField();
        this.txtf_mensaje.setColumns(30);
        acciones.add(this.txtf_mensaje);

        this.btnEnviar = new JButton("Enviar");
        aplicarEstiloBoton(this.btnEnviar);
        acciones.add(this.btnEnviar);

        JPanel panel_botones = new JPanel();
        panel_botones.setLayout(new GridLayout(2, 3, 10, 10));
        panel_botones.setBackground(GRIS_CLARO);
        contentPane.add(panel_botones, BorderLayout.SOUTH);

        this.btnAgregarChat = new JButton("Nueva conversacion");
        aplicarEstiloBoton(this.btnAgregarChat);
        this.btnAgregarContacto = new JButton("Agregar Usuario");
        aplicarEstiloBoton(this.btnAgregarContacto);
        this.btnLoguout = new JButton("Cerrar Sesión");
        aplicarEstiloBoton(this.btnLoguout);

        panel_botones.add(new JLabel());
        panel_botones.add(this.btnAgregarChat);
        panel_botones.add(this.btnAgregarContacto);
        panel_botones.add(this.btnLoguout);

        JPanel panel_norte = new JPanel();
        panel_norte.setBackground(AZUL_OSCURO);
        contentPane.add(panel_norte, BorderLayout.NORTH);
        this.lblCartelBienvenida = new JLabel("Bienvenido/a");
        this.lblCartelBienvenida.setForeground(BLANCO);
        panel_norte.add(this.lblCartelBienvenida);

        btnEnviar.setEnabled(false);
        txtf_mensaje.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                btnEnviar.setEnabled(!txtf_mensaje.getText().isEmpty());
            }
        });
    }

    private void aplicarEstiloBoton(JButton boton) {
        boton.setBackground(AZUL_OSCURO);
        boton.setForeground(BLANCO);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(ROSA);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(AZUL_OSCURO);
            }
        });
    }

    public void actualizarPanelChat(Conversacion conversacion) {
        this.panel_chat.setVisible(true);
        this.conversacion = conversacion;
        lista_chat.removeAll();
        for (Mensaje mensaje : conversacion.getMensajes()) {
            lista_chat.add("[" + mensaje.getTimestampCreado().getTime() + "] " + mensaje.getEmisor() + ":\n" + mensaje);
        }
        lblContactoActivo.setText("Chat con: " + conversacion.getIntegrante());
        lista_chat.revalidate();
        lista_chat.repaint();
    }

    @Override
    public void setActionListener(ActionListener actionListener) {
        this.btnAgregarChat.addActionListener(actionListener);
        this.btnAgregarContacto.addActionListener(actionListener);
        this.btnEnviar.addActionListener(actionListener);
        this.btnLoguout.addActionListener(actionListener);
    }

    @Override
    public void setVisibleVentana(boolean estado) {
        this.setVisible(estado);
    }

    @Override
    public void limpiarcampos() {
        this.txtf_mensaje.setText("");
        btnEnviar.setEnabled(false);
    }

    @Override
    public void setPanelchat(boolean estado) {
        this.panel_chat.setVisible(estado);
    }

    private class CustomListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Conversacion conversacion = (Conversacion) value;
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            panel.setBackground(isSelected ? AZUL_OSCURO : BLANCO);

            JLabel nombre = new JLabel(conversacion.getIntegrante());
            nombre.setForeground(isSelected ? BLANCO : Color.BLACK);
            nombre.setFont(new Font("Arial", Font.BOLD, 14));

            JLabel ultimoMensaje = new JLabel("" + conversacion.getMensajes().getLast()); //chequear
            ultimoMensaje.setForeground(isSelected ? BLANCO : Color.GRAY);
            ultimoMensaje.setFont(new Font("Arial", Font.PLAIN, 12));

            panel.add(nombre, BorderLayout.NORTH);
            panel.add(ultimoMensaje, BorderLayout.SOUTH);

            if (conversacion.isNotificado() && conversacion != getConversacionActiva()) {
                panel.setBorder(BorderFactory.createLineBorder(ROSA, 2));
            }

            return panel;
        }
    }

    public JButton getBtnNuevoContacto() {
        return btnAgregarContacto;
    }

    public JButton getBtnNuevaConversacion() {
        return btnAgregarChat;
    }

    public JButton getEnviarMensaje() {
        return btnEnviar;
    }

    public String getMensaje() {
        return this.txtf_mensaje.getText();
    }

    public Conversacion getConversacionActiva() {
        return this.conversacion;
    }

    public void setConversacion(Conversacion conversacion) {
        this.conversacion = conversacion;
    }

    public void setBienvenida(String nombre) {
        this.lblCartelBienvenida.setText("Bienvenido/a  " + nombre);
    }

    public JButton getBtnLoguout() {
        return btnLoguout;
    }

    public void mostrarModalError(String s) {
        JOptionPane.showMessageDialog(this, s, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarModalExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
} 
