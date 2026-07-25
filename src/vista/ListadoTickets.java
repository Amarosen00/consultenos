package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import dao.CatalogoDAO;
import dao.TicketDAO;
import modelo.EstadoTicket;
import modelo.Sucursal;
import modelo.Ticket;

/**
 * Pantalla de Listado de Tickets (HU-08, HU-16, HU-06, HU-07).
 *
 * Sirve dos roles distintos de docs/IMPLEMENTACION.md con la MISMA clase:
 *   - Tecnico: se autofiltra a su propia bandeja (TicketDAO.listarPorTecnico),
 *     sin panel de filtros ("Mis Tickets Asignados" en el menu).
 *   - Agente/Supervisor/Administrador: listado general con filtros
 *     combinables por estado, prioridad, sucursal (HU-06) y numero de serie
 *     del dispositivo (HU-07) ("Ver Tickets" en el menu).
 *
 * Filtro mas especifico primero: si hay numero de serie o sucursal
 * seleccionada, se usa TicketDAO.listarPorDispositivo()/listarPorCliente()
 * (mas acotado en la BD) y luego estado/prioridad se refinan en memoria
 * sobre ese resultado ya chico. Si no hay ninguno de esos dos, se usa
 * TicketDAO.listar(filtroEstado, filtroPrioridad) directamente en la BD.
 * Rango de fechas (opcional en el documento) no se implemento, para no
 * sumar complejidad de UI a algo marcado como opcional.
 *
 * Doble clic en una fila abre DetalleTicket (HU-03); al cerrar ese detalle
 * se recarga el listado, por si el estado del ticket cambio.
 */
public class ListadoTickets extends JFrame {

    private static final String[] COLUMNAS = {
        "ID", "Fecha creacion", "Estado", "Prioridad", "Ambito", "Reporta", "Sucursal", "Tecnico"
    };
    private static final int COL_ESTADO    = 2;
    private static final int COL_PRIORIDAD = 3;

    private static final String ROL_TECNICO = "Tecnico";

    // Colores "chip" por prioridad.
    private static final Color COLOR_ALTA_FONDO  = new Color(0xFF, 0xCD, 0xD2);
    private static final Color COLOR_ALTA_TEXTO  = new Color(0xB7, 0x1C, 0x1C);
    private static final Color COLOR_MEDIA_FONDO = new Color(0xFF, 0xF9, 0xC4);
    private static final Color COLOR_MEDIA_TEXTO = new Color(0x8D, 0x6E, 0x00);
    private static final Color COLOR_BAJA_FONDO  = new Color(0xC8, 0xE6, 0xC9);
    private static final Color COLOR_BAJA_TEXTO  = new Color(0x1B, 0x5E, 0x20);

    // Colores "chip" por estado (los 4 del ciclo de vida completo).
    private static final Color COLOR_ABIERTO_FONDO     = new Color(0xFF, 0xE0, 0xB2);
    private static final Color COLOR_ABIERTO_TEXTO     = new Color(0xE6, 0x51, 0x00);
    private static final Color COLOR_EN_PROGRESO_FONDO = new Color(0xBB, 0xDE, 0xFB);
    private static final Color COLOR_EN_PROGRESO_TEXTO = new Color(0x0D, 0x47, 0xA1);
    private static final Color COLOR_RESUELTO_FONDO    = new Color(0xE1, 0xBE, 0xE7);
    private static final Color COLOR_RESUELTO_TEXTO    = new Color(0x4A, 0x14, 0x8C);
    private static final Color COLOR_CERRADO_FONDO     = new Color(0xE0, 0xE0, 0xE0);
    private static final Color COLOR_CERRADO_TEXTO     = new Color(0x42, 0x42, 0x42);

    private final TicketDAO    ticketDAO    = new TicketDAO();
    private final CatalogoDAO  catalogoDAO  = new CatalogoDAO();

    private final int idEmpleadoLogueado;
    private final String rolLogueado;
    private final boolean esTecnico;

    private List<Ticket> ticketsMostrados;

    // Un DetalleTicket abierto por id de ticket: si ya esta abierto, un
    // segundo doble clic lo enfoca en vez de abrir otro encima.
    private final Map<Integer, DetalleTicket> detallesAbiertos = new HashMap<>();

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JComboBox<EstadoTicket> comboEstado;
    private JComboBox<String>       comboPrioridad;
    private JComboBox<Sucursal>     comboSucursal;
    private JTextField              txtNumeroSerie;

