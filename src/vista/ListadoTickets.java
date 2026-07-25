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
import dao.CatalogoDAO;
import dao.TicketDAO;
import modelo.EstadoTicket;
import modelo.Ticket;

/**
 * Pantalla de Listado de Tickets Activos (HU-16).
 *
 * Muestra los tickets en estado Abierto o En progreso, con distintivos de
 * color por prioridad y por estado. Permite cambiar el estado de un ticket
 * seleccionado sin salir de la pantalla (usa TicketDAO.cambiarEstado(), que
 * ademas deja constancia del cambio en Historial_Ticket).
 *
 * La vista NO sabe SQL: solo llama a TicketDAO y pinta lo que recibe.
 */
public class ListadoTickets extends JFrame {

    private static final String[] COLUMNAS = {
        "ID", "Reporta", "Ambito", "Prioridad", "Estado", "Tecnico", "Fecha creacion"
    };
    private static final int COL_PRIORIDAD = 3;
    private static final int COL_ESTADO    = 4;

    // Colores "chip" por prioridad: fondo suave + texto mas oscuro del mismo tono.
    private static final Color COLOR_ALTA_FONDO  = new Color(0xFF, 0xCD, 0xD2);
    private static final Color COLOR_ALTA_TEXTO  = new Color(0xB7, 0x1C, 0x1C);
    private static final Color COLOR_MEDIA_FONDO = new Color(0xFF, 0xF9, 0xC4);
    private static final Color COLOR_MEDIA_TEXTO = new Color(0x8D, 0x6E, 0x00);
    private static final Color COLOR_BAJA_FONDO  = new Color(0xC8, 0xE6, 0xC9);
    private static final Color COLOR_BAJA_TEXTO  = new Color(0x1B, 0x5E, 0x20);

    // Colores "chip" por estado (solo Abierto/En progreso aparecen en esta pantalla).
    private static final Color COLOR_ABIERTO_FONDO     = new Color(0xFF, 0xE0, 0xB2);
    private static final Color COLOR_ABIERTO_TEXTO     = new Color(0xE6, 0x51, 0x00);
    private static final Color COLOR_EN_PROGRESO_FONDO = new Color(0xBB, 0xDE, 0xFB);
    private static final Color COLOR_EN_PROGRESO_TEXTO = new Color(0x0D, 0x47, 0xA1);

    private static final Color COLOR_NAVY_INACAP = new Color(0x1F, 0x38, 0x64);

    private final TicketDAO ticketDAO = new TicketDAO();
    private final CatalogoDAO catalogoDAO = new CatalogoDAO(); // solo se usa listarEstados(), vive alli para no duplicar la consulta

    // Empleado con sesion iniciada. Se recibe por constructor porque
    // Login.java todavia no existe; cuando exista, el menu principal
    // pasara aqui el id real del empleado logueado.
    private final int idEmpleadoLogueado;

    // Guarda los mismos tickets que estan en la tabla, en el mismo orden de
    // filas, para poder recuperar el Ticket completo al seleccionar una fila.
    private List<Ticket> ticketsMostrados;

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JComboBox<EstadoTicket> comboEstado;
    private JButton btnAplicar;
    private JButton btnActualizar;
    private JLabel lblSeleccion;

    public ListadoTickets(int idEmpleadoLogueado) {
        this.idEmpleadoLogueado = idEmpleadoLogueado;

        setTitle("Consultenos - Tickets Activos");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(950, 500);
        setLocationRelativeTo(null);

        construirInterfaz();
        cargarEstados();
        cargarTickets();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(8, 8));

