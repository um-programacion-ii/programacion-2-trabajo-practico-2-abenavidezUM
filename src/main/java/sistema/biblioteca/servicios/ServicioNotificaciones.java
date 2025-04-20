package sistema.biblioteca.servicios;

import sistema.biblioteca.modelos.Usuario;

/**
 * Interfaz que define las operaciones básicas para enviar notificaciones a usuarios
 */
public interface ServicioNotificaciones {
    /**
     * Envía una notificación a un usuario
     * 
     * @param usuario El usuario destinatario de la notificación
     * @param mensaje El contenido de la notificación
     * @return true si la notificación fue enviada correctamente
     */
    boolean enviarNotificacion(Usuario usuario, String mensaje);
    
    /**
     * Envía una notificación a todos los usuarios del sistema
     * 
     * @param mensaje El contenido de la notificación
     * @return Número de notificaciones enviadas correctamente
     */
    int enviarNotificacionGlobal(String mensaje);
    
    /**
     * Verifica si hay notificaciones pendientes para un usuario
     * 
     * @param usuario El usuario para el cual verificar notificaciones pendientes
     * @return true si hay notificaciones pendientes, false en caso contrario
     */
    boolean notificacionesPendientes(Usuario usuario);
    
    /**
     * Procesa las notificaciones pendientes en el servicio
     */
    void procesarNotificacionesPendientes();
} 