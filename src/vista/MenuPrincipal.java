package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import dao.ReporteDAO;
import modelo.ConteoPorEstado;
import modelo.Empleado;

/**
 * Menu principal: se abre despues de un login correcto. Los botones
 * disponibles cambian segun el rol del empleado, tal como lo pide HU-12
 * en docs/IMPLEMENTACION.md:
 *   - Agente:        Registrar Ticket, Ver Tickets.
 *   - Supervisor:     lo anterior + Asignacion, Reportes.
 *   - Tecnico:        Mis Tickets Asignados (bandeja propia, no el listado general).
 *   - Administrador:  lo del Supervisor + Gestion de Personal (SOLO LECTURA).
 *
 * "Ver Tickets" y "Mis Tickets Asignados" abren la MISMA pantalla
 * (ListadoTickets), que internamente se adapta segun el rol.
 *
 * Layout (Prioridad 7, pulido UI/UX): sidebar de navegacion fijo + cabecera
 * con fecha/usuario + tarjetas de resumen (ReporteDAO.contarPorEstado()) a
 * modo de panel de inicio. Se mantiene la arquitectura de ventanas
 * separadas: cada boton del sidebar sigue abriendo su pantalla en un
 * JFrame propio (no se reescribieron las 7 pantallas a un panel central
 * unico), por decision explicita del usuario.
 *
 * "Historial" y "Tickets Activos" (extra) son funcionalidad ADICIONAL, no
 * comprometida en las 12 HU del documento (equivalen a HU-13). Se
 * mantienen por decision explicita del usuario, visibles solo para
 * Supervisor y Administrador, y claramente rotuladas como extra.
 */
public class MenuPrincipal extends JFrame {

    private static final String ROL_AGENTE        = "Agente";
    private static final String ROL_SUPERVISOR    = "Supervisor";
    private static final String ROL_TECNICO       = "Tecnico";
    private static final String ROL_ADMINISTRADOR = "Administrador";

    private final Empleado   empleadoLogueado;
    private final ReporteDAO reporteDAO = new ReporteDAO();

    // Una ventana abierta por pantalla: si ya esta abierta, un segundo clic
    // la enfoca en vez de abrir otra igual encima.
    private final Map<String, JFrame> ventanasAbiertas = new HashMap<>();

    public MenuPrincipal(Empleado empleadoLogueado) {
        this.empleadoLogueado = empleadoLogueado;

        setTitle("Consultenos - Menu Principal");
        setDefaultCloseOperation(EXIT_ON_CLOSE); // esta ventana es ahora la principal de la aplicacion
        setSize(980, 620);
        setLocationRelativeTo(null);

        construirInterfaz();
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(EstilosUI.FONDO);
        raiz.add(construirSidebar(), BorderLayout.WEST);
        raiz.add(construirCabecera(), BorderLayout.NORTH);
        raiz.add(construirContenido(), BorderLayout.CENTER);
        setContentPane(raiz);
    }

