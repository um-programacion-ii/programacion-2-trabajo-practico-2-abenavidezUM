package sistema.biblioteca.colas;

import sistema.biblioteca.modelos.Reserva;
import sistema.biblioteca.modelos.Usuario;

/**
 * Interfaz que define una solicitud de reserva que será procesada por el ProcesadorReservas
 */
public interface SolicitudReserva {
    
    /**
     * Enumeración que define los tipos de solicitudes
     */
    enum TipoSolicitud {
        CREAR,
        CANCELAR,
        COMPLETAR,
        EXTENDER,
        VERIFICAR_EXPIRADAS
    }
    
    /**
     * Obtiene el tipo de solicitud
     * @return El tipo de solicitud
     */
    TipoSolicitud getTipo();
    
    /**
     * Obtiene el identificador del recurso, si aplica
     * @return El identificador del recurso o null
     */
    String getIdRecurso();
    
    /**
     * Obtiene el usuario que realiza la solicitud, si aplica
     * @return El usuario o null
     */
    Usuario getUsuario();
    
    /**
     * Obtiene el identificador de la reserva, si aplica
     * @return El identificador de la reserva o null
     */
    String getIdReserva();
    
    /**
     * Obtiene la cantidad de días para extender, si aplica
     * @return La cantidad de días o 0
     */
    int getDiasExtension();
} 