package vista;

import modelo.Conversacion;
import modelo.Mensaje;
import modelo.Usuario;
import sistema.MensajeriaP2P;

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
    private JList listaConversaciones;
    private DefaultListModel<Conversacion> listModelConversaciones;
    private Conversacion conversacion;
    private JPanel panel_chat;
    private JLabel lblCartelBienvenida;

    public VistaInicio() {
        // Initialize componente
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(200, 0, 1200, 800);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        JPanel panel_central = new JPanel();
        contentPane.add(panel_central, BorderLayout.CENTER);
        panel_central.setLayout(new BorderLayout(0, 0));

        JPanel panel_Conversaciones = new JPanel();
        panel_central.add(panel_Conversaciones);
        panel_Conversaciones.setLayout(new BorderLayout(0, 0));
        panel_Conversaciones.setPreferredSize(new Dimension(400, 600));

        this.listModelConversaciones = new DefaultListModel<>();
        this.listaConversaciones = new JList<>(listModelConversaciones);
        this.listaConversaciones.setVisibleRowCount(6);
        this.listaConversaciones.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Conversacion selectedConversacion = (Conversacion) listaConversaciones.getSelectedValue();
                if (selectedConversacion != null) {
                    actualizarPanelChat(selectedConversacion);
                }
            }
        });
        panel_Conversaciones.add(this.listaConversaciones);

        this.panel_chat = new JPanel();
        panel_central.add(this.panel_chat, BorderLayout.EAST);
        this.panel_chat.setLayout(new BorderLayout(0, 0));
        this.panel_chat.setPreferredSize(new Dimension(800, 500));
        JPanel chat = new JPanel();
        this.panel_chat.add(chat, BorderLayout.CENTER);
        chat.setLayout(new BorderLayout(0, 0));

        this.lista_chat = new List();
        this.lista_chat.setBackground(new Color(255, 255, 255));
        chat.add(this.lista_chat);

        JPanel acciones = new JPanel();
        this.panel_chat.add(acciones, BorderLayout.SOUTH);

        this.txtf_mensaje = new TextField();
        this.txtf_mensaje.setColumns(30);
        acciones.add(this.txtf_mensaje);

        this.btnEnviar = new JButton("Enviar");
        acciones.add(this.btnEnviar);

        JPanel panel_botones = new JPanel();
        contentPane.add(panel_botones, BorderLayout.SOUTH);
        panel_botones.setLayout(new GridLayout(2, 3, 0, 0));

        JLabel lblNewLabel = new JLabel("");
        panel_botones.add(lblNewLabel);

        this.btnAgregarChat = new JButton("Nueva conversacion");
        panel_botones.add(this.btnAgregarChat);

        this.btnAgregarContacto = new JButton("Agregar Usuario");
        panel_botones.add(this.btnAgregarContacto);

        this.btnLoguout = new JButton("Cerrar Sesión");
        panel_botones.add(this.btnLoguout);

        JPanel panel_norte = new JPanel();
        contentPane.add(panel_norte, BorderLayout.NORTH);
        this.lblCartelBienvenida = new JLabel("Bienvenido/a");
        panel_norte.add(this.lblCartelBienvenida);

        /**

        this.txtf_buscar = new JTextField();
        this.txtf_buscar.setText("a quien buscas?");
        panel_norte.add(this.txtf_buscar);
        this.txtf_buscar.setColumns(10);

        this.btnBuscar = new JButton("buscar");
        panel_norte.add(this.btnBuscar);
    */

        // Desactivar el botón de enviar por defecto
        btnEnviar.setEnabled(false);

        // Habilitar/deshabilitar el botón de enviar según el contenido del mensaje
        txtf_mensaje.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Habilitar el botón de enviar si el campo no está vacío, deshabilitar si está vacío
                if (txtf_mensaje.getText().isEmpty()) {
                    btnEnviar.setEnabled(false);
                } else {
                    btnEnviar.setEnabled(true);
                }
            }
        });

        // Verificar si el TextField está vacío en la inicialización
        if (txtf_mensaje.getText().isEmpty()) {
            btnEnviar.setEnabled(false);
        }

        // Agregar un ListSelectionListener para restablecer el color cuando se selecciona el elemento

        listaConversaciones.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = listaConversaciones.getSelectedIndex();
                if (selectedIndex != -1) {
                    Conversacion selectedConversacion = listModelConversaciones.getElementAt(selectedIndex);
                    // Actualizar el estado de la conversación para que no se muestre en rojo
                    selectedConversacion.setNotificado(false);
                    // Forzar la renderización de la lista
                    listaConversaciones.repaint();
                }
            }
        });

        this.listaConversaciones.setCellRenderer(new CustomListCellRenderer());
    }

    @Override
    public void setActionListener(ActionListener actionListener) {
        this.btnBuscar.addActionListener(actionListener);
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
        // Desactivar el botón de enviar cuando se limpian los campos
        btnEnviar.setEnabled(false);
    }

    @Override
    public void setPanelchat(boolean estado) {
        this.panel_chat.setVisible(estado);
    }

    // Manejo de conversaciones
    public void actualizarPanelChat(Conversacion conversacion) {
        this.panel_chat.setVisible(true);
        lista_chat.removeAll();
        Usuario usuarioAgendado = MensajeriaP2P.getInstance().existeUsuario(conversacion.getUsuario().getIp(), conversacion.getUsuario().getPuerto());
        for (Mensaje mensaje : conversacion.getMensajes()) {
            lista_chat.add("[" + mensaje.getTimestamp().getTime() + "]" + mensaje.getEmisor().getNickname() + ":\n" + mensaje.toString());
        }
        lista_chat.revalidate();
        lista_chat.repaint();
        setConversacion(conversacion);
        // Si la conversación está activa, habilitar el botón de enviar y el campo de texto
        btnEnviar.setEnabled(conversacion.isActiva());
        txtf_mensaje.setEnabled(conversacion.isActiva());

        // Si no hay conversaciones activas, deshabilitar el panel de chat
        if (conversacion == null || !conversacion.isActiva()) {
            panel_chat.setVisible(false);
        } else {
            panel_chat.setVisible(true);
        }
    }

    public void actualizarListaConversaciones() {
        listModelConversaciones.clear();
        for (Conversacion conversacion : MensajeriaP2P.getInstance().getUser().getConversacionesActivas()) {
            listModelConversaciones.addElement(conversacion);
        }
    }

    /*
    //Change the border color of the element in the list
    public void Notificar(Conversacion c) {
        int index = listModelConversaciones.indexOf(c);
        if (index != -1) {
            listaConversaciones.setSelectionBackground(Color.BLUE);
            listaConversaciones.setSelectionForeground(Color.WHITE);
            listaConversaciones.setSelectedIndex(index);
            listaConversaciones.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        }
    }
    */
    private class CustomListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Conversacion conversacion = (Conversacion) value;
            if (isSelected || MensajeriaP2P.getInstance().getVistaInicio().getConversacionActiva() == conversacion) {
                component.setBackground(Color.BLUE);
                component.setForeground(Color.WHITE);
                ((JComponent) component).setBorder(null);
            } else if (conversacion.isNotificado()) {
                component.setBackground(list.getBackground());
                component.setForeground(list.getForeground());
                ((JComponent) component).setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            } else {
                component.setBackground(list.getBackground());
                component.setForeground(list.getForeground());
                ((JComponent) component).setBorder(null);
            }
            return component;
        }
    }

    public void Notificar(Conversacion c) {
        int index = listModelConversaciones.indexOf(c);
        if (index != -1) {
            listaConversaciones.setSelectedIndex(index);
        }
    }


                          // Getters y Setters
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
        this.lblCartelBienvenida.setText("Bienvenido/a  "+ nombre);
        // this.lblCartelBienvenida.setFont(new Font("Arial", Font.BOLD, 20));
        // this.lblCartelBienvenida.setForeground(Color.BLUE);
    }

    public JButton getBtnLoguout() {
        return btnLoguout;
    }

    // Modales
    public Usuario mostrarModalNuevaConversacion() {
        ArrayList<Usuario> contactosSinConversacion = MensajeriaP2P.getInstance().getUser().getContactosSinConversacion();
        Usuario[] opciones = contactosSinConversacion.toArray(new Usuario[0]);
        return (Usuario) JOptionPane.showInputDialog(
                this,
                "Seleccione un contacto para iniciar una nueva conversación:",
                "Nueva Conversación",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]
        );
    }

    public String[] mostrarModalAgregarContacto() {
        JTextField nombreField = new JTextField();
        JTextField ipField = new JTextField();
        JTextField puertoField = new JTextField();

        Object[] message = {
                "Nombre:", nombreField,
                "IP:", ipField,
                "Puerto:", puertoField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Agregar Usuario", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            return new String[]{nombreField.getText(), ipField.getText(), puertoField.getText()};
        } else {
            return null;
        }
    }

    public void mostrarModalError(String s) {
        JOptionPane.showMessageDialog(this, s, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarModalExito(String contactoAgregadoExitosamente) {
        JOptionPane.showMessageDialog(this, contactoAgregadoExitosamente, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
}
