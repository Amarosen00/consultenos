package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase encargada de establecer la conexion con la base de datos MySQL/MariaDB.
 *
 * ¿POR QUE UNA CLASE SOLO PARA ESTO?
 * Porque todas las demas clases del sistema necesitan hablar con la base de
 * datos. Si cada una escribiera su propia conexion, tendriamos el usuario y la
 * contrasena repetidos por todo el proyecto. Al centralizarlo aqui, si cambia
 * la contrasena de MySQL solo se modifica ESTE archivo.
 *
 * Proyecto: Consultenos - Mesa de Ayuda B2B
 * Asignatura: Taller de Desarrollo de Aplicaciones - INACAP Calama 2026
 */
public class Conexion {

    // ------------------------------------------------------------------
    // PARAMETROS DE CONEXION
    // ------------------------------------------------------------------
    // Desglose de la URL:
    //   jdbc:mysql://  -> protocolo: le dice a Java que use el driver de MySQL
    //   localhost      -> el servidor esta en este mismo computador (XAMPP)
    //   :3306          -> puerto por defecto de MySQL
    //   /consultenos_db-> nombre de nuestra base de datos
    //
    // Los parametros despues del "?" evitan problemas tipicos:
    //   useSSL=false            -> XAMPP local no usa SSL; sin esto lanza warning
    //   allowPublicKeyRetrieval -> evita error de autenticacion en MySQL 8+
    //   serverTimezone=America/Santiago -> sin esto, las fechas se guardan
    //                                      con horas corridas (error clasico)
    //   characterEncoding=UTF-8 -> para que las tildes y la ñ no salgan rotas
    private static final String URL =
        "jdbc:mysql://localhost:3306/consultenos_db"
        + "?useSSL=false"
        + "&allowPublicKeyRetrieval=true"
        + "&serverTimezone=America/Santiago"
        + "&characterEncoding=UTF-8";

    // Usuario por defecto de XAMPP. Si le pusieron contrasena a MySQL,
    // cambienla en la linea de abajo.
    private static final String USUARIO = "root";
    private static final String CLAVE   = "";

    /**
     * Entrega una conexion abierta a la base de datos.
     *
     * @return objeto Connection listo para ejecutar consultas
     * @throws SQLException si no logra conectar (XAMPP apagado, clave mala, etc.)
     */
    public static Connection obtener() throws SQLException {
        try {
            // Carga el driver JDBC de MySQL.
            // Si esta linea falla, es porque falta agregar el .jar del conector
            // a las librerias del proyecto en NetBeans.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "No se encontro el driver de MySQL.\n"
                + "Solucion: click derecho en el proyecto > Properties > Libraries\n"
                + "         > Add JAR/Folder > seleccionar mysql-connector-j-x.x.x.jar",
                e);
        }

        // DriverManager usa la URL para saber a que motor y base conectarse,
        // y devuelve la conexion ya autenticada.
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    /**
     * Cierra una conexion de forma segura.
     *
     * ¿POR QUE HAY QUE CERRAR?
     * Cada conexion abierta consume un recurso del servidor MySQL. Si nunca se
     * cierran, despues de un rato el servidor rechaza nuevas conexiones
     * ("too many connections"). Por eso: se abre, se usa, se cierra.
     *
     * @param cn la conexion a cerrar (acepta null sin reventar)
     */
    public static void cerrar(Connection cn) {
        if (cn != null) {
            try {
                cn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexion: " + e.getMessage());
            }
        }
    }

    /**
     * Metodo de prueba: verifica que la conexion funcione.
     *
     * COMO USARLO: click derecho en este archivo > Run File.
     * Si todo esta bien, imprime los datos del servidor.
     * Este metodo es solo para probar; la aplicacion real no lo usa.
     */
    public static void main(String[] args) {
        System.out.println("Probando conexion a la base de datos...\n");

        Connection cn = null;
        try {
            cn = obtener();
            System.out.println("CONEXION EXITOSA");
            System.out.println("  Base de datos : " + cn.getCatalog());
            System.out.println("  Motor         : " + cn.getMetaData().getDatabaseProductName());
            System.out.println("  Version       : " + cn.getMetaData().getDatabaseProductVersion());
            System.out.println("  Driver        : " + cn.getMetaData().getDriverVersion());
            System.out.println("\nTodo listo para trabajar.");

        } catch (SQLException e) {
            System.err.println("FALLO LA CONEXION");
            System.err.println("  Detalle: " + e.getMessage());
            System.err.println("\nRevisar en este orden:");
            System.err.println("  1. ¿Esta XAMPP encendido con MySQL en verde?");
            System.err.println("  2. ¿Ejecutaron el script 01_crear_bd_consultenos.sql?");
            System.err.println("  3. ¿Agregaron el .jar del conector a las librerias?");
            System.err.println("  4. ¿La contrasena de root es correcta en esta clase?");

        } finally {
            // El bloque finally SIEMPRE se ejecuta, haya error o no.
            // Por eso es el lugar correcto para cerrar la conexion.
            cerrar(cn);
        }
    }
}
