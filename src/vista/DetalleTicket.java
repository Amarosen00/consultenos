package vista;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import dao.HistorialDAO;
import dao.TicketDAO;
import modelo.HistorialTicket;
import modelo.Ticket;

/**
 * Pantalla de Detalle / Resolucion de Ticket (HU-03).
 *
 * Muestra todos los datos del ticket (TicketDAO.buscarPorId, con todos los
 * JOIN). Las acciones disponibles dependen del rol y del estado actual:
 *   - Tecnico, ticket "En progreso" y asignado a el: puede escribir el
 *     comentario de resolucion y marcarlo "Resuelto" (TicketDAO.resolver()).
 *   - Ticket "Resuelto" (cualquier rol que abra el detalle): boton "Cerrar"
 *     que lo pasa a "Cerrado" (TicketDAO.cambiarEstado(), sin tocar Historial_Ticket).
 *
 * El historial de eventos del ticket se muestra de solo lectura al final
 * (HistorialDAO.listarPorTicket) — es opcional segun el documento: esta
 * pantalla los MUESTRA, no los genera.
 */
public class DetalleTicket extends JFrame {

    private static final String ROL_TECNICO = "Tecnico";
    private static final String[] COLUMNAS_HISTORIAL = {"Fecha", "Responsable", "Accion"};

    private final TicketDAO    ticketDAO    = new TicketDAO();
    private final HistorialDAO historialDAO = new HistorialDAO();

    private final int    idTicket;
    private final int    idEmpleadoLogueado;
    private final String rolLogueado;

    private Ticket ticket;

    public DetalleTicket(int idTicket, int idEmpleadoLogueado, String rolLogueado) {
        this.idTicket = idTicket;
        this.idEmpleadoLogueado = idEmpleadoLogueado;
        this.rolLogueado = rolLogueado;

        setTitle("Consultenos - Detalle del Ticket #" + idTicket);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(650, 700);
        setLocationRelativeTo(null);

        cargarYMostrar();
    }

    /** Recarga el ticket desde la BD y reconstruye toda la pantalla (los datos y las acciones dependen del estado actual). */
    private void cargarYMostrar() {
        ticket = ticketDAO.buscarPorId(idTicket);

        getContentPane().removeAll();
        if (ticket == null) {
            JLabel lblError = new JLabel("No se encontro el ticket #" + idTicket + ".");
            lblError.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            add(lblError, BorderLayout.NORTH);
        } else {
            construirInterfaz();
        }
        revalidate();
        repaint();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(8, 8));

        JLabel lblTitulo = EstilosUI.titulo("Ticket #" + ticket.getIdTicket() + " - " + ticket.getNombreEstado(), 16f);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelDatos = new JPanel(new GridBagLayout());
        panelDatos.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        int fila = 0;

        String dispositivo = ticket.getTipoHardwareDispositivo() + " - " + ticket.getNumeroSerieDispositivo()
                + (ticket.getModeloDispositivo() != null ? " (" + ticket.getModeloDispositivo() + ")" : "");
        String tecnico = ticket.getNombreTecnico() != null ? ticket.getNombreTecnico() : "Sin asignar";
        String fechaCierre = ticket.getFechaHoraCierre() != null ? ticket.getFechaHoraCierre() : "-";
        String comentario = (ticket.getComentarioResolucion() != null && !ticket.getComentarioResolucion().isBlank())
                ? ticket.getComentarioResolucion() : "(sin comentario de resolucion)";

        fila = agregarFila(panelDatos, gbc, fila, "Empresa:", ticket.getRazonSocial());
        fila = agregarFila(panelDatos, gbc, fila, "Sucursal:", ticket.getNombreSucursal());
        fila = agregarFila(panelDatos, gbc, fila, "Reporta:", ticket.getCodigoUsuario() + " - " + ticket.getNombreUsuario());
        fila = agregarFila(panelDatos, gbc, fila, "Dispositivo:", dispositivo);
        fila = agregarFila(panelDatos, gbc, fila, "Ambito:", ticket.getNombreAmbito());
        fila = agregarFila(panelDatos, gbc, fila, "Naturaleza:", ticket.getNaturaleza());
        fila = agregarFila(panelDatos, gbc, fila, "Prioridad:", ticket.getPrioridad());
        fila = agregarFila(panelDatos, gbc, fila, "Lugar fisico:", ticket.getLugarFisicoExacto());
        fila = agregarFila(panelDatos, gbc, fila, "Registrado por:", ticket.getNombreAgenteCreador());
        fila = agregarFila(panelDatos, gbc, fila, "Atendido por:", tecnico);
        fila = agregarFila(panelDatos, gbc, fila, "Fecha creacion:", ticket.getFechaHoraCreacion());
        fila = agregarFila(panelDatos, gbc, fila, "Fecha cierre:", fechaCierre);

