package sistema.biblioteca.servicios;

import sistema.biblioteca.modelos.Usuario;
import sistema.biblioteca.repositorios.RepositorioUsuarios;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase adaptadora que integra el ProcesadorNotificaciones con el sistema de notificaciones existente.
 * Implementa ServicioNotificaciones y delega las operaciones al ProcesadorNotificaciones.
 */
public class AdaptadorNotificaciones implements ServicioNotificaciones {
    
    private final ProcesadorNotificaciones procesador;
    private final String tipoNotificacion;
    private final RepositorioUsuarios repositorioUsuarios;
    private final ConcurrentHashMap<String, Boolean> pendientes;
    
    /**
     * Constructor para el adaptador
     * 
     * @param procesador El procesador de notificaciones concurrente
     * @param tipoNotificacion El tipo de notificación que este adaptador utiliza
     * @param repositorioUsuarios Repositorio para acceder a los usuarios del sistema
     */
    public AdaptadorNotificaciones(ProcesadorNotificaciones procesador, 
                                  String tipoNotificacion,
                                  RepositorioUsuarios repositorioUsuarios) {
        this.procesador = procesador;
        this.tipoNotificacion = tipoNotificacion;
        this.repositorioUsuarios = repositorioUsuarios;
        this.pendientes = new ConcurrentHashMap<>();
    }
    
    @Override
    public boolean enviarNotificacion(Usuario usuario, String mensaje) {
        if (usuario == null || mensaje == null || mensaje.isEmpty()) {
            return false;
        }
        
        // Marcar como pendiente
        pendientes.put(usuario.getId(), true);
        
        // Encolar en el procesador
        procesador.encolarNotificacion(usuario, mensaje, tipoNotificacion);
        
        return true;
    }
    
    @Override
    public int enviarNotificacionGlobal(String mensaje) {
        if (mensaje == null || mensaje.isEmpty() || repositorioUsuarios == null) {
            return 0;
        }
        
        var usuarios = repositorioUsuarios.obtenerTodos();
        if (usuarios.isEmpty()) {
            return 0;
        }
        
        AtomicInteger contador = new AtomicInteger(0);
        
        for (Usuario usuario : usuarios) {
            boolean enviado = enviarNotificacion(usuario, mensaje);
            if (enviado) {
                contador.incrementAndGet();
            }
        }
        
        return contador.get();
    }
    
    @Override
    public boolean notificacionesPendientes(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        
        return pendientes.getOrDefault(usuario.getId(), false);
    }
    
    @Override
    public void procesarNotificacionesPendientes() {
        // No es necesario implementar esta lógica ya que el ProcesadorNotificaciones
        // ya maneja todo el procesamiento de forma automática
        // Este método se incluye sólo para cumplir con la interfaz
    }
    
    /**
     * Marca las notificaciones como procesadas para un usuario
     * 
     * @param usuario El usuario cuyas notificaciones se marcarán como procesadas
     */
    public void marcarNotificacionesProcesadas(Usuario usuario) {
        if (usuario != null) {
            pendientes.remove(usuario.getId());
        }
    }
    
    /**
     * Obtiene el procesador de notificaciones asociado
     * 
     * @return El procesador de notificaciones
     */
    public ProcesadorNotificaciones getProcesador() {
        return procesador;
    }
} 