    /** Barra lateral fija: marca, opciones segun rol, y cerrar sesion al final. */
    private JPanel construirSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(EstilosUI.NAVY_INACAP);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(22, 18, 20, 18));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel marca = new JLabel("CONSULTENOS");
        marca.setForeground(Color.WHITE);
        marca.setFont(marca.getFont().deriveFont(Font.BOLD, 20f));
        marca.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel descripcion = new JLabel("Mesa de ayuda");
        descripcion.setForeground(new Color(0xAB, 0xC6, 0xE0));
        descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(marca);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(descripcion);
        sidebar.add(Box.createVerticalStrut(28));

        String rol = empleadoLogueado.getNombreRol();

        // Agente, Supervisor y Administrador comparten Registrar + Ver Tickets.
        if (rol.equals(ROL_AGENTE) || rol.equals(ROL_SUPERVISOR) || rol.equals(ROL_ADMINISTRADOR)) {
            agregarItemSidebar(sidebar, "Registrar Ticket", true,
                    () -> abrirOEnfocar("registrar", () -> new RegistroTicket(empleadoLogueado.getIdEmpleado())));
            agregarItemSidebar(sidebar, "Ver Tickets", true,
                    () -> abrirOEnfocar("verTickets", () -> new ListadoTickets(empleadoLogueado.getIdEmpleado(), rol)));
        }

        // Tecnico solo ve su propia bandeja (no el listado general).
        if (rol.equals(ROL_TECNICO)) {
            agregarItemSidebar(sidebar, "Mis Tickets Asignados", true,
                    () -> abrirOEnfocar("misTickets", () -> new ListadoTickets(empleadoLogueado.getIdEmpleado(), rol)));
        }

        // Supervisor y Administrador suman Asignacion, Reportes y las extra.
        if (rol.equals(ROL_SUPERVISOR) || rol.equals(ROL_ADMINISTRADOR)) {
            agregarItemSidebar(sidebar, "Asignacion de Tecnicos", true,
                    () -> abrirOEnfocar("asignacion", AsignacionTicket::new));
            agregarItemSidebar(sidebar, "Reportes de Gestion", true,
                    () -> abrirOEnfocar("reportes", Reportes::new));
            agregarItemSidebar(sidebar, "Historial (extra)", true,
                    () -> abrirOEnfocar("historial", Historial::new));
            agregarItemSidebar(sidebar, "Tickets Activos (extra)", true,
                    () -> abrirOEnfocar("activosExtra", () -> new ListadoActivosExtra(empleadoLogueado.getIdEmpleado())));
        }

        // Administrador suma Gestion de Personal (solo lectura).
        if (rol.equals(ROL_ADMINISTRADOR)) {
            agregarItemSidebar(sidebar, "Gestion de Personal", true,
                    () -> abrirOEnfocar("personal", GestionPersonal::new));
        }

        sidebar.add(Box.createVerticalGlue());
        agregarItemSidebar(sidebar, "Cerrar sesion", true, this::cerrarSesion);
        return sidebar;
    }

    /**
     * Abre una pantalla nueva, o enfoca la que ya estaba abierta con esa
     * clave en vez de crear otra encima. "isDisplayable()" es false una vez
     * que la ventana se dispose() (se cerro), asi que ahi si se crea una nueva.
     *
     * Si la ventana reutilizada implementa Refrescable, se le pide refrescar
     * datos antes de enfocarla: si no, quedaria mostrando lo que tenia
     * cargado la primera vez que se abrio (ej. Historial no mostraria un
     * cambio de estado hecho despues, en otra pantalla).
     */
    private void abrirOEnfocar(String clave, Supplier<JFrame> creador) {
        JFrame existente = ventanasAbiertas.get(clave);
        if (existente != null && existente.isDisplayable()) {
            if (existente instanceof Refrescable) {
                ((Refrescable) existente).refrescar();
            }
            existente.toFront();
            existente.requestFocus();
            return;
        }

        JFrame nueva = creador.get();
        ventanasAbiertas.put(clave, nueva);
        nueva.setVisible(true);
    }

    /** Un boton de la barra lateral: texto blanco alineado a la izquierda, fondo navy, sin borde. */
    private void agregarItemSidebar(JPanel sidebar, String texto, boolean habilitado, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setForeground(new Color(0xE0, 0xEB, 0xF5));
        boton.setBackground(EstilosUI.NAVY_INACAP);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFocusPainted(false);
        boton.setEnabled(habilitado);
        if (habilitado) {
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            boton.addActionListener(e -> accion.run());
        }
        boton.putClientProperty("JButton.buttonType", "borderless");
        sidebar.add(boton);
        sidebar.add(Box.createVerticalStrut(4));
    }

    /** Cabecera superior: fecha + iniciales del usuario en un circulo + nombre/rol. */
    private JPanel construirCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Color.WHITE);
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstilosUI.BORDE),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        cabecera.add(EstilosUI.titulo("Inicio", 18f), BorderLayout.WEST);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        derecha.setOpaque(false);

        JLabel fecha = EstilosUI.textoSuave(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        JLabel avatar = new JLabel(iniciales(empleadoLogueado.getNombreCompleto()), SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(0xEB, 0xF1, 0xFA));
        avatar.setForeground(EstilosUI.NAVY_INACAP);
        avatar.setFont(avatar.getFont().deriveFont(Font.BOLD));
        avatar.setPreferredSize(new Dimension(36, 36));

        JLabel usuario = new JLabel("<html><b>" + empleadoLogueado.getNombreCompleto()
                + "</b><br><span style='color:#667484'>" + empleadoLogueado.getNombreRol() + "</span></html>");

        derecha.add(fecha);
        derecha.add(avatar);
        derecha.add(usuario);
        cabecera.add(derecha, BorderLayout.EAST);
        return cabecera;
    }

    /** Contenido central: saludo + tarjetas de resumen por estado (ReporteDAO.contarPorEstado()). */
    private JPanel construirContenido() {
        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));

        JLabel saludo = EstilosUI.titulo("Hola, " + empleadoLogueado.getNombreCompleto(), 22f);
        saludo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel bienvenida = EstilosUI.textoSuave("Bienvenido al sistema de gestion de tickets de Consultenos.");
        bienvenida.setAlignmentX(Component.LEFT_ALIGNMENT);

        contenido.add(saludo);
        contenido.add(Box.createVerticalStrut(4));
        contenido.add(bienvenida);
        contenido.add(Box.createVerticalStrut(20));
        contenido.add(construirTarjetasResumen());

        return contenido;
    }

    /** Una tarjeta blanca por cada estado del catalogo, con la cantidad actual de tickets. */
    private JPanel construirTarjetasResumen() {
        List<ConteoPorEstado> conteos = reporteDAO.contarPorEstado();

        JPanel fila = new JPanel(new GridLayout(1, Math.max(conteos.size(), 1), 16, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        for (ConteoPorEstado c : conteos) {
            TarjetaPanel tarjeta = new TarjetaPanel(new BorderLayout());
            JLabel lblEstado = EstilosUI.textoSuave(c.getNombreEstado());
            JLabel lblCantidad = new JLabel(String.valueOf(c.getCantidad()));
            lblCantidad.setFont(lblCantidad.getFont().deriveFont(Font.BOLD, 30f));
            lblCantidad.setForeground(EstilosUI.NAVY_INACAP);
            tarjeta.add(lblEstado, BorderLayout.NORTH);
            tarjeta.add(lblCantidad, BorderLayout.CENTER);
            fila.add(tarjeta);
        }

        return fila;
    }

    private String iniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, 1).toUpperCase();
        }
        return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
    }

    private void cerrarSesion() {
        dispose();
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
