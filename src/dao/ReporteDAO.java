package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.CargaTecnico;
import modelo.ConteoPorAmbito;
import modelo.ConteoPorEstado;
import modelo.ResumenMTTR;
import util.Conexion;

/**
 * DAO de reportes de gestion (HU-11). Las 4 consultas son las mismas ya
 * escritas y probadas en sql/02_consultas_consultenos.sql, seccion HU-11;
 * aqui solo se ejecutan via JDBC y se mapean a modelos.
 */
public class ReporteDAO {

    /** Cuenta cuantos tickets hay hoy en cada estado del catalogo. */
    public List<ConteoPorEstado> contarPorEstado() {
        List<ConteoPorEstado> lista = new ArrayList<>();

        String sql = "SELECT est.nombre_estado, COUNT(t.id_ticket) AS cantidad "
                   + "FROM Estado_Ticket est "
                   + "LEFT JOIN Ticket t ON t.id_estado = est.id_estado "
                   + "GROUP BY est.id_estado, est.nombre_estado "
                   + "ORDER BY est.id_estado";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ConteoPorEstado(rs.getString("nombre_estado"), rs.getInt("cantidad")));
            }

        } catch (SQLException e) {
            System.err.println("Error al contar tickets por estado: " + e.getMessage());
        }

        return lista;
    }

    /**
     * MTTR: tiempo promedio de resolucion en horas, entre creacion y cierre.
     * Solo considera tickets que ya tienen fecha_hora_cierre (Resuelto/Cerrado).
     */
    public ResumenMTTR calcularMTTR() {
        String sql = "SELECT ROUND(AVG(TIMESTAMPDIFF(MINUTE, t.fecha_hora_creacion, t.fecha_hora_cierre)) / 60, 2) "
                   + "           AS horas_promedio_resolucion, "
                   + "       COUNT(*) AS tickets_considerados "
                   + "FROM Ticket t "
                   + "WHERE t.fecha_hora_cierre IS NOT NULL";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double horas = rs.getDouble("horas_promedio_resolucion");
                Double horasPromedio = rs.wasNull() ? null : horas;
                return new ResumenMTTR(horasPromedio, rs.getInt("tickets_considerados"));
            }

        } catch (SQLException e) {
            System.err.println("Error al calcular MTTR: " + e.getMessage());
        }

        return new ResumenMTTR(null, 0);
    }

    /** Carga de trabajo por tecnico: cuantos tickets tiene asignados y cuantos ha resuelto. */
    public List<CargaTecnico> cargaPorTecnico() {
        List<CargaTecnico> lista = new ArrayList<>();

        String sql = "SELECT emp.nombre_completo AS tecnico, g.nombre_grupo, "
                   + "       COUNT(t.id_ticket) AS tickets_asignados, "
                   + "       SUM(CASE WHEN est.nombre_estado IN ('Resuelto','Cerrado') THEN 1 ELSE 0 END) AS resueltos "
                   + "FROM Empleado_Interno emp "
                   + "INNER JOIN Rol             r   ON emp.id_rol = r.id_rol "
                   + "LEFT  JOIN Grupo_Resolutor g   ON emp.id_grupo = g.id_grupo "
                   + "LEFT  JOIN Ticket          t   ON t.id_tecnico_asignado = emp.id_empleado "
                   + "LEFT  JOIN Estado_Ticket   est ON t.id_estado = est.id_estado "
                   + "WHERE r.nombre_rol = 'Tecnico' "
                   + "GROUP BY emp.id_empleado, emp.nombre_completo, g.nombre_grupo "
                   + "ORDER BY tickets_asignados DESC";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CargaTecnico c = new CargaTecnico();
                c.setNombreTecnico(rs.getString("tecnico"));
                c.setNombreGrupo(rs.getString("nombre_grupo"));
                c.setTicketsAsignados(rs.getInt("tickets_asignados"));
                c.setResueltos(rs.getInt("resueltos"));
                lista.add(c);
            }

        } catch (SQLException e) {
            System.err.println("Error al calcular carga por tecnico: " + e.getMessage());
        }

        return lista;
    }

    /** Cantidad de tickets por ambito tecnico (donde se concentra la demanda). */
    public List<ConteoPorAmbito> contarPorAmbito() {
        List<ConteoPorAmbito> lista = new ArrayList<>();

        String sql = "SELECT a.nombre_ambito, COUNT(t.id_ticket) AS total "
                   + "FROM Ambito a "
                   + "LEFT JOIN Ticket t ON t.id_ambito = a.id_ambito "
                   + "GROUP BY a.id_ambito, a.nombre_ambito "
                   + "ORDER BY total DESC";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ConteoPorAmbito(rs.getString("nombre_ambito"), rs.getInt("total")));
            }

        } catch (SQLException e) {
            System.err.println("Error al contar tickets por ambito: " + e.getMessage());
        }

        return lista;
    }

    // ======================================================================
    //  METODO DE PRUEBA
    //  Clic derecho en este archivo > Run File.
    // ======================================================================
    public static void main(String[] args) {
        ReporteDAO dao = new ReporteDAO();

        System.out.println("=== Tickets por estado ===");
        for (ConteoPorEstado c : dao.contarPorEstado()) {
            System.out.println("   " + c.getNombreEstado() + ": " + c.getCantidad());
        }

        System.out.println("\n=== MTTR ===");
        ResumenMTTR mttr = dao.calcularMTTR();
        System.out.println("   Horas promedio: " + mttr.getHorasPromedio()
                + " (sobre " + mttr.getTicketsConsiderados() + " tickets cerrados)");

        System.out.println("\n=== Carga por tecnico ===");
        for (CargaTecnico c : dao.cargaPorTecnico()) {
            System.out.println("   " + c.getNombreTecnico() + " (" + c.getNombreGrupo() + "): "
                    + c.getTicketsAsignados() + " asignados, " + c.getResueltos() + " resueltos");
        }

        System.out.println("\n=== Tickets por ambito ===");
        for (ConteoPorAmbito c : dao.contarPorAmbito()) {
            System.out.println("   " + c.getNombreAmbito() + ": " + c.getTotal());
        }
    }
}