        fila = agregarFilaTextoLargo(panelDatos, gbc, fila, "Descripcion del problema:", ticket.getDescripcionProblema());
        fila = agregarFilaTextoLargo(panelDatos, gbc, fila, "Comentario de resolucion:", comentario);

        JScrollPane scrollDatos = new JScrollPane(panelDatos);
        add(scrollDatos, BorderLayout.CENTER);

        add(construirPanelAcciones(), BorderLayout.SOUTH);
    }

    private int agregarFila(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, String valor) {
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0;
        panel.add(new JLabel(etiqueta), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(new JLabel(valor != null ? valor : "-"), gbc);
        return fila + 1;
    }

    private int agregarFilaTextoLargo(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, String texto) {
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0;
        panel.add(new JLabel(etiqueta), gbc);

        JTextArea area = new JTextArea(texto, 3, 30);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(panel.getBackground());

        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH;
        panel.add(area, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return fila + 1;
    }

    /** Arma el panel de acciones (resolver/cerrar) e historial, segun el rol y el estado actual del ticket. */
    private JPanel construirPanelAcciones() {
        JPanel panelSur = new JPanel(new BorderLayout(5, 5));
        panelSur.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));

        boolean puedeResolver = ROL_TECNICO.equals(rolLogueado)
                && "En progreso".equals(ticket.getNombreEstado())
                && ticket.getIdTecnicoAsignado() != null
                && ticket.getIdTecnicoAsignado() == idEmpleadoLogueado;
        boolean puedeCerrar = "Resuelto".equals(ticket.getNombreEstado());

        if (puedeResolver) {
            JPanel panelResolver = new JPanel(new BorderLayout(5, 5));
            panelResolver.add(new JLabel("Comentario de resolucion:"), BorderLayout.NORTH);
            JTextArea txtComentario = new JTextArea(3, 30);
            txtComentario.setLineWrap(true);
            txtComentario.setWrapStyleWord(true);
            panelResolver.add(new JScrollPane(txtComentario), BorderLayout.CENTER);

            JButton btnResolver = EstilosUI.botonPrimario("Marcar como Resuelto");
            btnResolver.addActionListener(e -> resolver(txtComentario.getText().trim()));
            JPanel panelBoton = new JPanel();
            panelBoton.add(btnResolver);
            panelResolver.add(panelBoton, BorderLayout.SOUTH);

            panelSur.add(panelResolver, BorderLayout.NORTH);
        } else if (puedeCerrar) {
            JButton btnCerrar = EstilosUI.botonPrimario("Cerrar Ticket");
            btnCerrar.addActionListener(e -> cerrar());
            JPanel panelBoton = new JPanel();
            panelBoton.add(btnCerrar);
            panelSur.add(panelBoton, BorderLayout.NORTH);
        }

        panelSur.add(construirPanelHistorial(), BorderLayout.CENTER);
        return panelSur;
    }

    /** Historial de eventos del ticket, de solo lectura (opcional segun el documento). */
    private JPanel construirPanelHistorial() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Historial del ticket:"), BorderLayout.NORTH);

        DefaultTableModel modelo = new DefaultTableModel(COLUMNAS_HISTORIAL, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (HistorialTicket h : historialDAO.listarPorTicket(idTicket)) {
            modelo.addRow(new Object[]{
                h.getFechaHoraAccion(),
                h.getNombreResponsable() + " (" + h.getNombreRolResponsable() + ")",
                h.getAccion()
            });
        }

        JTable tablaHistorial = new JTable(modelo);
        tablaHistorial.setRowHeight(22);
        JScrollPane scroll = new JScrollPane(tablaHistorial);
        scroll.setPreferredSize(new java.awt.Dimension(600, 120));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void resolver(String comentario) {
        if (comentario.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El comentario de resolucion es obligatorio.",
                    "Falta el comentario", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = ticketDAO.resolver(idTicket, comentario);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Ticket #" + idTicket + " marcado como Resuelto.",
                    "Ticket resuelto", JOptionPane.INFORMATION_MESSAGE);
            cargarYMostrar();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo marcar el ticket como resuelto.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrar() {
        boolean ok = ticketDAO.cambiarEstado(idTicket, "Cerrado");
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Ticket #" + idTicket + " cerrado.",
                    "Ticket cerrado", JOptionPane.INFORMATION_MESSAGE);
            cargarYMostrar();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cerrar el ticket.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ======================================================================
    //  PRUEBA MANUAL
    //  Clic derecho en este archivo > Run File. Ticket 2 (En progreso,
    //  tecnico id 5) y rol "Tecnico" para ver el flujo de resolucion.
    // ======================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DetalleTicket(2, 5, "Tecnico").setVisible(true));
    }
}
