package modelo;

/**
 * Clase modelo que representa a un EMPLEADO INTERNO de Consultenos
 * (administrador, agente, supervisor o tecnico).
 *
 * ¿QUE ES UNA CLASE MODELO?
 * Es el "molde" de una fila de la tabla Empleado_Interno. Cada objeto de esta
 * clase equivale a un registro de la tabla, pero viviendo en memoria de Java.
 * Sirve para transportar los datos entre las capas del sistema:
 *   base de datos  ->  DAO  ->  objeto Empleado  ->  pantalla.
 *
 * PATRON: por cada columna de la tabla hay un ATRIBUTO privado, y por cada
 * atributo hay un metodo get (leer) y un metodo set (escribir). A esto se le
 * llama "encapsulamiento": los datos son privados y se acceden por metodos.
 *
 * Tabla asociada: Empleado_Interno
 */
public class Empleado {

    // ------------------------------------------------------------------
    // ATRIBUTOS  (uno por cada columna relevante de la tabla)
    // Son 'private' para que nadie los modifique directamente desde afuera;
    // solo se tocan a traves de los get/set de mas abajo.
    // ------------------------------------------------------------------
    private int     idEmpleado;      // id_empleado  (clave primaria)
    private int     idRol;           // id_rol       (FK: que rol tiene)
    private Integer idGrupo;         // id_grupo     (FK: puede ser null -> por eso Integer, no int)
    private String  nombreCompleto;  // nombre_completo
    private String  usuario;         // usuario      (para el login)
    private String  contrasenaHash;  // contrasena_hash
    private boolean estadoActivo;    // estado_activo

    // Campo extra (no es columna): el nombre del rol en texto.
    // Sirve para mostrar "Supervisor" en pantalla sin consultar la tabla Rol
    // cada vez. El DAO lo rellena con un JOIN.
    private String  nombreRol;

    // ------------------------------------------------------------------
    // CONSTRUCTORES
    // Un constructor es el metodo que se llama al crear el objeto con 'new'.
    // ------------------------------------------------------------------

    /** Constructor vacio: crea un Empleado "en blanco" para llenarlo con set. */
    public Empleado() {
    }

    /** Constructor con los datos principales, util para el login. */
    public Empleado(int idEmpleado, String nombreCompleto, String usuario, String nombreRol) {
        this.idEmpleado     = idEmpleado;
        this.nombreCompleto = nombreCompleto;
        this.usuario        = usuario;
        this.nombreRol      = nombreRol;
    }

    // ------------------------------------------------------------------
    // GETTERS y SETTERS
    // get -> devuelve el valor de un atributo.
    // set -> cambia el valor de un atributo.
    // NetBeans puede generarlos solos: clic derecho > Insert Code >
    //   Getter and Setter. Aqui van escritos para que se entiendan.
    // ------------------------------------------------------------------

    public int getIdEmpleado() {
        return idEmpleado;
    }
    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public int getIdRol() {
        return idRol;
    }
    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public Integer getIdGrupo() {
        return idGrupo;
    }
    public void setIdGrupo(Integer idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getUsuario() {
        return usuario;
    }
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }
    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public boolean isEstadoActivo() {
        return estadoActivo;
    }
    public void setEstadoActivo(boolean estadoActivo) {
        this.estadoActivo = estadoActivo;
    }

    public String getNombreRol() {
        return nombreRol;
    }
    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    // ------------------------------------------------------------------
    // toString: define como se "imprime" el objeto. Util para depurar.
    // ------------------------------------------------------------------
    @Override
    public String toString() {
        return nombreCompleto + " (" + nombreRol + ")";
    }
}
