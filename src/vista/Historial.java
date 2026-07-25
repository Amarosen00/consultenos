package vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import dao.CatalogoDAO;
import dao.HistorialDAO;
import modelo.EstadoTicket;
import modelo.HistorialTicket;

/**
 * Pantalla de Historial y Busqueda (HU-06/07/08 aplicadas sobre Historial_Ticket).
 *
 * Filtros: texto libre (busca en Historial_Ticket.accion), estado ACTUAL del
 * ticket, y rango de fechas sobre fecha_hora_accion. Resultado ordenado de
 * mas reciente a mas antiguo (ya viene asi desde HistorialDAO.buscar()).
 *
 * La vista NO sabe SQL: solo llama a HistorialDAO/TicketDAO y pinta lo que recibe.
 */
public class Historial extends JFrame {

    private static final String[] COLUMNAS = {
        "Fecha y hora", "Ticket", "Accion", "Responsable", "Estado actual"
    };
    private static final String OPCION_TODOS = "Todos";

    private final HistorialDAO historialDAO = new HistorialDAO();
    private final CatalogoDAO  catalogoDAO  = new CatalogoDAO(); // solo se usa listarEstados(), para no duplicar esa consulta

    private JTextField txtTexto;
    private JComboBox<String> comboEstado;
    private JTextField txtDesde;
    private JTextField txtHasta;
    private JLabel lblResultado;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public Historial() {
        setTitle("Consultenos - Historial y Busqueda");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(950, 500);
        setLocationRelativeTo(null);

        construirInterfaz();
        cargarEstados();
        buscar(); // carga inicial sin filtros: todo el historial
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(8, 8));

        JLabel lblTitulo = EstilosUI.titulo("Historial y busqueda", 16f);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        txtTexto = new JTextField(18);
        comboEstado = new JComboBox<>();
        txtDesde = new JTextField(10);
        txtHasta = new JTextField(10);
        txtDesde.setToolTipText("Formato: yyyy-MM-dd (ejemplo: 2026-07-01)");
        txtHasta.setToolTipText("Formato: yyyy-MM-dd (ejemplo: 2026-07-31)");

        JButton btnBuscar = EstilosUI.botonPrimario("Buscar");
        btnBuscar.addActionListener(e -> buscar());
        JButton btnLimpiar = EstilosUI.botonSecundario("Limpiar filtros");
        btnLimpiar.addActionListener(e -> limpiarFiltros());

        panelFiltros.add(new JLabel("Texto:"));
        panelFiltros.add(txtTexto);
        panelFiltros.add(new JLabel("Estado:"));
        panelFiltros.add(comboEstado);
        panelFiltros.add(new JLabel("Desde:"));
        panelFiltros.add(txtDesde);
        panelFiltros.add(new JLabel("Hasta:"));
        panelFiltros.add(txtHasta);
        panelFiltros.add(btnBuscar);
        panelFiltros.add(btnLimpiar);
        add(panelFiltros, BorderLayout.PAGE_START);

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // pantalla de solo consulta
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(50);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        lblResultado = new JLabel(" ");
        lblResultado.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(lblResultado, BorderLayout.SOUTH);
    }

    /** Llena el combo de estados con el catalogo Estado_Ticket, mas la opcion "Todos". */
    private void cargarEstados() {
        comboEstado.removeAllItems();
        comboEstado.addItem(OPCION_TODOS);
        for (EstadoTicket e : catalogoDAO.listarEstados()) {
            comboEstado.addItem(e.getNombreEstado());
        }
    }

    private void buscar() {
        String texto = txtTexto.getText().trim();
        String estadoSeleccionado = (String) comboEstado.getSelectedItem();
        String nombreEstado = OPCION_TODOS.equals(estadoSeleccionado) ? null : estadoSeleccionado;
        String fechaDesde = txtDesde.getText().trim();
        String fechaHasta = txtHasta.getText().trim();

        if (!fechaValida(fechaDesde) || !fechaValida(fechaHasta)) {
            JOptionPane.showMessageDialog(this,
                    "Las fechas deben tener el formato yyyy-MM-dd (ejemplo: 2026-07-24).",
                    "Formato de fecha invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<HistorialTicket> resultados = historialDAO.buscar(texto, nombreEstado, fechaDesde, fechaHasta);
        llenarTabla(resultados);
        lblResultado.setText(resultados.size() + " resultado(s) encontrado(s).");
    }

    /** Acepta vacio (sin filtro) o el formato yyyy-MM-dd. */
    private boolean fechaValida(String fecha) {
        return fecha.isEmpty() || fecha.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private void llenarTabla(List<HistorialTicket> lista) {
        modeloTabla.setRowCount(0);
        for (HistorialTicket h : lista) {
            modeloTabla.addRow(new Object[]{
                h.getFechaHoraAccion(),
                h.getIdTicket(),
                h.getAccion(),
                h.getNombreResponsable() + " (" + h.getNombreRolResponsable() + ")",
                h.getNombreEstadoActual()
            });
        }
    }

    private void limpiarFiltros() {
        txtTexto.setText("");
        comboEstado.setSelectedIndex(0);
        txtDesde.setText("");
        txtHasta.setText("");
        buscar();
    }

    // ======================================================================
    //  PRUEBA MANUAL
    //  Clic derecho en este archivo > Run File.
    // ======================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Historial().setVisible(true));
    }
}
