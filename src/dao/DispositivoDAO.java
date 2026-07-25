package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Dispositivo;
import util.Conexion;

/**
 * DAO de la tabla Dispositivo. Sirve de apoyo al registro de tickets
 * (combo de equipos de la sucursal, HU-15) y al historial por dispositivo
 * (HU-07, buscarPorSerie).
 */
public class DispositivoDAO {

    /** Dispositivos de una sucursal, para el combo del formulario de registro. */
    public List<Dispositivo> listarPorSucursal(int idSucursal) {
        List<Dispositivo> lista = new ArrayList<>();

        String sql = "SELECT id_dispositivo, id_sucursal, numero_serie, tipo_hardware, modelo "
                   + "FROM Dispositivo "
                   + "WHERE id_sucursal = ? "
                   + "ORDER BY tipo_hardware";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idSucursal);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar dispositivos por sucursal: " + e.getMessage());
        }

        return lista;
    }

    /** Busca un dispositivo por su numero de serie (apoyo a HU-07, historial por equipo). */
    public Dispositivo buscarPorSerie(String numeroSerie) {
        String sql = "SELECT id_dispositivo, id_sucursal, numero_serie, tipo_hardware, modelo "
                   + "FROM Dispositivo "
                   + "WHERE numero_serie = ?";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, numeroSerie);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearFila(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar dispositivo por serie: " + e.getMessage());
        }

        return null;
    }

    private Dispositivo mapearFila(ResultSet rs) throws SQLException {
        Dispositivo d = new Dispositivo();
        d.setIdDispositivo(rs.getInt("id_dispositivo"));
        d.setIdSucursal(rs.getInt("id_sucursal"));
        d.setNumeroSerie(rs.getString("numero_serie"));
        d.setTipoHardware(rs.getString("tipo_hardware"));
        d.setModelo(rs.getString("modelo"));
        return d;
    }

    // ======================================================================
    //  METODO DE PRUEBA
    //  Clic derecho en este archivo > Run File.
    // ======================================================================
    public static void main(String[] args) {
        DispositivoDAO dao = new DispositivoDAO();

        System.out.println("=== PRUEBA 1: dispositivos de la sucursal 1 ===");
        for (Dispositivo d : dao.listarPorSucursal(1)) {
            System.out.println("   " + d);
        }

        System.out.println("\n=== PRUEBA 2: buscar por serie 'SN-IMP-00123' ===");
        Dispositivo d = dao.buscarPorSerie("SN-IMP-00123");
        System.out.println(d != null ? "   " + d : "   No encontrado.");
    }
}
