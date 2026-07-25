package modelo;

/**
 * Clase modelo del catalogo Ambito (Impresion, Redes, Radiofrecuencia,
 * Computadores, Telefonos). Se usa para llenar combos (registrar ticket,
 * filtrar listados), mismo patron que EstadoTicket.
 *
 * Tabla asociada: Ambito
 */
public class Ambito {

    private int    idAmbito;      // id_ambito (PK)
    private String nombreAmbito;  // nombre_ambito

    public Ambito() {
    }

    public Ambito(int idAmbito, String nombreAmbito) {
        this.idAmbito     = idAmbito;
        this.nombreAmbito = nombreAmbito;
    }

    public int getIdAmbito()              { return idAmbito; }
    public void setIdAmbito(int v)        { this.idAmbito = v; }

    public String getNombreAmbito()       { return nombreAmbito; }
    public void setNombreAmbito(String v) { this.nombreAmbito = v; }

    @Override
    public String toString() {
        return nombreAmbito;
    }
}
