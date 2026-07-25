package vista;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import dao.CatalogoDAO;
import dao.DispositivoDAO;
import dao.TicketDAO;
import dao.UsuarioDAO;
import modelo.Ambito;
import modelo.Dispositivo;
import modelo.Ticket;
import modelo.Usuario;

/**
 * Pantalla de Registro de Ticket (HU-01, HU-02, HU-15).
 *
 * Flujo: el agente escribe el codigo de usuario que llamo y presiona Enter
 * o el boton "Buscar" (no se uso un FocusListener: como el resto del
 * formulario empieza deshabilitado, no hay ningun otro componente al que
 * Swing le pueda pasar el foco con Tab, asi que focusLost no es confiable
 * aqui). Al encontrar el usuario se autocompleta nombre, sucursal y empresa
 * (UsuarioDAO.buscarPorCodigo) y se cargan los dispositivos de esa sucursal
 * (DispositivoDAO.listarPorSucursal). El resto del formulario (ambito,
 * prioridad, naturaleza, lugar, descripcion) queda deshabilitado hasta
 * encontrar un usuario valido.
 *
 * Al registrar, TicketDAO.insertar() fija el estado inicial en "Abierto";
 * esta vista no decide el estado.
 */
public class RegistroTicket extends JFrame {

    private static final String[] PRIORIDADES  = {"Alta", "Media", "Baja"};
    private static final String[] NATURALEZAS  = {"Fisica", "Logica"};

    private final UsuarioDAO     usuarioDAO     = new UsuarioDAO();
    private final DispositivoDAO dispositivoDAO = new DispositivoDAO();
    private final CatalogoDAO    catalogoDAO    = new CatalogoDAO();
    private final TicketDAO      ticketDAO      = new TicketDAO();

    // Empleado que registra la llamada (agente, supervisor o administrador con sesion iniciada).
    private final int idEmpleadoLogueado;

    // Usuario cliente encontrado por codigo; null mientras no se autocomplete.
    private Usuario usuarioEncontrado;

    private JTextField txtCodigoUsuario;
    private JButton     btnBuscarUsuario;
    private JLabel      lblDatosUsuario;
    private JComboBox<Dispositivo> comboDispositivo;
    private JComboBox<Ambito>      comboAmbito;
    private JComboBox<String>      comboPrioridad;
    private JComboBox<String>      comboNaturaleza;
    private JTextField  txtLugarFisico;
    private JTextArea   txtDescripcion;
    private JButton      btnRegistrar;

    public RegistroTicket(int idEmpleadoLogueado) {
        this.idEmpleadoLogueado = idEmpleadoLogueado;

        setTitle("Consultenos - Registro de Ticket");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(560, 560);
        setLocationRelativeTo(null);

        construirInterfaz();
        cargarAmbitos();
        habilitarFormulario(false); // hasta que se encuentre un usuario valido
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(8, 8));

        JLabel lblTitulo = EstilosUI.titulo("Registro de Ticket (llamada telefonica)", 16f);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int fila = 0;

        txtCodigoUsuario = new JTextField();
        txtCodigoUsuario.addActionListener(e -> autocompletarUsuario()); // Enter tambien busca
        btnBuscarUsuario = EstilosUI.botonSecundario("Buscar");
        btnBuscarUsuario.addActionListener(e -> autocompletarUsuario());

        JPanel panelCodigo = new JPanel(new BorderLayout(5, 0));
        panelCodigo.add(txtCodigoUsuario, BorderLayout.CENTER);
        panelCodigo.add(btnBuscarUsuario, BorderLayout.EAST);
        agregarFila(panelFormulario, gbc, fila++, "Codigo de usuario:", panelCodigo);

        lblDatosUsuario = new JLabel(" ");
        lblDatosUsuario.setForeground(EstilosUI.ROJO_INACAP);
        gbc.gridx = 1; gbc.gridy = fila++; gbc.weightx = 1;
        panelFormulario.add(lblDatosUsuario, gbc);

        comboDispositivo = new JComboBox<>();
        agregarFila(panelFormulario, gbc, fila++, "Dispositivo afectado:", comboDispositivo);

        comboAmbito = new JComboBox<>();
        agregarFila(panelFormulario, gbc, fila++, "Ambito:", comboAmbito);

        comboPrioridad = new JComboBox<>(PRIORIDADES);
        agregarFila(panelFormulario, gbc, fila++, "Prioridad:", comboPrioridad);

        comboNaturaleza = new JComboBox<>(NATURALEZAS);
        agregarFila(panelFormulario, gbc, fila++, "Naturaleza:", comboNaturaleza);

        txtLugarFisico = new JTextField();
        agregarFila(panelFormulario, gbc, fila++, "Lugar fisico exacto:", txtLugarFisico);