        JLabel lblTitulo = new JLabel("Tickets activos (Abierto / En progreso)");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));
        lblTitulo.setForeground(COLOR_NAVY_INACAP);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(lblTitulo, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // la tabla es solo de lectura; el cambio de estado se hace
                // con el combo y el boton de la parte inferior
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(24);
        tabla.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(COL_PRIORIDAD).setCellRenderer(new RendererPrioridad());
        tabla.getColumnModel().getColumn(COL_ESTADO).setCellRenderer(new RendererEstado());
        tabla.getSelectionModel().addListSelectionListener(this::alSeleccionarFila);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        lblSeleccion = new JLabel("Seleccione un ticket de la tabla para cambiar su estado.");
        comboEstado = new JComboBox<>();
        comboEstado.setEnabled(false);
        btnAplicar = new JButton("Aplicar cambio de estado");
        btnAplicar.setEnabled(false);
        btnAplicar.addActionListener(e -> aplicarCambioEstado());
        btnActualizar = new JButton("Actualizar listado");
        btnActualizar.addActionListener(e -> cargarTickets());

        panelInferior.add(lblSeleccion);
        panelInferior.add(new JLabel("Nuevo estado:"));
        panelInferior.add(comboEstado);
        panelInferior.add(btnAplicar);
        panelInferior.add(btnActualizar);
        add(panelInferior, BorderLayout.SOUTH);
    }

    /** Llena el combo de estados una sola vez con el catalogo Estado_Ticket. */
    private void cargarEstados() {
        comboEstado.removeAllItems();
        for (EstadoTicket e : catalogoDAO.listarEstados()) {
            comboEstado.addItem(e);
        }
    }

    /** Trae los tickets activos desde el DAO y repinta la tabla completa. */
    private void cargarTickets() {
        ticketsMostrados = ticketDAO.listarActivos();

        modeloTabla.setRowCount(0); // limpia filas anteriores antes de recargar
        for (Ticket t : ticketsMostrados) {
            modeloTabla.addRow(new Object[]{
                t.getIdTicket(),
                t.getNombreUsuario(),
                t.getNombreAmbito(),
                t.getPrioridad(),
                t.getNombreEstado(),
                t.getNombreTecnico(),
                t.getFechaHoraCreacion()
            });
        }

        // al recargar se pierde la seleccion previa de la tabla
        limpiarSeleccion();
    }

    private void alSeleccionarFila(ListSelectionEvent evt) {
        if (evt.getValueIsAdjusting()) {
            return; // Swing dispara el evento dos veces por cada clic; solo interesa el final
        }

        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            limpiarSeleccion();
            return;
        }

        Ticket seleccionado = ticketsMostrados.get(fila);
        lblSeleccion.setText("Ticket #" + seleccionado.getIdTicket()
                + " - estado actual: " + seleccionado.getNombreEstado());
        comboEstado.setEnabled(true);
        btnAplicar.setEnabled(true);

        // el combo parte mostrando el estado actual del ticket, para que el
        // supervisor vea claramente cual es el cambio que va a aplicar
        for (int i = 0; i < comboEstado.getItemCount(); i++) {
            if (comboEstado.getItemAt(i).getNombreEstado().equals(seleccionado.getNombreEstado())) {
                comboEstado.setSelectedIndex(i);
                break;
            }
        }
    }

    private void limpiarSeleccion() {
        lblSeleccion.setText("Seleccione un ticket de la tabla para cambiar su estado.");
        comboEstado.setEnabled(false);
        btnAplicar.setEnabled(false);
    }

    private void aplicarCambioEstado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return; // no deberia pasar: el boton esta deshabilitado sin seleccion
        }

        Ticket seleccionado = ticketsMostrados.get(fila);
        EstadoTicket nuevoEstado = (EstadoTicket) comboEstado.getSelectedItem();

        if (nuevoEstado.getNombreEstado().equals(seleccionado.getNombreEstado())) {
            JOptionPane.showMessageDialog(this,
                    "El ticket ya esta en ese estado.",
                    "Sin cambios", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean ok = ticketDAO.cambiarEstado(seleccionado.getIdTicket(), nuevoEstado, idEmpleadoLogueado);

        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Ticket #" + seleccionado.getIdTicket() + " actualizado a: " + nuevoEstado.getNombreEstado(),
                    "Estado actualizado", JOptionPane.INFORMATION_MESSAGE);
            // si el nuevo estado no es Abierto/En progreso, el ticket sale del listado al recargar
            cargarTickets();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo actualizar el estado. Revise la consola para mas detalle.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ======================================================================
    //  RENDERERS DE COLOR
    //  Pintan la celda segun el texto que muestran (prioridad o estado), para
    //  que el supervisor identifique urgencias de un vistazo en la tabla.
    // ======================================================================
    private static class RendererPrioridad extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            switch (String.valueOf(value)) {
                case "Alta":
                    c.setBackground(COLOR_ALTA_FONDO);
                    c.setForeground(COLOR_ALTA_TEXTO);
                    break;
                case "Media":
                    c.setBackground(COLOR_MEDIA_FONDO);
                    c.setForeground(COLOR_MEDIA_TEXTO);
                    break;
                case "Baja":
                    c.setBackground(COLOR_BAJA_FONDO);
                    c.setForeground(COLOR_BAJA_TEXTO);
                    break;
                default:
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
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
            String estado = String.valueOf(value);
            if ("Abierto".equals(estado)) {
                c.setBackground(COLOR_ABIERTO_FONDO);
                c.setForeground(COLOR_ABIERTO_TEXTO);
            } else if ("En progreso".equals(estado)) {
                c.setBackground(COLOR_EN_PROGRESO_FONDO);
                c.setForeground(COLOR_EN_PROGRESO_TEXTO);
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    // ======================================================================
    //  PRUEBA MANUAL
    //  Clic derecho en este archivo > Run File. Abre la pantalla con un
    //  empleado de prueba (id 4 = supervisor de los datos de ejemplo del
    //  script SQL) hasta que exista Login.java.
    // ======================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ListadoTickets(4).setVisible(true));
    }
}
