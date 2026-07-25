package vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;

/**
 * Utilidades visuales compartidas por todas las pantallas: paleta INACAP
 * (rojo institucional #E30514, navy #1F3864) y helpers para titulos y
 * botones consistentes. Evita repetir los mismos Color y estilos de boton
 * copiados en cada vista.
 *
 * Usa FlatLaf (lib/flatlaf-3.5.4.jar) para bordes redondeados via
 * putClientProperty("JButton.buttonType", "roundRect"); si FlatLaf no
 * esta activo como Look and Feel, Swing simplemente ignora esa propiedad
 * y el boton se ve como un JButton normal (no rompe nada).
 */
public final class EstilosUI {

    public static final Color ROJO_INACAP = new Color(0xE3, 0x05, 0x14);
    public static final Color NAVY_INACAP = new Color(0x1F, 0x38, 0x64);
    public static final Color FONDO       = new Color(0xF5, 0xF7, 0xFA);
    public static final Color TEXTO_SUAVE = new Color(0x66, 0x74, 0x84);
    public static final Color BORDE       = new Color(0xDD, 0xE4, 0xEC);

    private EstilosUI() {
    }

    /** Titulo de pantalla: negrita, navy institucional, tamano configurable. */
    public static JLabel titulo(String texto, float tamano) {
        JLabel label = new JLabel(texto);
        label.setForeground(NAVY_INACAP);
        label.setFont(label.getFont().deriveFont(Font.BOLD, tamano));
        return label;
    }

    /** Texto secundario (subtitulos, ayudas), en gris suave. */
    public static JLabel textoSuave(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(TEXTO_SUAVE);
        return label;
    }

    /** Boton de accion principal de la pantalla: rojo institucional, texto blanco. */
    public static JButton botonPrimario(String texto) {
        JButton boton = new JButton(texto);
        boton.setForeground(Color.WHITE);
        boton.setBackground(ROJO_INACAP);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.putClientProperty("JButton.buttonType", "roundRect");
        return boton;
    }

    /** Boton secundario (acciones de apoyo: limpiar, actualizar, cancelar). */
    public static JButton botonSecundario(String texto) {
        JButton boton = new JButton(texto);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.putClientProperty("JButton.buttonType", "roundRect");
        return boton;
    }

    /**
     * Ajustes globales de tema, para que el fondo gris claro y el color de
     * seleccion sean iguales en todas las pantallas sin tener que fijar el
     * background de cada JPanel una por una. Se llama una sola vez, justo
     * despues de FlatLightLaf.setup() en Login.main().
     */
    public static void configurarGlobal() {
        UIManager.put("Panel.background", FONDO);
        UIManager.put("Table.selectionBackground", new Color(0xEB, 0xF1, 0xFA));
        UIManager.put("Table.selectionForeground", NAVY_INACAP);
    }
}
