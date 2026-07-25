package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import dao.EmpleadoDAO;
import modelo.Empleado;

/**
 * Pantalla de inicio de sesion. Es la puerta de entrada de la aplicacion:
 * valida credenciales contra EmpleadoDAO.login() (ya existente, no se toca)
 * y, si son correctas, abre MenuPrincipal con el Empleado que inicio sesion.
 *
 * La vista NO sabe SQL ni de hashes: solo le pasa usuario/contrasena al DAO
 * y reacciona al resultado (Empleado o null).
 */
public class Login extends JFrame {

    private static final Color COLOR_NAVY_INACAP = new Color(0x1F, 0x38, 0x64);
    private static final Color COLOR_ROJO_INACAP  = new Color(0xE3, 0x05, 0x14);

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    private JTextField     txtUsuario;
    private JPasswordField txtContrasena;

    public Login() {
        setTitle("Consultenos - Ingreso");
        setDefaultCloseOperation(EXIT_ON_CLOSE); // cerrar el login sin ingresar termina la aplicacion
        setSize(380, 280);
        setLocationRelativeTo(null);
        setResizable(false);

        construirInterfaz();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitulo = new JLabel("Consultenos", SwingConstants.CENTER);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 22f));
        lblTitulo.setForeground(COLOR_NAVY_INACAP);
        JLabel lblSubtitulo = new JLabel("Mesa de ayuda - Ingreso de personal", SwingConstants.CENTER);
        lblSubtitulo.setForeground(COLOR_ROJO_INACAP);

        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));
        panelTitulo.add(lblTitulo, BorderLayout.CENTER);
        panelTitulo.add(lblSubtitulo, BorderLayout.SOUTH);
        add(panelTitulo, BorderLayout.NORTH);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUsuario = new JTextField();
        txtContrasena = new JPasswordField();
        // Enter en cualquiera de los dos campos intenta el login, igual que hacer clic en el boton.
        txtUsuario.addActionListener(e -> intentarLogin());
        txtContrasena.addActionListener(e -> intentarLogin());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panelFormulario.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panelFormulario.add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelFormulario.add(new JLabel("Contrasena:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panelFormulario.add(txtContrasena, gbc);

        add(panelFormulario, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 30, 15, 30));

        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.addActionListener(e -> intentarLogin());

        panelInferior.add(btnIngresar, BorderLayout.SOUTH);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void intentarLogin() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese usuario y contrasena.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Empleado empleado = empleadoDAO.login(usuario, contrasena);

        // Mensaje tal como lo pide docs/IMPLEMENTACION.md para HU-12.
        if (empleado == null) {
            JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos",
                    "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            txtContrasena.setText("");
            return;
        }

        dispose(); // cierra el login; MenuPrincipal pasa a ser la ventana principal
        SwingUtilities.invokeLater(() -> new MenuPrincipal(empleado).setVisible(true));
    }

    // ======================================================================
    //  PUNTO DE ENTRADA DE LA APLICACION
    // ======================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