    public ListadoTickets(int idEmpleadoLogueado, String rolLogueado) {
        this.idEmpleadoLogueado = idEmpleadoLogueado;
        this.rolLogueado = rolLogueado;
        this.esTecnico = ROL_TECNICO.equals(rolLogueado);

        setTitle(esTecnico ? "Consultenos - Mis Tickets Asignados" : "Consultenos - Listado de Tickets");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 520);
        setLocationRelativeTo(null);

        construirInterfaz();
        if (!esTecnico) {
            cargarCombos();
        }
        cargarTickets();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(8, 8));

        JPanel panelSuperior = new JPanel(new BorderLayout());

        JLabel lblTitulo = EstilosUI.titulo(
                esTecnico ? "Mis tickets asignados (Abierto / En progreso)" : "Listado de tickets", 16f);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panelSuperior.add(lblTitulo, BorderLayout.NORTH);

        if (!esTecnico) {
            JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

            comboEstado = new JComboBox<>();
            comboPrioridad = new JComboBox<>(new String[]{"Todas", "Alta", "Media", "Baja"});
            comboSucursal = new JComboBox<>();
            txtNumeroSerie = new JTextField(12);
            txtNumeroSerie.setToolTipText("Numero de serie del dispositivo (HU-07)");

            JButton btnBuscar = EstilosUI.botonPrimario("Buscar");
            btnBuscar.addActionListener(e -> cargarTickets());
            JButton btnLimpiar = EstilosUI.botonSecundario("Limpiar filtros");
            btnLimpiar.addActionListener(e -> limpiarFiltros());

            panelFiltros.add(new JLabel("Estado:"));
            panelFiltros.add(comboEstado);
            panelFiltros.add(new JLabel("Prioridad:"));
            panelFiltros.add(comboPrioridad);
            panelFiltros.add(new JLabel("Sucursal:"));
            panelFiltros.add(comboSucursal);
            panelFiltros.add(new JLabel("N. serie dispositivo:"));
            panelFiltros.add(txtNumeroSerie);
            panelFiltros.add(btnBuscar);
            panelFiltros.add(btnLimpiar);

            panelSuperior.add(panelFiltros, BorderLayout.CENTER);
        }

        add(panelSuperior, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // solo lectura; las acciones se hacen en DetalleTicket (doble clic)
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(COL_ESTADO).setCellRenderer(new RendererEstado());
        tabla.getColumnModel().getColumn(COL_PRIORIDAD).setCellRenderer(new RendererPrioridad());
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirDetalle();
                }
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton btnActualizar = EstilosUI.botonSecundario("Actualizar");
        btnActualizar.addActionListener(e -> cargarTickets());
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelInferior.add(new JLabel("Doble clic en una fila para ver el detalle."));
        panelInferior.add(btnActualizar);
        add(panelInferior, BorderLayout.SOUTH);
    }

    /** Llena los combos de filtro (estado y sucursal) con un item "Todos"/"Todas" al inicio. */
    private void cargarCombos() {
        comboEstado.removeAllItems();
        comboEstado.addItem(new EstadoTicket(0, "Todos"));
        for (EstadoTicket e : catalogoDAO.listarEstados()) {
            comboEstado.addItem(e);
        }

        comboSucursal.removeAllItems();
        comboSucursal.addItem(new Sucursal(0, "Todas"));
        for (Sucursal s : catalogoDAO.listarSucursales()) {
            comboSucursal.addItem(s);
        }
    }

    private void limpiarFiltros() {
        comboEstado.setSelectedIndex(0);
        comboPrioridad.setSelectedIndex(0);
        comboSucursal.setSelectedIndex(0);
        txtNumeroSerie.setText("");
        cargarTickets();
    }

    /** Trae los tickets (bandeja del tecnico, o resultado de los filtros) y repinta la tabla. */
    private void cargarTickets() {
        ticketsMostrados = esTecnico ? ticketDAO.listarPorTecnico(idEmpleadoLogueado) : buscarConFiltros();

        modeloTabla.setRowCount(0);
        for (Ticket t : ticketsMostrados) {
            modeloTabla.addRow(new Object[]{
                t.getIdTicket(),
                t.getFechaHoraCreacion(),
                t.getNombreEstado(),
                t.getPrioridad(),
                t.getNombreAmbito(),
                t.getNombreUsuario(),
                t.getNombreSucursal(),
                t.getNombreTecnico()
            });
        }
    }

    /** Aplica los filtros combinables: el mas especifico consulta la BD, el resto se refina en memoria. */
    private List<Ticket> buscarConFiltros() {
        EstadoTicket estadoSel = (EstadoTicket) comboEstado.getSelectedItem();
        String filtroEstado = (estadoSel == null || estadoSel.getIdEstado() == 0) ? null : estadoSel.getNombreEstado();

        String prioridadSel = (String) comboPrioridad.getSelectedItem();
        String filtroPrioridad = "Todas".equals(prioridadSel) ? null : prioridadSel;

        Sucursal sucursalSel = (Sucursal) comboSucursal.getSelectedItem();
        Integer filtroSucursal = (sucursalSel == null || sucursalSel.getIdSucursal() == 0) ? null : sucursalSel.getIdSucursal();

        String numeroSerie = txtNumeroSerie.getText().trim();

        List<Ticket> resultado;
        boolean refinarEnMemoria;

        if (!numeroSerie.isEmpty()) {
            resultado = ticketDAO.listarPorDispositivo(numeroSerie);
            refinarEnMemoria = true;
        } else if (filtroSucursal != null) {
            resultado = ticketDAO.listarPorCliente(filtroSucursal, null);
            refinarEnMemoria = true;
        } else {
            resultado = ticketDAO.listar(filtroEstado, filtroPrioridad);
            refinarEnMemoria = false; // listar() ya filtra en la BD
        }

        if (refinarEnMemoria) {
            if (filtroEstado != null) {
                resultado = resultado.stream()
                        .filter(t -> filtroEstado.equals(t.getNombreEstado()))
                        .collect(Collectors.toList());
            }
            if (filtroPrioridad != null) {
                resultado = resultado.stream()
                        .filter(t -> filtroPrioridad.equals(t.getPrioridad()))
                        .collect(Collectors.toList());
            }
        }

        return resultado;
    }

    /**
     * Abre el detalle del ticket seleccionado, o enfoca el que ya estuviera
     * abierto para ESE ticket; al cerrarlo, recarga el listado por si el
     * estado cambio. Tickets distintos si pueden tener su propia ventana
     * abierta al mismo tiempo (son "documentos" distintos).
     */
    private void abrirDetalle() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }

        Ticket seleccionado = ticketsMostrados.get(fila);
        int idTicket = seleccionado.getIdTicket();

        DetalleTicket existente = detallesAbiertos.get(idTicket);
        if (existente != null && existente.isDisplayable()) {
            existente.toFront();
            existente.requestFocus();
            return;
        }

        DetalleTicket detalle = new DetalleTicket(idTicket, idEmpleadoLogueado, rolLogueado);
        detalle.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                detallesAbiertos.remove(idTicket);
                cargarTickets();
            }
        });
        detallesAbiertos.put(idTicket, detalle);
        detalle.setVisible(true);
    }

    // ======================================================================
    //  RENDERERS DE COLOR (misma idea que ListadoActivosExtra, con los
    //  4 estados en vez de solo 2 porque aqui se ven todos)
    // ======================================================================
    private static class RendererPrioridad extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            switch (String.valueOf(value)) {
                case "Alta":
                    c.setBackground(COLOR_ALTA_FONDO); c.setForeground(COLOR_ALTA_TEXTO); break;
                case "Media":
                    c.setBackground(COLOR_MEDIA_FONDO); c.setForeground(COLOR_MEDIA_TEXTO); break;
                case "Baja":
                    c.setBackground(COLOR_BAJA_FONDO); c.setForeground(COLOR_BAJA_TEXTO); break;
                default:
                    c.setBackground(Color.WHITE); c.setForeground(Color.BLACK);
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    private static class RendererEstado extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            switch (String.valueOf(value)) {
                case "Abierto":
                    c.setBackground(COLOR_ABIERTO_FONDO); c.setForeground(COLOR_ABIERTO_TEXTO); break;
                case "En progreso":
                    c.setBackground(COLOR_EN_PROGRESO_FONDO); c.setForeground(COLOR_EN_PROGRESO_TEXTO); break;
                case "Resuelto":
                    c.setBackground(COLOR_RESUELTO_FONDO); c.setForeground(COLOR_RESUELTO_TEXTO); break;
                case "Cerrado":
                    c.setBackground(COLOR_CERRADO_FONDO); c.setForeground(COLOR_CERRADO_TEXTO); break;
                default:
                    c.setBackground(Color.WHITE); c.setForeground(Color.BLACK);
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    // ======================================================================
    //  PRUEBA MANUAL
    //  Clic derecho en este archivo > Run File. Prueba el modo general
    //  (supervisor, id 4). Para probar el modo Tecnico, cambiar el rol.
    // ======================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ListadoTickets(4, "Supervisor").setVisible(true));
    }
}
