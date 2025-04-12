package vista;

import javax.swing.*;
import java.awt.event.ActionListener;

public interface ILogin {
    String getUser();
    String getPuerto();
    void setActionListener(ActionListener actionListener);
    public void setVisibleVentana(boolean estado);
    public void limpiarcampos();
    public JButton getBotonInicio();
}
