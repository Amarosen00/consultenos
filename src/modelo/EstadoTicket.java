package modelo;

/**
 * Clase modelo del catalogo Estado_Ticket (Abierto, En progreso, Resuelto, Cerrado).
 *
 * Se usa para llenar combos en las pantallas (elegir el nuevo estado de un
 * ticket, o filtrar el historial por estado), en vez de escribir los nombres
 * de estado a mano en cada pantalla.
 *
 * Tabla asociada: Estado_Ticket
 */
public class EstadoTicket {

    private int    idEstado;      // id_estado (PK)
    private String nombreEstado;  // nombre_estado

    public EstadoTicket() {
    }

    public EstadoTicket(int idEstado, String nombreEstado) {
        this.idEstado     = idEstado;
        this.nombreEstado = nombreEstado;
    }

    public int getIdEstado()             { return idEstado; }
    public void setIdEstado(int v)       { this.idEstado = v; }

    public String getNombreEstado()      { return nombreEstado; }
    public void setNombreEstado(String v){ this.nombreEstado = v; }

    // toString define que texto se ve cuando este objeto se pone directo
    // en un JComboBox (Swing llama a toString() para dibujar cada opcion).
    @Override
    public String toString() {
        return nombreEstado;
    }
}
