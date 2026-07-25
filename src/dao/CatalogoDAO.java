package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Ambito;
import modelo.EstadoTicket;
import modelo.Sucursal;
import util.Conexion;

/**
 * DAO de catalogos simples (id + nombre) que se usan para llenar JComboBox
 * en varias pantallas: Ambito, Estado_Ticket y Sucursal.
 *
 * Se centralizan aqui en vez de repetir la misma consulta en cada DAO que
 * los necesite (TicketDAO, RegistroTicket, etc. los consultan desde un
 * solo lugar).
 */
public class CatalogoDAO {

    /** Catalogo de ambitos tecnicos (Impresion, Redes, Radiofrecuencia, etc.). */
    public List<Ambito> listarAmbitos() {
        List<Ambito> lista = new ArrayList<>();
        String sql = "SELECT id_ambito, nombre_ambito FROM Ambito ORDER BY nombre_ambito";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Ambito(rs.getInt("id_ambito"), rs.getString("nombre_ambito")));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar ambitos: " + e.getMessage());
        }

        return lista;
    }

    /** Catalogo de estados del ticket (Abierto, En progreso, Resuelto, Cerrado). */
    public List<EstadoTicket> listarEstados() {
        List<EstadoTicket> lista = new ArrayList<>();
        String sql = "SELECT id_estado, nombre_estado FROM Estado_Ticket ORDER BY id_estado";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new EstadoTicket(rs.getInt("id_estado"), rs.getString("nombre_estado")));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar estados: " + e.getMessage());
        }

        return lista;
    }

    /** Catalogo de sucursales (todas las empresas; hoy solo opera ServiAndina). */
    public List<Sucursal> listarSucursales() {
        List<Sucursal> lista = new ArrayList<>();
        String sql = "SELECT id_sucursal, nombre_sucursal FROM Sucursal ORDER BY nombre_sucursal";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Sucursal(rs.getInt("id_sucursal"), rs.getString("nombre_sucursal")));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar sucursales: " + e.getMessage());
        }

        return lista;
    }

    // ======================================================================
    //  METODO DE PRUEBA
    //  Clic derecho en este archivo > Run File.
    // ======================================================================
    public static void main(String[] args) {
        CatalogoDAO dao = new CatalogoDAO();

        System.out.println("=== Ambitos ===");
        for (Ambito a : dao.listarAmbitos()) {
            System.out.println("   " + a.getIdAmbito() + " - " + a.getNombreAmbito());
        }

        System.out.println("\n=== Estados ===");
        for (EstadoTicket e : dao.listarEstados()) {
            System.out.println("   " + e.getIdEstado() + " - " + e.getNombreEstado());
        }

        System.out.println("\n=== Sucursales ===");
        for (Sucursal s : dao.listarSucursales()) {
            System.out.println("   " + s.getIdSucursal() + " - " + s.getNombreSucursal());
        }
    }
}
