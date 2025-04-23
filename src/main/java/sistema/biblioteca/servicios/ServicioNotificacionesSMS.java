package sistema.biblioteca.servicios;

import sistema.biblioteca.modelos.Usuario;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ServicioNotificacionesSMS implements ServicioNotificaciones {
    private Map<String, Queue<String>> notificacionesPendientes;
    
    public ServicioNotificacionesSMS() {
        this.notificacionesPendientes = new HashMap<>();
    }
    
    @Override
    public void enviarNotificacion(Usuario usuario, String mensaje) {
        if (usuario == null || usuario.getTelefono() == null || usuario.getTelefono().isEmpty()) {
            return;
        }
        
        // Truncar mensaje si es muy largo (los SMS tienen límite)
        String mensajeFinal = mensaje;
        if (mensaje.length() > 160) {
            mensajeFinal = mensaje.substring(0, 157) + "...";
        }
        
        // Agregar a la cola de notificaciones pendientes
        notificacionesPendientes
            .computeIfAbsent(usuario.getId(), k -> new LinkedList<>())
            .add(mensajeFinal);
        
        // Simulamos envío real
        System.out.println("SMS enviado a " + usuario.getTelefono() + ": " + mensajeFinal);
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
            
            // En un entorno real, limitar número de SMS por usuario por hora
            int count = 0;
            while (!mensajes.isEmpty() && count < 5) {  
                String mensaje = mensajes.poll();
                System.out.println("Procesando SMS pendiente para usuario " + usuarioId + ": " + mensaje);
                count++;
            }
            
            if (!mensajes.isEmpty()) {
                System.out.println("Quedan " + mensajes.size() + " SMS pendientes para procesar más tarde");
            }
        }
    }
} 