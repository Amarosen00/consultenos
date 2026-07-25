package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import modelo.EstadoTicket;
import modelo.Ticket;
import util.Conexion;

/**
 * DAO de la tabla Ticket. Cubre el nucleo del sistema: registrar, listar con
 * filtros, ver el detalle completo, asignar tecnico y los cambios de estado
 * del ciclo de vida (Abierto -> En progreso -> Resuelto -> Cerrado).
 *
 * listarEstados() vive en CatalogoDAO (junto con Ambito y Sucursal), no aqui,
 * para no repetir esa consulta en cada DAO que necesite el catalogo.
 *
 * Nota sobre listarActivos()/cambiarEstado(idTicket, EstadoTicket, idEmpleado):
 * son de una version anterior de la pantalla de Listado (antes de conocer
 * docs/IMPLEMENTACION.md) y ademas escriben en Historial_Ticket (HU-13, fuera
 * de las 12 HU comprometidas). Se mantienen por decision explicita del
 * usuario como funcionalidad extra; los metodos nuevos de aqui en adelante
 * NO tocan Historial_Ticket.
 *
 * Mismo estilo que EmpleadoDAO: try-with-resources, PreparedStatement con
 * '?', mapeo fila -> objeto, wasNull() para columnas nullable.
 */
public class TicketDAO {

    // ======================================================================
    //  LISTADO DE ACTIVOS (extra, ver nota de la clase) + CAMBIO DE ESTADO
    //  CON REGISTRO EN HISTORIAL (extra, HU-13 fuera de alcance)
    // ======================================================================

