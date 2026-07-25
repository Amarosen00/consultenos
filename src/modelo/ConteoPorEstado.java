package modelo;

/**
 * Fila del reporte "tickets por estado" (HU-11): cuantos tickets hay
 * actualmente en cada estado del catalogo Estado_Ticket.
 */
public class ConteoPorEstado {

    private String nombreEstado;
    private int    cantidad;

    public ConteoPorEstado() {
    }

    public ConteoPorEstado(String nombreEstado, int cantidad) {
        this.nombreEstado = nombreEstado;
        this.cantidad = cantidad;
    }

    public String getNombreEstado()       { return nombreEstado; }
    public void setNombreEstado(String v) { this.nombreEstado = v; }

    public int getCantidad()        { return cantidad; }
    public void setCantidad(int v)  { this.cantidad = v; }
}
