package modelo;

/**
 * Clase modelo del USUARIO CLIENTE: el trabajador de la empresa cliente que
 * reporta una falla por telefono. Equivale a una fila de la tabla Usuario_Cliente.
 *
 * (Mismo patron que Empleado: atributos privados + get/set.)
 */
public class Usuario {

    private int    idUsuario;       // id_usuario (PK)
    private int    idSucursal;      // id_sucursal (FK)
    private String codigoUsuario;   // codigo_usuario (ej: Mjopi001)
    private String rut;             // rut
    private String nombreCompleto;  // nombre_completo
    private String telefono;        // telefono
    private String correo;          // correo

    // Campos extra rellenados por JOIN (para mostrar en pantalla):
    private String nombreSucursal;  // de la tabla Sucursal
    private String razonSocial;     // de la tabla Empresa_Cliente

    public Usuario() {
    }

    public int getIdUsuario()              { return idUsuario; }
    public void setIdUsuario(int v)        { this.idUsuario = v; }

    public int getIdSucursal()             { return idSucursal; }
    public void setIdSucursal(int v)       { this.idSucursal = v; }

    public String getCodigoUsuario()       { return codigoUsuario; }
    public void setCodigoUsuario(String v) { this.codigoUsuario = v; }

    public String getRut()                 { return rut; }
    public void setRut(String v)           { this.rut = v; }

    public String getNombreCompleto()      { return nombreCompleto; }
    public void setNombreCompleto(String v){ this.nombreCompleto = v; }

    public String getTelefono()            { return telefono; }
    public void setTelefono(String v)      { this.telefono = v; }

    public String getCorreo()              { return correo; }
    public void setCorreo(String v)        { this.correo = v; }

    public String getNombreSucursal()      { return nombreSucursal; }
    public void setNombreSucursal(String v){ this.nombreSucursal = v; }

    public String getRazonSocial()         { return razonSocial; }
    public void setRazonSocial(String v)   { this.razonSocial = v; }

    @Override
    public String toString() {
        return codigoUsuario + " - " + nombreCompleto;
    }
}
