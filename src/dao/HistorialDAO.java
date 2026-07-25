package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.HistorialTicket;
import util.Conexion;

/**
 * DAO de la tabla Historial_Ticket. Alimenta la pantalla de Historial y
 * Busqueda: trae las filas de auditoria con los nombres ya resueltos por
 * JOIN (responsable, su rol, y el estado actual del ticket).
 *
 * Historial_Ticket no guarda su propio estado (solo id_ticket, id_empleado,
 * fecha_hora_accion y accion en texto libre). Por eso el filtro "por estado"
 * se aplica sobre el estado ACTUAL del ticket asociado a cada fila
 * (Historial_Ticket -> Ticket -> Estado_Ticket), que es la unica lectura
 * consistente con el esquema.
 */
public class HistorialDAO {

    /**
     * Busca en el historial combinando filtros opcionales. Cualquier filtro
     * en null o vacio se ignora (no restringe la busqueda).
     *
     * @param texto        se busca con LIKE dentro de Historial_Ticket.accion
     * @param nombreEstado estado ACTUAL del ticket (Abierto, En progreso, Resuelto, Cerrado)
     * @param fechaDesde   limite inferior de fecha_hora_accion; acepta 'yyyy-MM-dd'
     *                     o 'yyyy-MM-dd HH:mm:ss'. Si viene solo la fecha, se
     *                     completa con 00:00:00
     * @param fechaHasta   limite superior de fecha_hora_accion; misma logica que
     *                     fechaDesde, pero completando con 23:59:59 para incluir
     *                     todo el dia final
     * @return filas encontradas, ordenadas de la mas reciente a la mas antigua
     */
    public List<HistorialTicket> buscar(String texto, String nombreEstado,
                                         String fechaDesde, String fechaHasta) {
        List<HistorialTicket> lista = new ArrayList<>();

        texto        = vacioComoNulo(texto);
        nombreEstado = vacioComoNulo(nombreEstado);
        fechaDesde   = normalizarInicioDia(vacioComoNulo(fechaDesde));
        fechaHasta   = normalizarFinDia(vacioComoNulo(fechaHasta));

        // Patron "(? IS NULL OR campo = ?)": si el parametro viene null, esa
        // condicion se ignora completa (queda en TRUE) sin tener que armar
        // el SQL a mano segun que filtros esten presentes.
        String sql = "SELECT h.id_historial, h.id_ticket, h.fecha_hora_accion, h.accion, "
                   + "       emp.nombre_completo AS responsable, r.nombre_rol, "
                   + "       est.nombre_estado AS estado_actual "
                   + "FROM Historial_Ticket h "
                   + "INNER JOIN Ticket           t   ON h.id_ticket   = t.id_ticket "
                   + "INNER JOIN Estado_Ticket    est ON t.id_estado    = est.id_estado "
                   + "INNER JOIN Empleado_Interno emp ON h.id_empleado  = emp.id_empleado "
                   + "INNER JOIN Rol              r   ON emp.id_rol    = r.id_rol "
                   + "WHERE (? IS NULL OR h.accion LIKE CONCAT('%', ?, '%')) "
                   + "  AND (? IS NULL OR est.nombre_estado = ?) "
                   + "  AND (? IS NULL OR h.fecha_hora_accion >= ?) "
                   + "  AND (? IS NULL OR h.fecha_hora_accion <= ?) "
                   + "ORDER BY h.fecha_hora_accion DESC";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, texto);
            ps.setString(2, texto);
            ps.setString(3, nombreEstado);
            ps.setString(4, nombreEstado);
            ps.setString(5, fechaDesde);
            ps.setString(6, fechaDesde);
            ps.setString(7, fechaHasta);
            ps.setString(8, fechaHasta);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialTicket h = new HistorialTicket();
                    h.setIdHistorial(rs.getInt("id_historial"));
                    h.setIdTicket(rs.getInt("id_ticket"));
                    h.setFechaHoraAccion(rs.getString("fecha_hora_accion"));
                    h.setAccion(rs.getString("accion"));
                    h.setNombreResponsable(rs.getString("responsable"));
                    h.setNombreRolResponsable(rs.getString("nombre_rol"));
                    h.setNombreEstadoActual(rs.getString("estado_actual"));
                    lista.add(h);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar en el historial: " + e.getMessage());
        }

        return lista;
    }

    /** Convierte cadenas vacias o solo espacios en null, para tratarlas como "sin filtro". */
    private static String vacioComoNulo(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    /** Si viene solo la fecha (sin hora), la completa con el inicio del dia. */
    private static String normalizarInicioDia(String fecha) {
        if (fecha == null) {
            return null;
        }
        return fecha.contains(" ") ? fecha : fecha + " 00:00:00";
    }

    /** Si viene solo la fecha (sin hora), la completa con el final del dia (incluye todo el dia). */
    private static String normalizarFinDia(String fecha) {
        if (fecha == null) {
            return null;
        }
        return fecha.contains(" ") ? fecha : fecha + " 23:59:59";
    }

    // ======================================================================
    //  METODO DE PRUEBA
    //  Clic derecho en este archivo > Run File.
    // ======================================================================
    public static void main(String[] args) {
        HistorialDAO dao = new HistorialDAO();

        System.out.println("=== PRUEBA 1: sin filtros (todo el historial) ===");
        for (HistorialTicket h : dao.buscar(null, null, null, null)) {
            System.out.println("   " + h.getFechaHoraAccion()
                    + " | ticket #" + h.getIdTicket()
                    + " | " + h.getAccion()
                    + " | " + h.getNombreResponsable() + " (" + h.getNombreRolResponsable() + ")"
                    + " | estado actual: " + h.getNombreEstadoActual());
        }

        System.out.println("\n=== PRUEBA 2: filtro por texto 'asignado' ===");
        for (HistorialTicket h : dao.buscar("asignado", null, null, null)) {
            System.out.println("   ticket #" + h.getIdTicket() + " | " + h.getAccion());
        }

        System.out.println("\n=== PRUEBA 3: filtro por estado 'Resuelto' ===");
        for (HistorialTicket h : dao.buscar(null, "Resuelto", null, null)) {
            System.out.println("   ticket #" + h.getIdTicket() + " | " + h.getAccion());
        }

        System.out.println("\n=== PRUEBA 4: filtro por rango de fechas (solo dia) ===");
        for (HistorialTicket h : dao.buscar(null, null, "2026-07-10", "2026-07-10")) {
            System.out.println("   " + h.getFechaHoraAccion() + " | ticket #" + h.getIdTicket());
        }
    }
}
