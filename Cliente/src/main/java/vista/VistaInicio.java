package vista;

import common.Mensaje;
import common.Conversacion;
import sistema.Sistema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JButton;

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
    private Conversacion conversacion = null;
    private JPanel panel_chat;
    private JLabel lblCartelBienvenida;
    private JLabel lblContactoActivo;
    private boolean darkMode = false;
    private JButton btnModo;

    // Paleta Chatty
    private final Color AZUL_OSCURO = new Color(5, 10, 42);
    private final Color ROSA = new Color(255, 118, 123);
    private final Color GRIS_CLARO = new Color(240, 240, 240);
    private final Color BLANCO = Color.WHITE;
    
 // Paleta Dark Mode Chatty
    private final Color NEGRO = new Color(18, 18, 18);
    private final Color GRIS_OSCURO = new Color(48, 48, 48);
    private final Color BLANCO_GRISACEO = new Color(220, 220, 220);
    private final Color AZUL_CLARO = new Color(100, 149, 237);

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

        JPanel panel_norte = new JPanel(new BorderLayout());
        panel_norte.setBackground(AZUL_OSCURO);
        contentPane.add(panel_norte, BorderLayout.NORTH);

        this.lblCartelBienvenida = new JLabel("Bienvenido/a");
        this.lblCartelBienvenida.setForeground(BLANCO);
        this.lblCartelBienvenida.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel_norte.add(this.lblCartelBienvenida, BorderLayout.WEST);

        // botón Modo Oscuro/Claro
        this.btnModo = new JButton("🌙");
        aplicarEstiloBoton(btnModo);
        panel_norte.add(btnModo, BorderLayout.EAST);

        btnEnviar.setEnabled(false);
        txtf_mensaje.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                btnEnviar.setEnabled(!txtf_mensaje.getText().isEmpty());
            }
        });
        
        btnModo.addActionListener(e -> toggleDarkMode());
    }

    private void toggleDarkMode() {
        darkMode = !darkMode;

        if (darkMode) {
            contentPane.setBackground(AZUL_OSCURO);
            listaConversaciones.setBackground(Color.DARK_GRAY);
            listaConversaciones.setForeground(BLANCO);
            lista_chat.setBackground(Color.DARK_GRAY);
            lista_chat.setForeground(BLANCO);
            panel_chat.setBackground(AZUL_OSCURO);
            lblContactoActivo.setForeground(ROSA);
            lblCartelBienvenida.setForeground(ROSA);
            btnModo.setText("☀️"); // Cambia a modo claro

            // --- Colores de botones en dark mode ---
            setColorBoton(btnEnviar, ROSA, BLANCO);
            setColorBoton(btnAgregarChat, ROSA, BLANCO);
            setColorBoton(btnAgregarContacto, ROSA, BLANCO);
            setColorBoton(btnLoguout, ROSA, BLANCO);

        } else {
            contentPane.setBackground(BLANCO);
            listaConversaciones.setBackground(GRIS_CLARO);
            listaConversaciones.setForeground(Color.BLACK);
            lista_chat.setBackground(BLANCO);
            lista_chat.setForeground(Color.BLACK);
            panel_chat.setBackground(BLANCO);
            lblContactoActivo.setForeground(BLANCO);
            lblCartelBienvenida.setForeground(BLANCO);
            btnModo.setText("🌙"); // Cambia a modo oscuro

            // --- Colores de botones en modo claro ---
            setColorBoton(btnEnviar, AZUL_OSCURO, BLANCO);
            setColorBoton(btnAgregarChat, AZUL_OSCURO, BLANCO);
            setColorBoton(btnAgregarContacto, AZUL_OSCURO, BLANCO);
            setColorBoton(btnLoguout, AZUL_OSCURO, BLANCO);
        }

        listaConversaciones.repaint();
        lista_chat.repaint();
        panel_chat.repaint();
        contentPane.repaint();
    }

    private void setColorBoton(JButton boton, Color fondo, Color texto) {
        boton.setBackground(fondo);
        boton.setForeground(texto);
    }

    private void aplicarEstiloBoton(JButton boton) {
        boton.setBackground(AZUL_OSCURO);
        boton.setForeground(BLANCO);
        boton.setFocusPainted(false);
        boton.setBorder(new RoundedBorder(5)); // 15 píxeles de radio redondeado
        boton.setContentAreaFilled(true);
        boton.setOpaque(true);

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
        lblContactoActivo.setText("Chat con: " + Sistema.getInstance().getUsuarioLogueado().getApodo(conversacion.getIntegrante()));
        this.conversacion = conversacion;
        lista_chat.removeAll();
        for (Mensaje mensaje : conversacion.getMensajes()) {
            Date fecha = new Date(mensaje.getTimestampCreado().getTime());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy hh:mm");
            String formateada = formatter.format(fecha);
            if (mensaje.getEmisor().equalsIgnoreCase(Sistema.getInstance().getUsuarioLogueado().getNombre())) {
                lista_chat.add("[" + formateada + "] " + Sistema.getInstance().getUsuarioLogueado().getNombre() + ":\n" + mensaje);
            } else
            lista_chat.add("[" + formateada + "] " + Sistema.getInstance().getUsuarioLogueado().getApodo(mensaje.getEmisor()) + ":\n" + mensaje);
        }
        lblContactoActivo.revalidate();
        lblContactoActivo.repaint();
        lista_chat.revalidate();
        lista_chat.repaint();
        lista_chat.makeVisible(lista_chat.getItemCount() - 1);
        this.revalidate();
        this.repaint();
    }

    public void actualizarListaConversaciones() {
        listModelConversaciones.clear();
        for (Conversacion conversacion : Sistema.getInstance().getUsuarioLogueado().getConversaciones()) {
            listModelConversaciones.addElement(conversacion);
        }
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

            JLabel nombre = new JLabel(Sistema.getInstance().getUsuarioLogueado().getApodo(conversacion.getIntegrante()));
            nombre.setForeground(isSelected ? BLANCO : Color.BLACK);
            nombre.setFont(new Font("Arial", Font.BOLD, 14));
            panel.add(nombre, BorderLayout.NORTH);

            if (!conversacion.getMensajes().isEmpty()) {
                JLabel ultimoMensaje = new JLabel("" + conversacion.getMensajes().getLast()); //chequear
                ultimoMensaje.setForeground(isSelected ? BLANCO : Color.GRAY);
                ultimoMensaje.setFont(new Font("Arial", Font.PLAIN, 12));
                panel.add(ultimoMensaje, BorderLayout.SOUTH);
            }

            if (conversacion.isNotificado() && conversacion != getConversacionActiva()) {
                panel.setBorder(BorderFactory.createLineBorder(ROSA, 2));
            }

            return panel;
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
 public String mostrarModalNuevaConversacion(ArrayList<String> opciones) {
     if (opciones == null || opciones.isEmpty()) {
         JOptionPane.showMessageDialog(this, "No hay contactos disponibles para iniciar una conversación.", "Información", JOptionPane.INFORMATION_MESSAGE);
         return null;
     }

     String seleccion = (String) JOptionPane.showInputDialog(
             this,
             "Seleccione un contacto para iniciar una nueva conversación:",
             "Nueva Conversación",
             JOptionPane.PLAIN_MESSAGE,
             null,
             opciones.toArray(),
             opciones.get(0)
     );

     if (seleccion == null) {
         return null; // User cancelled the dialog
     }

     return seleccion; // Return the selected contact or null if no selection was made

 }

 public ArrayList<String> mostrarModalAgregarContacto(ArrayList<String> posiblesContactos) {
     if (posiblesContactos == null || posiblesContactos.isEmpty()) {
         JOptionPane.showMessageDialog(this, "No hay contactos disponibles para agregar.", "Información", JOptionPane.INFORMATION_MESSAGE);
         return null;
     }

     String[] opciones = posiblesContactos.toArray(new String[0]);
     JComboBox<String> comboBox = new JComboBox<>(opciones);
     JTextField nicknameField = new JTextField();

     Object[] message = {
             "Seleccione un contacto para agregar:", comboBox,
             "Ingrese un nickname personal:", nicknameField
     };

     int option = JOptionPane.showConfirmDialog(
             this,
             message,
             "Agregar Contacto",
             JOptionPane.OK_CANCEL_OPTION,
             JOptionPane.PLAIN_MESSAGE
     );

     if (option == JOptionPane.OK_OPTION) {
         String seleccion = (String) comboBox.getSelectedItem();
         String nickname = nicknameField.getText().trim();

         if (seleccion != null) {
             ArrayList<String> respuesta = new ArrayList<>();
             respuesta.add(seleccion);
             if (nickname.isEmpty()) {
                 respuesta.add(seleccion);
             }else{
                 respuesta.add(nickname);
             }
             return respuesta;
         } else {
             JOptionPane.showMessageDialog(this, "Debe seleccionar un contacto y proporcionar un nickname.", "Error", JOptionPane.ERROR_MESSAGE);
         }
     }

     return null;
 }



 public void mostrarModalError(String s) {
     JOptionPane.showMessageDialog(this, s, "Error", JOptionPane.ERROR_MESSAGE);
 }

 public void mostrarModalExito(String contactoAgregadoExitosamente) {
     JOptionPane.showMessageDialog(this, contactoAgregadoExitosamente, "Éxito", JOptionPane.INFORMATION_MESSAGE);
 }
} 
