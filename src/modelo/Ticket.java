package modelo;

/**
 * Clase modelo del TICKET: es la entidad central del sistema. Representa un
 * incidente reportado. Equivale a una fila de la tabla Ticket.
 *
 * NOTA IMPORTANTE DE DISENO:
 * El ticket guarda id_ambito pero NO id_grupo. El grupo resolutor se deduce
 * del ambito mediante un JOIN (Ticket -> Ambito -> Grupo_Resolutor). Esto
 * evita redundancia y respeta la normalizacion (3FN).
 *
 * Los campos que pueden ser nulos en la base (tecnico sin asignar, fecha de
 * cierre) se declaran con tipos que aceptan null: Integer en vez de int.
 */
public class Ticket {

    // --- Claves foraneas (guardan el id de la fila relacionada) ---
    private int     idTicket;            // id_ticket (PK)
    private int     idUsuario;           // id_usuario (quien reporta)
    private int     idDispositivo;       // id_dispositivo (equipo afectado)
    private int     idAmbito;            // id_ambito (tipo de problema)
    private int     idEstado;            // id_estado (estado actual)
    private int     idAgenteCreador;     // id_agente_creador (quien registro)
    private Integer idTecnicoAsignado;   // id_tecnico_asignado (puede ser null)

    // --- Datos propios del ticket ---
    private String  fechaHoraCreacion;   // fecha_hora_creacion
    private String  fechaHoraCierre;     // fecha_hora_cierre (puede ser null)
    private String  prioridad;           // ENUM: 'Alta','Media','Baja'
    private String  naturaleza;          // ENUM: 'Fisica','Logica'
    private String  lugarFisicoExacto;   // lugar_fisico_exacto
    private String  descripcionProblema; // descripcion_problema
    private String  comentarioResolucion;// comentario_resolucion (null hasta resolver)

    // --- Campos extra rellenados por JOIN (para mostrar en listados) ---
    private String  nombreEstado;        // de Estado_Ticket
    private String  nombreAmbito;        // de Ambito
    private String  nombreUsuario;       // de Usuario_Cliente
    private String  nombreTecnico;       // de Empleado_Interno
    private String  nombreAgenteCreador; // de Empleado_Interno (quien registro la llamada)

    // --- Campos extra de detalle (solo se llenan en TicketDAO.buscarPorId) ---
    private String  nombreSucursal;      // de Sucursal, via Usuario_Cliente
    private String  razonSocial;         // de Empresa_Cliente, via Sucursal
    private String  codigoUsuario;       // de Usuario_Cliente (codigo_usuario)
    private String  numeroSerieDispositivo; // de Dispositivo
    private String  tipoHardwareDispositivo; // de Dispositivo
    private String  modeloDispositivo;      // de Dispositivo

    public Ticket() {
    }

    public int getIdTicket()                 { return idTicket; }
    public void setIdTicket(int v)           { this.idTicket = v; }

    public int getIdUsuario()                { return idUsuario; }
    public void setIdUsuario(int v)          { this.idUsuario = v; }

    public int getIdDispositivo()            { return idDispositivo; }
    public void setIdDispositivo(int v)      { this.idDispositivo = v; }

    public int getIdAmbito()                 { return idAmbito; }
    public void setIdAmbito(int v)           { this.idAmbito = v; }

    public int getIdEstado()                 { return idEstado; }
    public void setIdEstado(int v)           { this.idEstado = v; }

    public int getIdAgenteCreador()          { return idAgenteCreador; }
    public void setIdAgenteCreador(int v)    { this.idAgenteCreador = v; }

    public Integer getIdTecnicoAsignado()        { return idTecnicoAsignado; }
    public void setIdTecnicoAsignado(Integer v)  { this.idTecnicoAsignado = v; }

    public String getFechaHoraCreacion()     { return fechaHoraCreacion; }
    public void setFechaHoraCreacion(String v){ this.fechaHoraCreacion = v; }

    public String getFechaHoraCierre()       { return fechaHoraCierre; }
    public void setFechaHoraCierre(String v) { this.fechaHoraCierre = v; }

    public String getPrioridad()             { return prioridad; }
    public void setPrioridad(String v)       { this.prioridad = v; }

    public String getNaturaleza()            { return naturaleza; }
    public void setNaturaleza(String v)      { this.naturaleza = v; }

    public String getLugarFisicoExacto()     { return lugarFisicoExacto; }
    public void setLugarFisicoExacto(String v){ this.lugarFisicoExacto = v; }

    public String getDescripcionProblema()   { return descripcionProblema; }
    public void setDescripcionProblema(String v){ this.descripcionProblema = v; }

    public String getComentarioResolucion()  { return comentarioResolucion; }
    public void setComentarioResolucion(String v){ this.comentarioResolucion = v; }

    public String getNombreEstado()          { return nombreEstado; }
    public void setNombreEstado(String v)    { this.nombreEstado = v; }

    public String getNombreAmbito()          { return nombreAmbito; }
    public void setNombreAmbito(String v)    { this.nombreAmbito = v; }

    public String getNombreUsuario()         { return nombreUsuario; }
    public void setNombreUsuario(String v)   { this.nombreUsuario = v; }

    public String getNombreTecnico()         { return nombreTecnico; }
    public void setNombreTecnico(String v)   { this.nombreTecnico = v; }

    public String getNombreAgenteCreador()       { return nombreAgenteCreador; }
    public void setNombreAgenteCreador(String v) { this.nombreAgenteCreador = v; }

    public String getNombreSucursal()        { return nombreSucursal; }
    public void setNombreSucursal(String v)  { this.nombreSucursal = v; }

    public String getRazonSocial()           { return razonSocial; }
    public void setRazonSocial(String v)     { this.razonSocial = v; }

    public String getCodigoUsuario()         { return codigoUsuario; }
    public void setCodigoUsuario(String v)   { this.codigoUsuario = v; }

    public String getNumeroSerieDispositivo()      { return numeroSerieDispositivo; }
    public void setNumeroSerieDispositivo(String v){ this.numeroSerieDispositivo = v; }

    public String getTipoHardwareDispositivo()      { return tipoHardwareDispositivo; }
    public void setTipoHardwareDispositivo(String v){ this.tipoHardwareDispositivo = v; }

    public String getModeloDispositivo()       { return modeloDispositivo; }
    public void setModeloDispositivo(String v) { this.modeloDispositivo = v; }

    @Override
    public String toString() {
        return "Ticket #" + idTicket + " - " + nombreEstado;
    }
}
