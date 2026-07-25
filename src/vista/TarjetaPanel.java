package vista;

import java.awt.Color;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Panel blanco reutilizable con borde suave, para agrupar informacion
 * (tarjetas de resumen, secciones de un formulario). El "arc" es una
 * propiedad de FlatLaf para las esquinas redondeadas; si FlatLaf no esta
 * activo, Swing la ignora y el panel se ve como un JPanel normal.
 */
public class TarjetaPanel extends JPanel {

    public TarjetaPanel(LayoutManager layout) {
        super(layout);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstilosUI.BORDE),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        putClientProperty("FlatLaf.style", "arc: 14");
    }
}
