package vista;

import java.awt.*;
import javax.swing.border.Border;
import javax.swing.JButton;

public class RoundedBorder implements Border {
    private int radius;

    public RoundedBorder(int radius) {
        this.radius = radius;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Suaviza el borde

        // Si el componente es un botón, tomamos su color de fondo
        if (c instanceof JButton) {
            JButton button = (JButton) c;
            g2d.setColor(button.getBackground());
        } else {
            g2d.setColor(Color.GRAY); // Color por defecto si no es botón
        }

        g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }
}


