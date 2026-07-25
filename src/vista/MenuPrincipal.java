package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
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
 * (ListadoTickets), que internamente se adapta segun el rol: para Tecnico
 * se autofiltra a sus propios tickets asignados; para los demas roles
 * muestra el listado general con filtros (HU-08/16/06/07).
 *
 * "Registrar Ticket" (RegistroTicket), "Asignacion de Tecnicos"
 * (AsignacionTicket), "Reportes de Gestion" (Reportes) y "Gestion de
 * Personal" tambien estan conectadas. Con esto las 7 pantallas de
 * docs/IMPLEMENTACION.md quedan construidas.
 *
 * "Historial" y "Tickets Activos" (extra) son funcionalidad ADICIONAL, no
 * comprometida en las 12 HU del documento (equivalen a HU-13). Se
 * mantienen por decision explicita del usuario, visibles solo para
 * Supervisor y Administrador, y claramente rotuladas como extra.
 */
public class MenuPrincipal extends JFrame {

    private static final Color COLOR_NAVY_INACAP = new Color(0x1F, 0x38, 0x64);

    private static final String ROL_AGENTE       = "Agente";
    private static final String ROL_SUPERVISOR   = "Supervisor";
    private static final String ROL_TECNICO      = "Tecnico";
    private static final String ROL_ADMINISTRADOR = "Administrador";

    private final Empleado empleadoLogueado;

    public MenuPrincipal(Empleado empleadoLogueado) {
        this.empleadoLogueado = empleadoLogueado;

        setTitle("Consultenos - Menu Principal");
        setDefaultCloseOperation(EXIT_ON_CLOSE); // esta ventana es ahora la principal de la aplicacion
        setSize(420, 480);
        setLocationRelativeTo(null);

        construirInterfaz();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(10, 10));

        JLabel lblBienvenida = new JLabel(
                "<html>Bienvenido, " + empleadoLogueado.getNombreCompleto()
                        + "<br>Rol: " + empleadoLogueado.getNombreRol() + "</html>",
                SwingConstants.CENTER);
        lblBienvenida.setFont(lblBienvenida.getFont().deriveFont(Font.BOLD, 15f));
        lblBienvenida.setForeground(COLOR_NAVY_INACAP);
        lblBienvenida.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        add(lblBienvenida, BorderLayout.NORTH);

        JPanel panelOpciones = new JPanel(new GridLayout(0, 1, 10, 10));
        panelOpciones.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        String rol = empleadoLogueado.getNombreRol();

        // Agente, Supervisor y Administrador comparten Registrar + Ver Tickets.
        if (rol.equals(ROL_AGENTE) || rol.equals(ROL_SUPERVISOR) || rol.equals(ROL_ADMINISTRADOR)) {
            panelOpciones.add(botonRegistrarTicket());
            panelOpciones.add(botonVerTickets());
        }

        // Tecnico solo ve su propia bandeja (no el listado general).
        if (rol.equals(ROL_TECNICO)) {
            panelOpciones.add(botonMisTicketsAsignados());
        }

        // Supervisor y Administrador suman Asignacion y Reportes.
        if (rol.equals(ROL_SUPERVISOR) || rol.equals(ROL_ADMINISTRADOR)) {
            panelOpciones.add(botonAsignacion());
            panelOpciones.add(botonReportes());
            panelOpciones.add(botonHistorialExtra());
            panelOpciones.add(botonTicketsActivosExtra());
        }

        // Administrador suma Gestion de Personal (solo lectura).
        if (rol.equals(ROL_ADMINISTRADOR)) {
            panelOpciones.add(botonGestionPersonal());
        }

        add(panelOpciones, BorderLayout.CENTER);

        JButton btnCerrarSesion = new JButton("Cerrar sesion");
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));
        panelInferior.add(Box.createVerticalStrut(5));
        panelInferior.add(btnCerrarSesion);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JButton botonRegistrarTicket() {
        JButton boton = new JButton("Registrar Ticket");
        boton.addActionListener(e -> new RegistroTicket(empleadoLogueado.getIdEmpleado()).setVisible(true));
        return boton;
    }

    private JButton botonVerTickets() {
        JButton boton = new JButton("Ver Tickets");
        boton.addActionListener(e -> new ListadoTickets(
                empleadoLogueado.getIdEmpleado(), empleadoLogueado.getNombreRol()).setVisible(true));
        return boton;
    }

    private JButton botonMisTicketsAsignados() {
        JButton boton = new JButton("Mis Tickets Asignados");
        boton.addActionListener(e -> new ListadoTickets(
                empleadoLogueado.getIdEmpleado(), empleadoLogueado.getNombreRol()).setVisible(true));
        return boton;
    }

    private JButton botonAsignacion() {
        JButton boton = new JButton("Asignacion de Tecnicos");
        boton.addActionListener(e -> new AsignacionTicket().setVisible(true));
        return boton;
    }

    private JButton botonReportes() {
        JButton boton = new JButton("Reportes de Gestion");
        boton.addActionListener(e -> new Reportes().setVisible(true));
        return boton;
    }

    private JButton botonHistorialExtra() {
        JButton boton = new JButton("Historial (extra, fuera de las 12 HU)");
        boton.setToolTipText("Busqueda sobre Historial_Ticket. No es parte de las 12 HU comprometidas"
                + " en docs/IMPLEMENTACION.md (equivale a HU-13); se mantiene como funcionalidad adicional.");
        boton.addActionListener(e -> new Historial().setVisible(true));
        return boton;
    }

    private JButton botonTicketsActivosExtra() {
        JButton boton = new JButton("Tickets Activos (extra, fuera de las 12 HU)");
        boton.setToolTipText("Listado activos + cambio de estado inline con auto-registro en Historial_Ticket."
                + " No es parte de las 12 HU comprometidas (equivale a HU-13); se mantiene como funcionalidad adicional.");
        boton.addActionListener(e -> new ListadoActivosExtra(empleadoLogueado.getIdEmpleado()).setVisible(true));
        return boton;
    }

    private JButton botonGestionPersonal() {
        JButton boton = new JButton("Gestion de Personal (solo lectura)");
        boton.addActionListener(e -> new GestionPersonal().setVisible(true));
        return boton;
    }

    /** Boton deshabilitado para pantallas que aun no se han construido. */
    private JButton crearBotonProximamente(String texto) {
        JButton boton = new JButton(texto + " (Proximamente)");
        boton.setEnabled(false);
        boton.setToolTipText("Esta pantalla todavia no esta construida.");
        return boton;
    }

    private void cerrarSesion() {
        dispose();
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