    /**
     * Lista los tickets activos (estado Abierto o En progreso).
     * Usada por vista/ListadoTickets.java (version activa-unicamente).
     */
    public List<Ticket> listarActivos() {
        List<Ticket> lista = new ArrayList<>();

        String sql = "SELECT t.id_ticket, t.id_estado, t.fecha_hora_creacion, t.prioridad, "
                   + "       est.nombre_estado, a.nombre_ambito, "
                   + "       u.nombre_completo AS reporta, "
                   + "       COALESCE(tec.nombre_completo, 'Sin asignar') AS tecnico "
                   + "FROM Ticket t "
                   + "INNER JOIN Estado_Ticket   est ON t.id_estado  = est.id_estado "
                   + "INNER JOIN Ambito          a   ON t.id_ambito  = a.id_ambito "
                   + "INNER JOIN Usuario_Cliente u   ON t.id_usuario = u.id_usuario "
                   + "LEFT  JOIN Empleado_Interno tec ON t.id_tecnico_asignado = tec.id_empleado "
                   + "WHERE est.nombre_estado IN ('Abierto','En progreso') "
                   + "ORDER BY FIELD(t.prioridad,'Alta','Media','Baja'), t.fecha_hora_creacion";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Ticket t = new Ticket();
                t.setIdTicket(rs.getInt("id_ticket"));
                t.setIdEstado(rs.getInt("id_estado"));
                t.setFechaHoraCreacion(rs.getString("fecha_hora_creacion"));
                t.setPrioridad(rs.getString("prioridad"));
                t.setNombreEstado(rs.getString("nombre_estado"));
                t.setNombreAmbito(rs.getString("nombre_ambito"));
                t.setNombreUsuario(rs.getString("reporta"));
                t.setNombreTecnico(rs.getString("tecnico"));
                lista.add(t);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar tickets activos: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Cambia el estado de un ticket Y deja constancia en Historial_Ticket.
     * Extra (HU-13 fuera de alcance); usado por vista/ListadoTickets.java.
     * Para el flujo normal de estados (HU-03/HU-04) usar resolver(),
     * asignarTecnico() o cambiarEstado(idTicket, nombreEstado) de mas abajo.
     */
    public boolean cambiarEstado(int idTicket, EstadoTicket nuevoEstado, int idEmpleado) {
        String sqlUpdate = "UPDATE Ticket SET id_estado = ? WHERE id_ticket = ?";
        String sqlHistorial = "INSERT INTO Historial_Ticket (id_ticket, id_empleado, accion) "
                             + "VALUES (?, ?, ?)";

        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);

            try (PreparedStatement psUpdate = cn.prepareStatement(sqlUpdate)) {
                psUpdate.setInt(1, nuevoEstado.getIdEstado());
                psUpdate.setInt(2, idTicket);
                psUpdate.executeUpdate();
            }

            try (PreparedStatement psHistorial = cn.prepareStatement(sqlHistorial)) {
                psHistorial.setInt(1, idTicket);
                psHistorial.setInt(2, idEmpleado);
                psHistorial.setString(3, "Estado cambiado a: " + nuevoEstado.getNombreEstado());
                psHistorial.executeUpdate();
            }

            cn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del ticket: " + e.getMessage());
            return false;
        }
    }

    // ======================================================================
    //  HU-01 / HU-02 — REGISTRO
    // ======================================================================

    /**
     * Inserta un ticket nuevo. El estado inicial siempre es "Abierto"
     * (no depende de lo que traiga el objeto Ticket).
     *
     * @param t datos cargados desde el formulario de RegistroTicket
     * @return el id_ticket generado, o -1 si fallo la insercion
     */
    public int insertar(Ticket t) {
        String sql = "INSERT INTO Ticket (id_usuario, id_dispositivo, id_ambito, id_estado, "
                   + "                    id_agente_creador, prioridad, naturaleza, "
                   + "                    lugar_fisico_exacto, descripcion_problema) "
                   + "VALUES (?, ?, ?, (SELECT id_estado FROM Estado_Ticket WHERE nombre_estado = 'Abierto'), "
                   + "        ?, ?, ?, ?, ?)";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, t.getIdUsuario());
            ps.setInt(2, t.getIdDispositivo());
            ps.setInt(3, t.getIdAmbito());
            ps.setInt(4, t.getIdAgenteCreador());
            ps.setString(5, t.getPrioridad());
            ps.setString(6, t.getNaturaleza());
            ps.setString(7, t.getLugarFisicoExacto());
            ps.setString(8, t.getDescripcionProblema());

            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar ticket: " + e.getMessage());
        }

        return -1;
    }

    // ======================================================================
    //  HU-08 / HU-16 — LISTADO GENERAL CON FILTROS
    // ======================================================================

    /**
     * Lista tickets de cualquier estado, con filtros opcionales por estado y
     * prioridad (null = sin filtro), para la pantalla de Listado de Tickets.
     *
     * @param filtroEstado    nombre de Estado_Ticket, o null para no filtrar
     * @param filtroPrioridad 'Alta'/'Media'/'Baja', o null para no filtrar
     * @return tickets encontrados, del mas reciente al mas antiguo
     */
    public List<Ticket> listar(String filtroEstado, String filtroPrioridad) {
        List<Ticket> lista = new ArrayList<>();

        String sql = "SELECT t.id_ticket, t.id_estado, t.fecha_hora_creacion, t.prioridad, "
                   + "       est.nombre_estado, a.nombre_ambito, "
                   + "       u.nombre_completo AS reporta, s.nombre_sucursal, "
                   + "       COALESCE(tec.nombre_completo, 'Sin asignar') AS tecnico "
                   + "FROM Ticket t "
                   + "INNER JOIN Estado_Ticket   est ON t.id_estado  = est.id_estado "
                   + "INNER JOIN Ambito          a   ON t.id_ambito  = a.id_ambito "
                   + "INNER JOIN Usuario_Cliente u   ON t.id_usuario = u.id_usuario "
                   + "INNER JOIN Sucursal        s   ON u.id_sucursal = s.id_sucursal "
                   + "LEFT  JOIN Empleado_Interno tec ON t.id_tecnico_asignado = tec.id_empleado "
                   + "WHERE (? IS NULL OR est.nombre_estado = ?) "
                   + "  AND (? IS NULL OR t.prioridad = ?) "
                   + "ORDER BY t.fecha_hora_creacion DESC";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, filtroEstado);
            ps.setString(2, filtroEstado);
            ps.setString(3, filtroPrioridad);
            ps.setString(4, filtroPrioridad);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ticket t = new Ticket();
                    t.setIdTicket(rs.getInt("id_ticket"));
                    t.setIdEstado(rs.getInt("id_estado"));
                    t.setFechaHoraCreacion(rs.getString("fecha_hora_creacion"));
                    t.setPrioridad(rs.getString("prioridad"));
                    t.setNombreEstado(rs.getString("nombre_estado"));
                    t.setNombreAmbito(rs.getString("nombre_ambito"));
                    t.setNombreUsuario(rs.getString("reporta"));
                    t.setNombreSucursal(rs.getString("nombre_sucursal"));
                    t.setNombreTecnico(rs.getString("tecnico"));
                    lista.add(t);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar tickets: " + e.getMessage());
        }

        return lista;
    }

    /**
     * HU-06: historial de tickets de un cliente. Con un solo cliente activo
     * (ServiAndina), el filtro demostrable es por sucursal o por usuario que
     * reporta; ambos son opcionales (null = no filtra por ese criterio).
     */
    public List<Ticket> listarPorCliente(Integer idSucursal, Integer idUsuario) {
        List<Ticket> lista = new ArrayList<>();

        String sql = "SELECT t.id_ticket, t.id_estado, t.fecha_hora_creacion, t.prioridad, "
                   + "       est.nombre_estado, a.nombre_ambito, "
                   + "       u.nombre_completo AS reporta, s.nombre_sucursal, "
                   + "       COALESCE(tec.nombre_completo, 'Sin asignar') AS tecnico "
                   + "FROM Ticket t "
                   + "INNER JOIN Estado_Ticket   est ON t.id_estado  = est.id_estado "
                   + "INNER JOIN Ambito          a   ON t.id_ambito  = a.id_ambito "
                   + "INNER JOIN Usuario_Cliente u   ON t.id_usuario = u.id_usuario "
                   + "INNER JOIN Sucursal        s   ON u.id_sucursal = s.id_sucursal "
                   + "LEFT  JOIN Empleado_Interno tec ON t.id_tecnico_asignado = tec.id_empleado "
                   + "WHERE (? IS NULL OR s.id_sucursal = ?) "
                   + "  AND (? IS NULL OR u.id_usuario = ?) "
                   + "ORDER BY t.fecha_hora_creacion DESC";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            setEnteroONulo(ps, 1, idSucursal);
            setEnteroONulo(ps, 2, idSucursal);
            setEnteroONulo(ps, 3, idUsuario);
            setEnteroONulo(ps, 4, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ticket t = new Ticket();
                    t.setIdTicket(rs.getInt("id_ticket"));
                    t.setIdEstado(rs.getInt("id_estado"));
                    t.setFechaHoraCreacion(rs.getString("fecha_hora_creacion"));
                    t.setPrioridad(rs.getString("prioridad"));
                    t.setNombreEstado(rs.getString("nombre_estado"));
                    t.setNombreAmbito(rs.getString("nombre_ambito"));
                    t.setNombreUsuario(rs.getString("reporta"));
                    t.setNombreSucursal(rs.getString("nombre_sucursal"));
                    t.setNombreTecnico(rs.getString("tecnico"));
                    lista.add(t);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar tickets por cliente: " + e.getMessage());
        }

        return lista;
    }

    /**
     * HU-07: historial completo de fallas de un equipo, por numero de serie.
     * Trae las mismas columnas "generales" que listar()/listarPorCliente()
     * (prioridad, reporta, sucursal, tecnico) para que vista/ListadoTickets.java
     * pueda mostrar una sola tabla sin importar cual filtro este activo.
     */
    public List<Ticket> listarPorDispositivo(String numeroSerie) {
        List<Ticket> lista = new ArrayList<>();

        String sql = "SELECT t.id_ticket, t.id_estado, t.fecha_hora_creacion, t.fecha_hora_cierre, t.prioridad, "
                   + "       a.nombre_ambito, t.naturaleza, est.nombre_estado, "
                   + "       u.nombre_completo AS reporta, s.nombre_sucursal, "
                   + "       COALESCE(tec.nombre_completo, 'Sin asignar') AS tecnico, "
                   + "       t.descripcion_problema, t.comentario_resolucion "
                   + "FROM Ticket t "
                   + "INNER JOIN Dispositivo     d   ON t.id_dispositivo = d.id_dispositivo "
                   + "INNER JOIN Ambito          a   ON t.id_ambito      = a.id_ambito "
                   + "INNER JOIN Estado_Ticket   est ON t.id_estado      = est.id_estado "
                   + "INNER JOIN Usuario_Cliente u   ON t.id_usuario     = u.id_usuario "
                   + "INNER JOIN Sucursal        s   ON u.id_sucursal    = s.id_sucursal "
                   + "LEFT  JOIN Empleado_Interno tec ON t.id_tecnico_asignado = tec.id_empleado "
                   + "WHERE d.numero_serie = ? "
                   + "ORDER BY t.fecha_hora_creacion DESC";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, numeroSerie);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ticket t = new Ticket();
                    t.setIdTicket(rs.getInt("id_ticket"));
                    t.setIdEstado(rs.getInt("id_estado"));
                    t.setFechaHoraCreacion(rs.getString("fecha_hora_creacion"));
                    t.setFechaHoraCierre(rs.getString("fecha_hora_cierre"));
                    t.setPrioridad(rs.getString("prioridad"));
                    t.setNombreAmbito(rs.getString("nombre_ambito"));
                    t.setNaturaleza(rs.getString("naturaleza"));
                    t.setNombreEstado(rs.getString("nombre_estado"));
                    t.setNombreUsuario(rs.getString("reporta"));
                    t.setNombreSucursal(rs.getString("nombre_sucursal"));
                    t.setNombreTecnico(rs.getString("tecnico"));
                    t.setDescripcionProblema(rs.getString("descripcion_problema"));
                    t.setComentarioResolucion(rs.getString("comentario_resolucion"));
                    lista.add(t);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar tickets por dispositivo: " + e.getMessage());
        }

        return lista;
    }

    // ======================================================================
    //  HU-04 / HU-05 — ASIGNACION
    // ======================================================================

    /** Tickets Abierto y sin tecnico asignado, para la pantalla de Asignacion. */
    public List<Ticket> listarAbiertosSinAsignar() {
        List<Ticket> lista = new ArrayList<>();

        String sql = "SELECT t.id_ticket, t.id_estado, t.id_ambito, t.fecha_hora_creacion, t.prioridad, "
                   + "       est.nombre_estado, a.nombre_ambito, "
                   + "       u.nombre_completo AS reporta, s.nombre_sucursal, "
                   + "       t.descripcion_problema "
                   + "FROM Ticket t "
                   + "INNER JOIN Estado_Ticket   est ON t.id_estado  = est.id_estado "
                   + "INNER JOIN Ambito          a   ON t.id_ambito  = a.id_ambito "
                   + "INNER JOIN Usuario_Cliente u   ON t.id_usuario = u.id_usuario "
                   + "INNER JOIN Sucursal        s   ON u.id_sucursal = s.id_sucursal "
                   + "WHERE est.nombre_estado = 'Abierto' "
                   + "  AND t.id_tecnico_asignado IS NULL "
                   + "ORDER BY FIELD(t.prioridad,'Alta','Media','Baja'), t.fecha_hora_creacion";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Ticket t = new Ticket();
                t.setIdTicket(rs.getInt("id_ticket"));
                t.setIdEstado(rs.getInt("id_estado"));
                t.setIdAmbito(rs.getInt("id_ambito"));
                t.setFechaHoraCreacion(rs.getString("fecha_hora_creacion"));
                t.setPrioridad(rs.getString("prioridad"));
                t.setNombreEstado(rs.getString("nombre_estado"));
                t.setNombreAmbito(rs.getString("nombre_ambito"));
                t.setNombreUsuario(rs.getString("reporta"));
                t.setNombreSucursal(rs.getString("nombre_sucursal"));
                t.setDescripcionProblema(rs.getString("descripcion_problema"));
                lista.add(t);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar tickets abiertos sin asignar: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Asigna un tecnico a un ticket y lo pasa a "En progreso".
     * La "notificacion" al tecnico (HU-05) es que el ticket aparecera en su
     * bandeja al llamar listarPorTecnico(); no hay aviso emergente ni correo.
     */
    public boolean asignarTecnico(int idTicket, int idTecnico) {
        String sql = "UPDATE Ticket "
                   + "SET id_tecnico_asignado = ?, "
                   + "    id_estado = (SELECT id_estado FROM Estado_Ticket WHERE nombre_estado = 'En progreso') "
                   + "WHERE id_ticket = ?";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idTecnico);
            ps.setInt(2, idTicket);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al asignar tecnico: " + e.getMessage());
            return false;
        }
    }

    /** Bandeja del tecnico: sus tickets activos (Abierto o En progreso). */
    public List<Ticket> listarPorTecnico(int idTecnico) {
        List<Ticket> lista = new ArrayList<>();

        String sql = "SELECT t.id_ticket, t.id_estado, t.fecha_hora_creacion, t.prioridad, "
                   + "       est.nombre_estado, a.nombre_ambito, s.nombre_sucursal, "
                   + "       t.lugar_fisico_exacto, d.tipo_hardware, "
                   + "       u.nombre_completo AS reporta "
                   + "FROM Ticket t "
                   + "INNER JOIN Estado_Ticket   est ON t.id_estado      = est.id_estado "
                   + "INNER JOIN Ambito          a   ON t.id_ambito      = a.id_ambito "
                   + "INNER JOIN Usuario_Cliente u   ON t.id_usuario     = u.id_usuario "
                   + "INNER JOIN Sucursal        s   ON u.id_sucursal    = s.id_sucursal "
                   + "INNER JOIN Dispositivo     d   ON t.id_dispositivo = d.id_dispositivo "
                   + "WHERE t.id_tecnico_asignado = ? "
                   + "  AND est.nombre_estado IN ('Abierto','En progreso') "
                   + "ORDER BY FIELD(t.prioridad,'Alta','Media','Baja'), t.fecha_hora_creacion";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idTecnico);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ticket t = new Ticket();
                    t.setIdTicket(rs.getInt("id_ticket"));
                    t.setIdEstado(rs.getInt("id_estado"));
                    t.setFechaHoraCreacion(rs.getString("fecha_hora_creacion"));
                    t.setPrioridad(rs.getString("prioridad"));
                    t.setNombreEstado(rs.getString("nombre_estado"));
                    t.setNombreAmbito(rs.getString("nombre_ambito"));
                    t.setNombreSucursal(rs.getString("nombre_sucursal"));
                    t.setLugarFisicoExacto(rs.getString("lugar_fisico_exacto"));
                    t.setTipoHardwareDispositivo(rs.getString("tipo_hardware"));
                    t.setNombreUsuario(rs.getString("reporta"));
                    lista.add(t);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar tickets por tecnico: " + e.getMessage());
        }

        return lista;
    }

    // ======================================================================
    //  HU-03 — DETALLE Y FLUJO DE ESTADOS
    // ======================================================================

    /** Detalle completo de un ticket (todos los JOIN), para vista/DetalleTicket.java. */
    public Ticket buscarPorId(int idTicket) {
        String sql = "SELECT t.id_ticket, t.id_estado, t.id_tecnico_asignado, "
                   + "       ec.razon_social, s.nombre_sucursal, "
                   + "       u.codigo_usuario, u.nombre_completo AS reporta, "
                   + "       d.numero_serie, d.tipo_hardware, d.modelo, "
                   + "       a.nombre_ambito, t.naturaleza, t.prioridad, est.nombre_estado, "
                   + "       t.lugar_fisico_exacto, t.descripcion_problema, t.comentario_resolucion, "
                   + "       ag.nombre_completo  AS registrado_por, "
                   + "       tec.nombre_completo AS atendido_por, "
                   + "       t.fecha_hora_creacion, t.fecha_hora_cierre "
                   + "FROM Ticket t "
                   + "INNER JOIN Usuario_Cliente  u   ON t.id_usuario          = u.id_usuario "
                   + "INNER JOIN Sucursal         s   ON u.id_sucursal         = s.id_sucursal "
                   + "INNER JOIN Empresa_Cliente  ec  ON s.id_empresa          = ec.id_empresa "
                   + "INNER JOIN Dispositivo      d   ON t.id_dispositivo      = d.id_dispositivo "
                   + "INNER JOIN Ambito           a   ON t.id_ambito           = a.id_ambito "
                   + "INNER JOIN Estado_Ticket    est ON t.id_estado           = est.id_estado "
                   + "INNER JOIN Empleado_Interno ag  ON t.id_agente_creador   = ag.id_empleado "
                   + "LEFT  JOIN Empleado_Interno tec ON t.id_tecnico_asignado = tec.id_empleado "
                   + "WHERE t.id_ticket = ?";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idTicket);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ticket t = new Ticket();
                    t.setIdTicket(rs.getInt("id_ticket"));
                    t.setIdEstado(rs.getInt("id_estado"));

                    int idTecnico = rs.getInt("id_tecnico_asignado");
                    t.setIdTecnicoAsignado(rs.wasNull() ? null : idTecnico);

                    t.setRazonSocial(rs.getString("razon_social"));
                    t.setNombreSucursal(rs.getString("nombre_sucursal"));
                    t.setCodigoUsuario(rs.getString("codigo_usuario"));
                    t.setNombreUsuario(rs.getString("reporta"));
                    t.setNumeroSerieDispositivo(rs.getString("numero_serie"));
                    t.setTipoHardwareDispositivo(rs.getString("tipo_hardware"));
                    t.setModeloDispositivo(rs.getString("modelo"));
                    t.setNombreAmbito(rs.getString("nombre_ambito"));
                    t.setNaturaleza(rs.getString("naturaleza"));
                    t.setPrioridad(rs.getString("prioridad"));
                    t.setNombreEstado(rs.getString("nombre_estado"));
                    t.setLugarFisicoExacto(rs.getString("lugar_fisico_exacto"));
                    t.setDescripcionProblema(rs.getString("descripcion_problema"));
                    t.setComentarioResolucion(rs.getString("comentario_resolucion"));
                    t.setNombreAgenteCreador(rs.getString("registrado_por"));
                    t.setNombreTecnico(rs.getString("atendido_por"));
                    t.setFechaHoraCreacion(rs.getString("fecha_hora_creacion"));
                    t.setFechaHoraCierre(rs.getString("fecha_hora_cierre"));

                    return t;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar ticket por id: " + e.getMessage());
        }

        return null;
    }

    /** Marca un ticket como Resuelto: guarda el comentario y la fecha de cierre. */
    public boolean resolver(int idTicket, String comentarioResolucion) {
        String sql = "UPDATE Ticket "
                   + "SET id_estado = (SELECT id_estado FROM Estado_Ticket WHERE nombre_estado = 'Resuelto'), "
                   + "    comentario_resolucion = ?, "
                   + "    fecha_hora_cierre = CURRENT_TIMESTAMP "
                   + "WHERE id_ticket = ?";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, comentarioResolucion);
            ps.setInt(2, idTicket);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al marcar ticket como resuelto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cambio de estado generico por nombre (por ejemplo, pasar de "Resuelto"
     * a "Cerrado"). A diferencia de cambiarEstado(idTicket, EstadoTicket,
     * idEmpleado) de mas arriba, este NO escribe en Historial_Ticket.
     */
    public boolean cambiarEstado(int idTicket, String nombreEstado) {
        String sql = "UPDATE Ticket "
                   + "SET id_estado = (SELECT id_estado FROM Estado_Ticket WHERE nombre_estado = ?) "
                   + "WHERE id_ticket = ?";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, nombreEstado);
            ps.setInt(2, idTicket);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del ticket: " + e.getMessage());
            return false;
        }
    }

    /** Aplica un Integer nullable a un parametro, para el patron "? IS NULL OR ...". */
    private void setEnteroONulo(PreparedStatement ps, int indice, Integer valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.INTEGER);
        } else {
            ps.setInt(indice, valor);
        }
    }

    // ======================================================================
    //  METODO DE PRUEBA
    //  Clic derecho en este archivo > Run File para probar el DAO
    //  sin necesidad de tener las pantallas construidas todavia.
    // ======================================================================
    public static void main(String[] args) {
        TicketDAO dao = new TicketDAO();

        System.out.println("=== PRUEBA 1: listar() sin filtros (todos los tickets) ===");
        for (Ticket t : dao.listar(null, null)) {
            System.out.println("   #" + t.getIdTicket() + " | " + t.getNombreEstado()
                    + " | " + t.getPrioridad() + " | " + t.getNombreSucursal());
        }

        System.out.println("\n=== PRUEBA 2: listar(estado='Cerrado') ===");
        for (Ticket t : dao.listar("Cerrado", null)) {
            System.out.println("   #" + t.getIdTicket() + " | " + t.getNombreEstado());
        }

        System.out.println("\n=== PRUEBA 3: listarAbiertosSinAsignar() ===");
        for (Ticket t : dao.listarAbiertosSinAsignar()) {
            System.out.println("   #" + t.getIdTicket() + " | " + t.getPrioridad() + " | " + t.getNombreSucursal());
        }

        System.out.println("\n=== PRUEBA 4: listarPorTecnico(5) ===");
        for (Ticket t : dao.listarPorTecnico(5)) {
            System.out.println("   #" + t.getIdTicket() + " | " + t.getNombreEstado());
        }

        System.out.println("\n=== PRUEBA 5: buscarPorId(3) ===");
        Ticket detalle = dao.buscarPorId(3);
        System.out.println(detalle != null
                ? "   " + detalle.getRazonSocial() + " | " + detalle.getNombreSucursal()
                        + " | " + detalle.getNombreUsuario() + " | " + detalle.getNombreEstado()
                : "   No encontrado.");

        System.out.println("\n=== PRUEBA 6: listarPorCliente(idSucursal=1, idUsuario=null) ===");
        for (Ticket t : dao.listarPorCliente(1, null)) {
            System.out.println("   #" + t.getIdTicket() + " | " + t.getNombreSucursal());
        }

        System.out.println("\n=== PRUEBA 7: listarPorDispositivo('SN-IMP-00123') ===");
        for (Ticket t : dao.listarPorDispositivo("SN-IMP-00123")) {
            System.out.println("   #" + t.getIdTicket() + " | " + t.getNombreEstado());
        }

        // ------------------------------------------------------------------
        // PRUEBAS DE ESCRITURA (deshabilitadas por defecto)
        // insertar/asignarTecnico/resolver/cambiarEstado SI modifican datos.
        // Descomentar solo para probar manualmente.
        // ------------------------------------------------------------------
        // Ticket nuevo = new Ticket();
        // nuevo.setIdUsuario(1); nuevo.setIdDispositivo(2); nuevo.setIdAmbito(4);
        // nuevo.setIdAgenteCreador(2); nuevo.setPrioridad("Media"); nuevo.setNaturaleza("Logica");
        // nuevo.setLugarFisicoExacto("Prueba TicketDAO"); nuevo.setDescripcionProblema("Prueba de insercion.");
        // System.out.println("Ticket insertado con id: " + dao.insertar(nuevo));
    }
}
