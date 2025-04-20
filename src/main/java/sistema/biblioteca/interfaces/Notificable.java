package sistema.biblioteca.interfaces;

import java.util.List;
import sistema.biblioteca.modelos.Notificacion;

public interface Notificable {
    void enviarNotificacion(String mensaje);
    List<Notificacion> getNotificacionesPendientes();
} 