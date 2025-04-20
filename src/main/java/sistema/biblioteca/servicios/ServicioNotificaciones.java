package sistema.biblioteca.servicios;

import sistema.biblioteca.modelos.Usuario;

public interface ServicioNotificaciones {
    void enviarNotificacion(Usuario usuario, String mensaje);
    boolean notificacionesPendientes(Usuario usuario);
    void procesarNotificacionesPendientes();
} 