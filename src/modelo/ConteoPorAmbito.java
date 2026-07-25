package modelo;

/** Fila del reporte "tickets por ambito" (HU-11): demanda por ambito tecnico. */
public class ConteoPorAmbito {

    private String nombreAmbito;
    private int    total;

    public ConteoPorAmbito() {
    }

    public ConteoPorAmbito(String nombreAmbito, int total) {
        this.nombreAmbito = nombreAmbito;
        this.total = total;
    }

    public String getNombreAmbito()       { return nombreAmbito; }
    public void setNombreAmbito(String v) { this.nombreAmbito = v; }

    public int getTotal()       { return total; }
    public void setTotal(int v) { this.total = v; }
}
