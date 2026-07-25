package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import dao.EmpleadoDAO;
import dao.TicketDAO;
import modelo.Empleado;
import modelo.Ticket;

/**
 * Pantalla de Asignacion de Tecnicos (HU-04, HU-05).
 *
 * Lista los tickets Abierto y sin tecnico (TicketDAO.listarAbiertosSinAsignar).
 * Al seleccionar uno, el combo se llena con los tecnicos del ambito de ESE
 * ticket (EmpleadoDAO.listarTecnicosPorAmbito, ya existia). Al asignar,
 * TicketDAO.asignarTecnico() pasa el ticket a "En progreso".
 *
 * Nota HU-05: una app de escritorio no tiene notificacion push real. La
 * "notificacion" al tecnico es que el ticket recien asignado aparece en su
 * bandeja "Mis Tickets Asignados" (ListadoTickets en modo Tecnico) la
 * proxima vez que la abra. No se envia correo ni aviso emergente.
 */
public class AsignacionTicket extends JFrame {

    private static final String[] COLUMNAS = {
        "ID", "Fecha creacion", "Prioridad", "Ambito", "Reporta", "Sucursal", "Descripcion"
    };
    private static final int COL_PRIORIDAD = 2;

    private static final Color COLOR_ALTA_FONDO  = new Color(0xFF, 0xCD, 0xD2);
    private static final Color COLOR_ALTA_TEXTO  = new Color(0xB7, 0x1C, 0x1C);
    private static final Color COLOR_MEDIA_FONDO = new Color(0xFF, 0xF9, 0xC4);
    private static final Color COLOR_MEDIA_TEXTO = new Color(0x8D, 0x6E, 0x00);
    private static final Color COLOR_BAJA_FONDO  = new Color(0xC8, 0xE6, 0xC9);
    private static final Color COLOR_BAJA_TEXTO  = new Color(0x1B, 0x5E, 0x20);
    private static final Color COLOR_NAVY_INACAP = new Color(0x1F, 0x38, 0x64);

    private final TicketDAO   ticketDAO   = new TicketDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    private List<Ticket> ticketsMostrados;

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JComboBox<Empleado> comboTecnico;
    private JButton btnAsignar;
    private JButton btnActualizar;
    private JLabel  lblSeleccion;

    public AsignacionTicket() {
        setTitle("Consultenos - Asignacion de Tecnicos");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(950, 500);
        setLocationRelativeTo(null);

        construirInterfaz();
        cargarTickets();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(8, 8));

        JLabel lblTitulo = new JLabel("Tickets abiertos sin asignar");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));
        lblTitulo.setForeground(COLOR_NAVY_INACAP);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(lblTitulo, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // solo lectura; la asignacion se hace con el combo de abajo
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(24);
        tabla.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(COL_PRIORIDAD).setCellRenderer(new RendererPrioridad());
        tabla.getSelectionModel().addListSelectionListener(this::alSeleccionarFila);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        lblSeleccion = new JLabel("Seleccione un ticket de la tabla para asignarle un tecnico.");
        comboTecnico = new JComboBox<>();
        comboTecnico.setEnabled(false);
        btnAsignar = new JButton("Asignar Tecnico");
        btnAsignar.setEnabled(false);
        btnAsignar.addActionListener(e -> asignar());
        btnActualizar = new JButton("Actualizar listado");
        btnActualizar.addActionListener(e -> cargarTickets());

        panelInferior.add(lblSeleccion);
        panelInferior.add(new JLabel("Tecnico:"));
        panelInferior.add(comboTecnico);
        panelInferior.add(btnAsignar);
        panelInferior.add(btnActualizar);
        add(panelInferior, BorderLayout.SOUTH);
    }

    /** Trae los tickets Abierto sin tecnico y repinta la tabla completa. */
    private void cargarTickets() {
        ticketsMostrados = ticketDAO.listarAbiertosSinAsignar();

        modeloTabla.setRowCount(0);
        for (Ticket t : ticketsMostrados) {
            modeloTabla.addRow(new Object[]{
                t.getIdTicket(),
                t.getFechaHoraCreacion(),
                t.getPrioridad(),
                t.getNombreAmbito(),
                t.getNombreUsuario(),
                t.getNombreSucursal(),
                t.getDescripcionProblema()
            });
        }

        limpiarSeleccion();
    }

    private void alSeleccionarFila(ListSelectionEvent evt) {
        if (evt.getValueIsAdjusting()) {
            return;
        }

        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            limpiarSeleccion();
            return;
        }

        Ticket seleccionado = ticketsMostrados.get(fila);

        comboTecnico.removeAllItems();
        for (Empleado tecnico : empleadoDAO.listarTecnicosPorAmbito(seleccionado.getIdAmbito())) {
            comboTecnico.addItem(tecnico);
        }

        if (comboTecnico.getItemCount() == 0) {
            lblSeleccion.setText("Ticket #" + seleccionado.getIdTicket()
                    + " (" + seleccionado.getNombreAmbito() + ") - no hay tecnicos disponibles para este ambito.");
            comboTecnico.setEnabled(false);
            btnAsignar.setEnabled(false);
            return;
        }

        lblSeleccion.setText("Ticket #" + seleccionado.getIdTicket()
                + " (" + seleccionado.getNombreAmbito() + ") - elija un tecnico:");
        comboTecnico.setEnabled(true);
        btnAsignar.setEnabled(true);
    }

    private void limpiarSeleccion() {
        lblSeleccion.setText("Seleccione un ticket de la tabla para asignarle un tecnico.");
        comboTecnico.removeAllItems();
        comboTecnico.setEnabled(false);
        btnAsignar.setEnabled(false);
    }

    private void asignar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return; // no deberia pasar: el boton esta deshabilitado sin seleccion
        }

        Ticket seleccionado = ticketsMostrados.get(fila);
        Empleado tecnico = (Empleado) comboTecnico.getSelectedItem();
        if (tecnico == null) {
            return;
        }

        boolean ok = ticketDAO.asignarTecnico(seleccionado.getIdTicket(), tecnico.getIdEmpleado());

        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Ticket #" + seleccionado.getIdTicket() + " asignado a " + tecnico.getNombreCompleto()
                            + ". Pasa a estado 'En progreso'.\n"
                            + "El tecnico lo vera en su bandeja 'Mis Tickets Asignados' la proxima vez que la abra.",
                    "Ticket asignado", JOptionPane.INFORMATION_MESSAGE);
            cargarTickets(); // el ticket ya no aparece: dejo de estar "sin asignar"
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo asignar el tecnico. Revise la consola para mas detalle.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ======================================================================
    //  RENDERER DE COLOR POR PRIORIDAD (misma idea que en las demas pantallas)
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

    // ======================================================================
    //  PRUEBA MANUAL
    //  Clic derecho en este archivo > Run File.
    // ======================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AsignacionTicket().setVisible(true));
    }
}
