package vista;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import dao.EmpleadoDAO;
import modelo.Empleado;

/**
 * Gestion de Personal, version SOLO LECTURA para el rol Administrador
 * (segun docs/IMPLEMENTACION.md: crear/editar/eliminar personal queda
 * fuera de esta entrega, para no abrir alcance nuevo).
 *
 * Reutiliza EmpleadoDAO.listarTodos(), que ya existe y esta probado; esta
 * pantalla solo lo muestra en una tabla, no agrega SQL nuevo.
 */
public class GestionPersonal extends JFrame {

    private static final String[] COLUMNAS = {"ID", "Nombre completo", "Usuario", "Rol", "Activo"};

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private DefaultTableModel modeloTabla;

    public GestionPersonal() {
        setTitle("Consultenos - Gestion de Personal (solo lectura)");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(650, 420);
        setLocationRelativeTo(null);

        construirInterfaz();
        cargarPersonal();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(8, 8));

        JLabel lblTitulo = EstilosUI.titulo("Personal interno (solo lectura)", 16f);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(lblTitulo, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // solo lectura: alta/edicion/baja quedan fuera de alcance
            }
        };
        JTable tabla = new JTable(modeloTabla);
        tabla.setRowHeight(24);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    private void cargarPersonal() {
        modeloTabla.setRowCount(0);
        for (Empleado emp : empleadoDAO.listarTodos()) {
            modeloTabla.addRow(new Object[]{
                emp.getIdEmpleado(),
                emp.getNombreCompleto(),
                emp.getUsuario(),
                emp.getNombreRol(),
                emp.isEstadoActivo() ? "Si" : "No"
            });
        }
    }
}
