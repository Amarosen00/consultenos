package vista;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import dao.ReporteDAO;
import modelo.CargaTecnico;
import modelo.ConteoPorAmbito;
import modelo.ConteoPorEstado;
import modelo.ResumenMTTR;

/**
 * Pantalla de Reportes de Gestion (HU-11). Cuatro reportes en pestañas,
 * todos de solo lectura, usando las consultas ya escritas y probadas en
 * sql/02_consultas_consultenos.sql (seccion HU-11) a traves de ReporteDAO.
 */
public class Reportes extends JFrame implements Refrescable {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    private DefaultTableModel modeloEstado;
    private DefaultTableModel modeloTecnico;
    private DefaultTableModel modeloAmbito;
    private JLabel lblHorasPromedio;
    private JLabel lblTicketsConsiderados;

    public Reportes() {
        setTitle("Consultenos - Reportes de Gestion");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        construirInterfaz();
        cargarTodo();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(8, 8));

        JLabel lblTitulo = EstilosUI.titulo("Reportes de gestion", 16f);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(lblTitulo, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Por Estado", construirTabEstado());
        tabs.addTab("MTTR", construirTabMTTR());
        tabs.addTab("Por Tecnico", construirTabTecnico());
        tabs.addTab("Por Ambito", construirTabAmbito());
        add(tabs, BorderLayout.CENTER);

        JButton btnActualizar = EstilosUI.botonSecundario("Actualizar reportes");
        btnActualizar.addActionListener(e -> cargarTodo());
        JPanel panelInferior = new JPanel();
        panelInferior.add(btnActualizar);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel construirTabEstado() {
        modeloEstado = new DefaultTableModel(new String[]{"Estado", "Cantidad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        return envolverEnPanel(new JTable(modeloEstado));
    }

    private JPanel construirTabTecnico() {
        modeloTecnico = new DefaultTableModel(new String[]{"Tecnico", "Grupo", "Asignados", "Resueltos"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        return envolverEnPanel(new JTable(modeloTecnico));
    }

    private JPanel construirTabAmbito() {
        modeloAmbito = new DefaultTableModel(new String[]{"Ambito", "Total tickets"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        return envolverEnPanel(new JTable(modeloAmbito));
    }

    private JPanel construirTabMTTR() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        lblHorasPromedio = new JLabel("-", SwingConstants.CENTER);
        lblHorasPromedio.setFont(lblHorasPromedio.getFont().deriveFont(Font.BOLD, 28f));
        lblHorasPromedio.setForeground(EstilosUI.NAVY_INACAP);

        lblTicketsConsiderados = new JLabel("-", SwingConstants.CENTER);

        JPanel panelHoras = new JPanel(new BorderLayout());
        panelHoras.add(new JLabel("Horas promedio de resolucion (MTTR)", SwingConstants.CENTER), BorderLayout.NORTH);
        panelHoras.add(lblHorasPromedio, BorderLayout.CENTER);

        JPanel panelCantidad = new JPanel(new BorderLayout());
        panelCantidad.add(new JLabel("Tickets considerados (con fecha de cierre)", SwingConstants.CENTER), BorderLayout.NORTH);
        panelCantidad.add(lblTicketsConsiderados, BorderLayout.CENTER);

        panel.add(panelHoras);
        panel.add(panelCantidad);
        return panel;
    }

    private JPanel envolverEnPanel(JTable tabla) {
        tabla.setRowHeight(24);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    /** Recarga los datos cuando se reutiliza esta ventana desde el menu (ver Refrescable). */
    @Override
    public void refrescar() {
        cargarTodo();
    }

    /** Vuelve a ejecutar las 4 consultas y repinta cada pestaña. */
    private void cargarTodo() {
        modeloEstado.setRowCount(0);
        for (ConteoPorEstado c : reporteDAO.contarPorEstado()) {
            modeloEstado.addRow(new Object[]{c.getNombreEstado(), c.getCantidad()});
        }

        modeloTecnico.setRowCount(0);
        for (CargaTecnico c : reporteDAO.cargaPorTecnico()) {
            modeloTecnico.addRow(new Object[]{
                c.getNombreTecnico(),
                c.getNombreGrupo() != null ? c.getNombreGrupo() : "Sin grupo",
                c.getTicketsAsignados(),
                c.getResueltos()
            });
        }

        modeloAmbito.setRowCount(0);
        for (ConteoPorAmbito c : reporteDAO.contarPorAmbito()) {
            modeloAmbito.addRow(new Object[]{c.getNombreAmbito(), c.getTotal()});
        }

        ResumenMTTR mttr = reporteDAO.calcularMTTR();
        lblHorasPromedio.setText(mttr.getHorasPromedio() != null
                ? mttr.getHorasPromedio() + " hrs" : "Sin datos aun");
        lblTicketsConsiderados.setText(String.valueOf(mttr.getTicketsConsiderados()));
    }

    // ======================================================================
    //  PRUEBA MANUAL
    //  Clic derecho en este archivo > Run File.
    // ======================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Reportes().setVisible(true));
    }
}
