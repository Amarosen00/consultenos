package modelo;

/**
 * Clase modelo de una fila de Historial_Ticket, ya con los nombres resueltos
 * por JOIN (responsable, su rol, y el estado actual del ticket al que
 * pertenece). Se usa para la pantalla de Historial y Busqueda.
 *
 * Tabla asociada: Historial_Ticket
 */
public class HistorialTicket {

    private int    idHistorial;          // id_historial (PK)
    private int    idTicket;             // id_ticket
    private String fechaHoraAccion;      // fecha_hora_accion
    private String accion;               // accion (texto libre de la auditoria)

    // Campos extra rellenados por JOIN (para mostrar en pantalla):
    private String nombreResponsable;    // de Empleado_Interno, via id_empleado
    private String nombreRolResponsable; // de Rol, via Empleado_Interno.id_rol
    private String nombreEstadoActual;   // de Estado_Ticket, estado ACTUAL del ticket (no el de ese momento)

    public HistorialTicket() {
    }

    public int getIdHistorial()               { return idHistorial; }
    public void setIdHistorial(int v)         { this.idHistorial = v; }

    public int getIdTicket()                  { return idTicket; }
    public void setIdTicket(int v)            { this.idTicket = v; }

    public String getFechaHoraAccion()        { return fechaHoraAccion; }
    public void setFechaHoraAccion(String v)  { this.fechaHoraAccion = v; }

    public String getAccion()                 { return accion; }
    public void setAccion(String v)           { this.accion = v; }

    public String getNombreResponsable()      { return nombreResponsable; }
    public void setNombreResponsable(String v){ this.nombreResponsable = v; }

    public String getNombreRolResponsable()       { return nombreRolResponsable; }
    public void setNombreRolResponsable(String v) { this.nombreRolResponsable = v; }

    public String getNombreEstadoActual()      { return nombreEstadoActual; }
    public void setNombreEstadoActual(String v){ this.nombreEstadoActual = v; }

    @Override
    public String toString() {
        return "Historial #" + idHistorial + " - ticket #" + idTicket;
    }
}
