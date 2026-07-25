package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Usuario;
import util.Conexion;

/**
 * DAO de la tabla Usuario_Cliente. Sirve de apoyo al registro de tickets
 * (HU-15: autocompletar al ingresar el codigo de usuario).
 */
public class UsuarioDAO {

    /**
     * Busca un usuario cliente por su codigo (ej. "Mjopi001") y trae tambien
     * su sucursal y empresa por JOIN, para autocompletar el formulario de
     * registro de ticket.
     *
     * @param codigoUsuario codigo escrito/leido en el formulario
     * @return el Usuario con sus datos; null si no existe ese codigo
     */
    public Usuario buscarPorCodigo(String codigoUsuario) {
        String sql = "SELECT u.id_usuario, u.id_sucursal, u.codigo_usuario, u.rut, "
                   + "       u.nombre_completo, u.telefono, u.correo, "
                   + "       s.nombre_sucursal, ec.razon_social "
                   + "FROM Usuario_Cliente u "
                   + "INNER JOIN Sucursal        s  ON u.id_sucursal = s.id_sucursal "
                   + "INNER JOIN Empresa_Cliente ec ON s.id_empresa  = ec.id_empresa "
                   + "WHERE u.codigo_usuario = ?";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, codigoUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearFila(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por codigo: " + e.getMessage());
        }

        return null;
    }

    /** Lista los usuarios clientes de una sucursal (apoyo a formularios y filtros). */
    public List<Usuario> listarPorSucursal(int idSucursal) {
        List<Usuario> lista = new ArrayList<>();

        String sql = "SELECT u.id_usuario, u.id_sucursal, u.codigo_usuario, u.rut, "
                   + "       u.nombre_completo, u.telefono, u.correo, "
                   + "       s.nombre_sucursal, ec.razon_social "
                   + "FROM Usuario_Cliente u "
                   + "INNER JOIN Sucursal        s  ON u.id_sucursal = s.id_sucursal "
                   + "INNER JOIN Empresa_Cliente ec ON s.id_empresa  = ec.id_empresa "
                   + "WHERE u.id_sucursal = ? "
                   + "ORDER BY u.nombre_completo";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idSucursal);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios por sucursal: " + e.getMessage());
        }

        return lista;
    }

    /** Arma un Usuario a partir de la fila actual del ResultSet (mismas columnas en ambas consultas). */
    private Usuario mapearFila(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setIdSucursal(rs.getInt("id_sucursal"));
        u.setCodigoUsuario(rs.getString("codigo_usuario"));
        u.setRut(rs.getString("rut"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setTelefono(rs.getString("telefono"));
        u.setCorreo(rs.getString("correo"));
        u.setNombreSucursal(rs.getString("nombre_sucursal"));
        u.setRazonSocial(rs.getString("razon_social"));
        return u;
    }

    // ======================================================================
    //  METODO DE PRUEBA
    //  Clic derecho en este archivo > Run File.
    // ======================================================================
    public static void main(String[] args) {
        UsuarioDAO dao = new UsuarioDAO();

        System.out.println("=== PRUEBA 1: buscar por codigo 'Mjopi001' ===");
        Usuario u = dao.buscarPorCodigo("Mjopi001");
        System.out.println(u != null
                ? "   " + u.getNombreCompleto() + " | " + u.getNombreSucursal() + " | " + u.getRazonSocial()
                : "   No encontrado.");

        System.out.println("\n=== PRUEBA 2: usuarios de la sucursal 1 ===");
        for (Usuario us : dao.listarPorSucursal(1)) {
            System.out.println("   " + us.getCodigoUsuario() + " - " + us.getNombreCompleto());
        }
    }
}
