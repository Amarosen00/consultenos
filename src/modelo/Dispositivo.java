package modelo;

/**
 * Clase modelo del DISPOSITIVO: el hardware corporativo que presenta la falla.
 * Equivale a una fila de la tabla Dispositivo.
 */
public class Dispositivo {

    private int    idDispositivo;   // id_dispositivo (PK)
    private int    idSucursal;      // id_sucursal (FK)
    private String numeroSerie;     // numero_serie (unico)
    private String tipoHardware;    // tipo_hardware
    private String modelo;          // modelo

    public Dispositivo() {
    }

    public int getIdDispositivo()          { return idDispositivo; }
    public void setIdDispositivo(int v)    { this.idDispositivo = v; }

    public int getIdSucursal()             { return idSucursal; }
    public void setIdSucursal(int v)       { this.idSucursal = v; }

    public String getNumeroSerie()         { return numeroSerie; }
    public void setNumeroSerie(String v)   { this.numeroSerie = v; }

    public String getTipoHardware()        { return tipoHardware; }
    public void setTipoHardware(String v)  { this.tipoHardware = v; }

    public String getModelo()              { return modelo; }
    public void setModelo(String v)        { this.modelo = v; }

    @Override
    public String toString() {
        // Lo que se vera en un combo de seleccion:
        return tipoHardware + " - " + numeroSerie;
    }
}
