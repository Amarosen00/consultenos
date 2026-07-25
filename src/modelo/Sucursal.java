package modelo;

/**
 * Clase modelo simple de Sucursal: solo id + nombre, para llenar combos
 * de seleccion (CatalogoDAO.listarSucursales()). Si mas adelante alguna
 * pantalla necesita direccion o la empresa dueña, se amplia aqui.
 *
 * Tabla asociada: Sucursal
 */
public class Sucursal {

    private int    idSucursal;      // id_sucursal (PK)
    private String nombreSucursal;  // nombre_sucursal

    public Sucursal() {
    }

    public Sucursal(int idSucursal, String nombreSucursal) {
        this.idSucursal     = idSucursal;
        this.nombreSucursal = nombreSucursal;
    }

    public int getIdSucursal()               { return idSucursal; }
    public void setIdSucursal(int v)         { this.idSucursal = v; }

    public String getNombreSucursal()        { return nombreSucursal; }
    public void setNombreSucursal(String v)  { this.nombreSucursal = v; }

    @Override
    public String toString() {
        return nombreSucursal;
    }
}
