package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Empleado;
import util.Conexion;

/**
 * DAO = Data Access Object (Objeto de Acceso a Datos).
 *
 * ¿QUE HACE ESTA CLASE?
 * Aqui vive TODO el SQL relacionado con la tabla Empleado_Interno.
 * La regla es simple: la pantalla no sabe SQL, y el DAO no sabe de botones.
 * Cada capa hace lo suyo.
 *
 * FLUJO TIPICO:
 *   Pantalla Login  ->  EmpleadoDAO.login(usuario, clave)
 *                   ->  consulta la base de datos
 *                   ->  devuelve un objeto Empleado (o null si no existe)
 *
 * Historia de usuario asociada: HU-12 (roles diferenciados / inicio de sesion)
 */
public class EmpleadoDAO {

    /**
     * Valida las credenciales de un empleado e inicia sesion.
     *
     * @param usuario   nombre de usuario escrito en la pantalla
     * @param contrasena contrasena escrita en la pantalla
     * @return el Empleado si las credenciales son correctas; null si no lo son
     */
    public Empleado login(String usuario, String contrasena) {

        // ------------------------------------------------------------------
        // 1. LA CONSULTA SQL
        //
        // Fijarse en los signos '?': son PARAMETROS. No se escriben los datos
        // directamente en el texto del SQL.
        //
        // ¿POR QUE? Por seguridad. Si escribieramos:
        //     "... WHERE usuario = '" + usuario + "'"
        // alguien podria escribir en el campo usuario algo como:  ' OR '1'='1
        // y entrar sin contrasena. Eso se llama INYECCION SQL.
        // Con '?' la base trata el valor como dato, nunca como codigo.
        //
        // El JOIN con Rol es para traer tambien el nombre del rol en texto
        // ("Supervisor", "Tecnico"), que necesitamos para saber que menu mostrar.
        // ------------------------------------------------------------------
        String sql = "SELECT e.id_empleado, e.id_rol, e.id_grupo, "
                   + "       e.nombre_completo, e.usuario, e.estado_activo, "
                   + "       r.nombre_rol "
                   + "FROM Empleado_Interno e "
                   + "INNER JOIN Rol r ON e.id_rol = r.id_rol "
                   + "WHERE e.usuario = ? "
                   + "  AND e.contrasena_hash = ? "
                   + "  AND e.estado_activo = 1";

        // try-with-resources: lo que se abre dentro del try( ... ) se cierra
        // solo al terminar, aunque ocurra un error. Evita dejar conexiones
        // abiertas, que es un problema clasico.
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2. RELLENAR LOS PARAMETROS
            // El 1 es el primer '?', el 2 es el segundo. El orden importa.
            ps.setString(1, usuario);
            ps.setString(2, contrasena);

            // 3. EJECUTAR Y LEER EL RESULTADO
            try (ResultSet rs = ps.executeQuery()) {

                // rs.next() avanza a la primera fila. Si devuelve true,
                // encontro un empleado con esas credenciales.
                if (rs.next()) {
                    // 4. PASAR LOS DATOS DE LA FILA AL OBJETO Empleado
                    Empleado emp = new Empleado();
                    emp.setIdEmpleado(rs.getInt("id_empleado"));
                    emp.setIdRol(rs.getInt("id_rol"));

                    // id_grupo puede venir NULL en la base (solo los tecnicos
                    // tienen grupo). getInt devolveria 0, por eso preguntamos
                    // con wasNull() si el valor era realmente nulo.
                    int grupo = rs.getInt("id_grupo");
                    emp.setIdGrupo(rs.wasNull() ? null : grupo);

                    emp.setNombreCompleto(rs.getString("nombre_completo"));
                    emp.setUsuario(rs.getString("usuario"));
                    emp.setEstadoActivo(rs.getBoolean("estado_activo"));
                    emp.setNombreRol(rs.getString("nombre_rol"));

                    return emp;   // login correcto
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en login: " + e.getMessage());
        }

        return null;   // usuario o contrasena incorrectos
    }

    /**
     * Lista los tecnicos que pueden atender un ambito determinado.
     *
     * Esta consulta muestra en la practica la decision de diseno del modelo:
     * el Ticket guarda solo id_ambito, y el grupo resolutor se deduce con
     * JOIN (Ambito -> Grupo_Resolutor -> Empleado_Interno).
     *
     * Historia de usuario asociada: HU-04 (asignacion por supervisor)
     *
     * @param idAmbito ambito tecnico del ticket
     * @return lista de tecnicos activos de ese ambito
     */
    public List<Empleado> listarTecnicosPorAmbito(int idAmbito) {
        List<Empleado> lista = new ArrayList<>();

        String sql = "SELECT emp.id_empleado, emp.nombre_completo, "
                   + "       emp.id_grupo, r.nombre_rol "
                   + "FROM Empleado_Interno emp "
                   + "INNER JOIN Rol r             ON emp.id_rol   = r.id_rol "
                   + "INNER JOIN Grupo_Resolutor g ON emp.id_grupo = g.id_grupo "
                   + "WHERE g.id_ambito     = ? "
                   + "  AND r.nombre_rol    = 'Tecnico' "
                   + "  AND emp.estado_activo = 1 "
                   + "ORDER BY emp.nombre_completo";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idAmbito);

            try (ResultSet rs = ps.executeQuery()) {
                // while (no if): recorremos TODAS las filas del resultado.
                while (rs.next()) {
                    Empleado emp = new Empleado();
                    emp.setIdEmpleado(rs.getInt("id_empleado"));
                    emp.setNombreCompleto(rs.getString("nombre_completo"));
                    int grupo = rs.getInt("id_grupo");
                    emp.setIdGrupo(rs.wasNull() ? null : grupo);
                    emp.setNombreRol(rs.getString("nombre_rol"));
                    lista.add(emp);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar tecnicos: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Lista todo el personal interno con su rol.
     * Sirve para la pantalla de administracion de usuarios.
     */
    public List<Empleado> listarTodos() {
        List<Empleado> lista = new ArrayList<>();

        String sql = "SELECT e.id_empleado, e.id_rol, e.id_grupo, "
                   + "       e.nombre_completo, e.usuario, e.estado_activo, "
                   + "       r.nombre_rol "
                   + "FROM Empleado_Interno e "
                   + "INNER JOIN Rol r ON e.id_rol = r.id_rol "
                   + "ORDER BY r.nombre_rol, e.nombre_completo";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Empleado emp = new Empleado();
                emp.setIdEmpleado(rs.getInt("id_empleado"));
                emp.setIdRol(rs.getInt("id_rol"));
                int grupo = rs.getInt("id_grupo");
                emp.setIdGrupo(rs.wasNull() ? null : grupo);
                emp.setNombreCompleto(rs.getString("nombre_completo"));
                emp.setUsuario(rs.getString("usuario"));
                emp.setEstadoActivo(rs.getBoolean("estado_activo"));
                emp.setNombreRol(rs.getString("nombre_rol"));
                lista.add(emp);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar empleados: " + e.getMessage());
        }

        return lista;
    }

    // ======================================================================
    //  METODO DE PRUEBA
    //  Clic derecho en este archivo > Run File para probar el login
    //  sin necesidad de tener la pantalla construida todavia.
    // ======================================================================
    public static void main(String[] args) {
        EmpleadoDAO dao = new EmpleadoDAO();

        System.out.println("=== PRUEBA 1: login correcto ===");
        Empleado emp = dao.login("supervisor", "hash_demo_super");
        if (emp != null) {
            System.out.println("Acceso concedido:");
            System.out.println("   Nombre : " + emp.getNombreCompleto());
            System.out.println("   Rol    : " + emp.getNombreRol());
            System.out.println("   ID     : " + emp.getIdEmpleado());
        } else {
            System.out.println("Acceso denegado.");
        }

        System.out.println("\n=== PRUEBA 2: contrasena incorrecta ===");
        Empleado malo = dao.login("supervisor", "clave_equivocada");
        System.out.println(malo == null
                ? "Acceso denegado (correcto, la clave estaba mal)."
                : "ERROR: no deberia haber entrado.");

        System.out.println("\n=== PRUEBA 3: tecnicos del ambito 1 (Impresion) ===");
        for (Empleado t : dao.listarTecnicosPorAmbito(1)) {
            System.out.println("   - " + t.getNombreCompleto());
        }

        System.out.println("\n=== PRUEBA 4: todo el personal ===");
        for (Empleado e : dao.listarTodos()) {
            System.out.println("   " + e.getNombreRol() + ": " + e.getNombreCompleto());
        }
    }
}
