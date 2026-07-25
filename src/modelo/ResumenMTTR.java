package modelo;

/**
 * Resultado del calculo de MTTR (Mean Time To Resolve, HU-11): tiempo
 * promedio de resolucion en horas, entre creacion y cierre del ticket.
 *
 * horasPromedio es Double (no double) porque el AVG de SQL da NULL cuando
 * todavia no hay ningun ticket cerrado.
 */
public class ResumenMTTR {

    private Double horasPromedio;
    private int    ticketsConsiderados;

    public ResumenMTTR() {
    }

    public ResumenMTTR(Double horasPromedio, int ticketsConsiderados) {
        this.horasPromedio = horasPromedio;
        this.ticketsConsiderados = ticketsConsiderados;
    }

    public Double getHorasPromedio()       { return horasPromedio; }
    public void setHorasPromedio(Double v) { this.horasPromedio = v; }

    public int getTicketsConsiderados()       { return ticketsConsiderados; }
    public void setTicketsConsiderados(int v) { this.ticketsConsiderados = v; }
}