        txtDescripcion = new JTextArea(4, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        panelFormulario.add(new JLabel("Descripcion del problema:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH;
        panelFormulario.add(new JScrollPane(txtDescripcion), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
        fila++;

        add(panelFormulario, BorderLayout.CENTER);

        btnRegistrar = EstilosUI.botonPrimario("Registrar Ticket");
        btnRegistrar.addActionListener(e -> registrar());
        JPanel panelInferior = new JPanel();
        panelInferior.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));
        panelInferior.add(btnRegistrar);
        add(panelInferior, BorderLayout.SOUTH);
    }

    /** Agrega una fila etiqueta+campo al formulario, avanzando la fila del GridBagConstraints. */
    private void agregarFila(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, java.awt.Component campo) {
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0;
        panel.add(new JLabel(etiqueta), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(campo, gbc);
    }

    private void cargarAmbitos() {
        comboAmbito.removeAllItems();
        for (Ambito a : catalogoDAO.listarAmbitos()) {
            comboAmbito.addItem(a);
        }
    }

    /** Busca el usuario por codigo y, si existe, carga sus dispositivos. Si no, limpia y bloquea el formulario. */
    private void autocompletarUsuario() {
        String codigo = txtCodigoUsuario.getText().trim();
        if (codigo.isEmpty()) {
            usuarioEncontrado = null;
            lblDatosUsuario.setText(" ");
            habilitarFormulario(false);
            return;
        }

        usuarioEncontrado = usuarioDAO.buscarPorCodigo(codigo);

        if (usuarioEncontrado == null) {
            lblDatosUsuario.setText("Codigo no encontrado.");
            comboDispositivo.removeAllItems();
            habilitarFormulario(false);
            return;
        }

        lblDatosUsuario.setForeground(EstilosUI.NAVY_INACAP);
        lblDatosUsuario.setText(usuarioEncontrado.getNombreCompleto()
                + " - " + usuarioEncontrado.getNombreSucursal()
                + " (" + usuarioEncontrado.getRazonSocial() + ")");

        comboDispositivo.removeAllItems();
        for (Dispositivo d : dispositivoDAO.listarPorSucursal(usuarioEncontrado.getIdSucursal())) {
            comboDispositivo.addItem(d);
        }

        habilitarFormulario(true);
    }

    private void habilitarFormulario(boolean habilitado) {
        comboDispositivo.setEnabled(habilitado);
        comboAmbito.setEnabled(habilitado);
        comboPrioridad.setEnabled(habilitado);
        comboNaturaleza.setEnabled(habilitado);
        txtLugarFisico.setEnabled(habilitado);
        txtDescripcion.setEnabled(habilitado);
        btnRegistrar.setEnabled(habilitado);
    }

    private void registrar() {
        if (usuarioEncontrado == null) {
            JOptionPane.showMessageDialog(this,
                    "Busque un usuario valido antes de registrar.",
                    "Falta el usuario", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (comboDispositivo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Esa sucursal no tiene dispositivos registrados; no se puede continuar.",
                    "Sin dispositivos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String lugar = txtLugarFisico.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        if (lugar.isEmpty() || descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Complete el lugar fisico y la descripcion del problema.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Dispositivo dispositivo = (Dispositivo) comboDispositivo.getSelectedItem();
        Ambito ambito = (Ambito) comboAmbito.getSelectedItem();

        Ticket t = new Ticket();
        t.setIdUsuario(usuarioEncontrado.getIdUsuario());
        t.setIdDispositivo(dispositivo.getIdDispositivo());
        t.setIdAmbito(ambito.getIdAmbito());
        t.setIdAgenteCreador(idEmpleadoLogueado);
        t.setPrioridad((String) comboPrioridad.getSelectedItem());
        t.setNaturaleza((String) comboNaturaleza.getSelectedItem());
        t.setLugarFisicoExacto(lugar);
        t.setDescripcionProblema(descripcion);

        int idGenerado = ticketDAO.insertar(t);

        if (idGenerado > 0) {
            JOptionPane.showMessageDialog(this,
                    "Ticket #" + idGenerado + " registrado con estado Abierto.",
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo registrar el ticket. Revise la consola para mas detalle.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Deja el formulario listo para registrar la siguiente llamada. */
    private void limpiarFormulario() {
        txtCodigoUsuario.setText("");
        lblDatosUsuario.setText(" ");
        usuarioEncontrado = null;
        comboDispositivo.removeAllItems();
        comboAmbito.setSelectedIndex(0);
        comboPrioridad.setSelectedIndex(0);
        comboNaturaleza.setSelectedIndex(0);
        txtLugarFisico.setText("");
        txtDescripcion.setText("");
        habilitarFormulario(false);
        txtCodigoUsuario.requestFocusInWindow();
    }

    // ======================================================================
    //  PRUEBA MANUAL
    //  Clic derecho en este archivo > Run File. Abre la pantalla con un
    //  empleado de prueba (id 2 = agente1 de los datos de ejemplo del
    //  script SQL) hasta que se pruebe desde el Login/MenuPrincipal real.
    // ======================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistroTicket(2).setVisible(true));
    }
}
