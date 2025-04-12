package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class VistaLogin extends JFrame implements ILogin{

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtf_user;
	private JTextField txtf_port;
	private	JButton btn_inicio;

	/**
	 * Launch the application.
	 */
	/**
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VistaLogin frame = new VistaLogin();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
    */
	/**
	 * Create the frame.
	 */
	public VistaLogin() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_centro = new JPanel();
		contentPane.add(panel_centro, BorderLayout.CENTER);
		panel_centro.setLayout(new GridLayout(3, 2, 0, 0));
		
		JPanel panel_6 = new JPanel();
		panel_centro.add(panel_6);
		
		JLabel lblNewLabel = new JLabel("Usuario : ");
		panel_6.add(lblNewLabel);
		
		JPanel panel_1 = new JPanel();
		panel_centro.add(panel_1);
		
		this.txtf_user = new JTextField();
		panel_1.add(txtf_user);
		txtf_user.setColumns(10);
		
		JPanel panel_4 = new JPanel();
		panel_centro.add(panel_4);
		
		JLabel lblNewLabel_1 = new JLabel("Puerto : ");
		panel_4.add(lblNewLabel_1);
		
		JPanel panel_2 = new JPanel();
		panel_centro.add(panel_2);
		
		this.txtf_port = new JTextField();
		panel_2.add(txtf_port);
		txtf_port.setColumns(10);
		
		JPanel panel_5 = new JPanel();
		panel_centro.add(panel_5);
		
		JLabel lblNewLabel_2 = new JLabel("");
		panel_5.add(lblNewLabel_2);
		
		JPanel panel_3 = new JPanel();
		panel_centro.add(panel_3);
		
		this.btn_inicio = new JButton("Iniciar sesión");
		panel_3.add(btn_inicio);
		
		JPanel panel_norte = new JPanel();
		contentPane.add(panel_norte, BorderLayout.NORTH);
		
		JLabel lbl_title = new JLabel("Bienvenido/a!");
		panel_norte.add(lbl_title);

		setVisible(true);
		setLocationRelativeTo(null); // Center window
	}

	@Override
    public String getUser(){
		return this.txtf_user.getText();
	};
	@Override
    public String getPuerto(){
		return this.txtf_port.getText();
	};
	@Override
    public void setActionListener(ActionListener actionListener){
		this.btn_inicio.addActionListener(actionListener);
	};
	@Override
	public void setVisibleVentana(boolean estado){
		this.setVisible(estado);
	};

	public JButton getBotonInicio() {
		return this.btn_inicio;
	}

	public void limpiarcampos() {
		this.txtf_user.setText("");
		this.txtf_port.setText("");

	}

	public void mostrarModalError(String s) {
		javax.swing.JOptionPane.showMessageDialog(this, s, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
	}
}
