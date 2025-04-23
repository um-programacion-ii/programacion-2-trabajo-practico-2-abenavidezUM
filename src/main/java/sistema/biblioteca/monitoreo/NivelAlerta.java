package sistema.biblioteca.monitoreo;

/**
 * Enumeración que define los diferentes niveles de alerta en el sistema
 */
public enum NivelAlerta {
    
    /**
     * Alerta baja, informativa o recordatorio general.
     * Prioridad de notificación: 5-7
     */
    BAJA(5, "Informativa", "Información general o recordatorio"),
    
    /**
     * Alerta media, recordatorio importante o advertencia.
     * Prioridad de notificación: 3-4
     */
    MEDIA(3, "Advertencia", "Recordatorio importante o advertencia"),
    
    /**
     * Alerta alta, requiere atención inmediata.
     * Prioridad de notificación: 1-2
     */
    ALTA(1, "Urgente", "Requiere atención inmediata"),
    
    /**
     * Alerta crítica, problema grave del sistema o seguridad.
     * Prioridad de notificación: 1 y envío por múltiples canales
     */
    CRITICA(1, "Crítica", "Problema grave del sistema o seguridad");
    
    private final int prioridadNotificacion;
    private final String etiqueta;
    private final String descripcion;
    
    private NivelAlerta(int prioridadNotificacion, String etiqueta, String descripcion) {
        this.prioridadNotificacion = prioridadNotificacion;
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }
    
    /**
     * Obtiene la prioridad de notificación asociada a este nivel de alerta
     * Las prioridades más bajas numéricamente son más importantes (1 es máxima)
     * 
     * @return Valor numérico de prioridad
     */
    public int getPrioridadNotificacion() {
        return prioridadNotificacion;
    }
    
    /**
     * Obtiene la etiqueta descriptiva del nivel de alerta
     * 
     * @return Etiqueta del nivel de alerta
     */
    public String getEtiqueta() {
        return etiqueta;
    }
    
    /**
     * Obtiene la descripción detallada del nivel de alerta
     * 
     * @return Descripción del nivel de alerta
     */
    public String getDescripcion() {
        return descripcion;
    }
    
    /**
     * Formatea un mensaje agregando la etiqueta del nivel de alerta
     * 
     * @param mensaje El mensaje original
     * @return Mensaje formateado con la etiqueta
     */
    public String formatearMensaje(String mensaje) {
        return "[" + etiqueta.toUpperCase() + "] " + mensaje;
    }
} 