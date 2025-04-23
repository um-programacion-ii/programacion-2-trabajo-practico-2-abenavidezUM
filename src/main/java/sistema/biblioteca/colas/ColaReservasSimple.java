package sistema.biblioteca.colas;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Implementación básica de la interfaz ColaReservas
 */
public class ColaReservasSimple implements ColaReservas {
    private Map<String, Queue<String>> reservasPorRecurso;
    
    public ColaReservasSimple() {
        this.reservasPorRecurso = new HashMap<>();
    }
    
    @Override
    public boolean tieneReservasPendientes(String idRecurso) {
        Queue<String> reservas = reservasPorRecurso.get(idRecurso);
        return reservas != null && !reservas.isEmpty();
    }
    
    /**
     * Agrega una reserva a la cola para un recurso específico
     * 
     * @param idRecurso Identificador del recurso
     * @param idUsuario Identificador del usuario que realiza la reserva
     */
    public void agregarReserva(String idRecurso, String idUsuario) {
        if (idRecurso == null || idUsuario == null) {
            return;
        }
        
        // Obtener o crear la cola para este recurso
        Queue<String> colaReservas = reservasPorRecurso.computeIfAbsent(
            idRecurso, 
            k -> new LinkedList<>()
        );
        
        // Agregar el usuario a la cola
        colaReservas.add(idUsuario);
    }
    
    /**
     * Elimina y retorna el siguiente usuario en la cola de reservas
     * 
     * @param idRecurso Identificador del recurso
     * @return Identificador del siguiente usuario o null si no hay reservas
     */
    public String obtenerSiguienteReserva(String idRecurso) {
        Queue<String> colaReservas = reservasPorRecurso.get(idRecurso);
        
        if (colaReservas == null || colaReservas.isEmpty()) {
            return null;
        }
        
        return colaReservas.poll();
    }
    
    /**
     * Consulta el siguiente usuario en la cola sin eliminarlo
     * 
     * @param idRecurso Identificador del recurso
     * @return Identificador del siguiente usuario o null si no hay reservas
     */
    public String consultarSiguienteReserva(String idRecurso) {
        Queue<String> colaReservas = reservasPorRecurso.get(idRecurso);
        
        if (colaReservas == null || colaReservas.isEmpty()) {
            return null;
        }
        
        return colaReservas.peek();
    }
    
    /**
     * Cancela una reserva específica
     * 
     * @param idRecurso Identificador del recurso
     * @param idUsuario Identificador del usuario
     * @return true si la reserva fue cancelada, false en caso contrario
     */
    public boolean cancelarReserva(String idRecurso, String idUsuario) {
        Queue<String> colaReservas = reservasPorRecurso.get(idRecurso);
        
        if (colaReservas == null) {
            return false;
        }
        
        return colaReservas.remove(idUsuario);
    }
    
    /**
     * Obtiene la cantidad de reservas pendientes para un recurso
     * 
     * @param idRecurso Identificador del recurso
     * @return Cantidad de reservas pendientes
     */
    public int getCantidadReservas(String idRecurso) {
        Queue<String> colaReservas = reservasPorRecurso.get(idRecurso);
        
        if (colaReservas == null) {
            return 0;
        }
        
        return colaReservas.size();
    }
} 