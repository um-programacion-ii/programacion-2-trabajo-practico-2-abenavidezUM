package sistema.biblioteca.servicios;

import sistema.biblioteca.modelos.Usuario;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ServicioNotificacionesEmail implements ServicioNotificaciones {
    private Map<String, Queue<String>> notificacionesPendientes;
    
    public ServicioNotificacionesEmail() {
        this.notificacionesPendientes = new HashMap<>();
    }
    
    @Override
    public void enviarNotificacion(Usuario usuario, String mensaje) {
        if (usuario == null || usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
            return;
        }
        
        // Agregar a la cola de notificaciones pendientes
        notificacionesPendientes
            .computeIfAbsent(usuario.getId(), k -> new LinkedList<>())
            .add(mensaje);
        
        // Simulamos envío real con un println
        System.out.println("EMAIL enviado a " + usuario.getEmail() + ": " + mensaje);
    }
    
    @Override
    public boolean notificacionesPendientes(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        
        Queue<String> pendientes = notificacionesPendientes.get(usuario.getId());
        return pendientes != null && !pendientes.isEmpty();
    }
    
    @Override
    public void procesarNotificacionesPendientes() {
        for (Map.Entry<String, Queue<String>> entry : notificacionesPendientes.entrySet()) {
            String usuarioId = entry.getKey();
            Queue<String> mensajes = entry.getValue();
            
            // Procesar mensajes pendientes
            while (!mensajes.isEmpty()) {
                String mensaje = mensajes.poll();
                System.out.println("Procesando email pendiente para usuario " + usuarioId + ": " + mensaje);
                // Aquí iría lógica real de envío
            }
        }
    }
} 