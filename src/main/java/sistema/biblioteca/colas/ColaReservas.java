package sistema.biblioteca.colas;

/**
 * Interfaz para manejar las colas de reservas de recursos
 */
public interface ColaReservas {
    
    /**
     * Verifica si hay reservas pendientes para un recurso específico
     * 
     * @param idRecurso Identificador del recurso a verificar
     * @return true si hay reservas pendientes, false en caso contrario
     */
    boolean tieneReservasPendientes(String idRecurso);
} 