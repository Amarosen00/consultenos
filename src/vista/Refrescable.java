package vista;

/**
 * Pantallas que pueden recargar sus datos sin volver a construirse desde
 * cero. Se usa cuando se reutiliza una ventana ya abierta (en vez de crear
 * otra encima): si ademas implementa esta interfaz, se le pide refrescar()
 * para que no se quede mostrando datos viejos de la primera vez que se abrio.
 */
public interface Refrescable {
    void refrescar();
}
