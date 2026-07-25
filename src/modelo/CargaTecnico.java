package modelo;

/** Fila del reporte "carga de trabajo por tecnico" (HU-11). */
public class CargaTecnico {

    private String nombreTecnico;
    private String nombreGrupo;      // puede ser null si el tecnico no tiene grupo asignado
    private int    ticketsAsignados;
    private int    resueltos;        // Resuelto + Cerrado

    public CargaTecnico() {
    }

    public String getNombreTecnico()       { return nombreTecnico; }
    public void setNombreTecnico(String v) { this.nombreTecnico = v; }

    public String getNombreGrupo()       { return nombreGrupo; }
    public void setNombreGrupo(String v) { this.nombreGrupo = v; }

    public int getTicketsAsignados()       { return ticketsAsignados; }
    public void setTicketsAsignados(int v) { this.ticketsAsignados = v; }

    public int getResueltos()       { return resueltos; }
    public void setResueltos(int v) { this.resueltos = v; }
}
