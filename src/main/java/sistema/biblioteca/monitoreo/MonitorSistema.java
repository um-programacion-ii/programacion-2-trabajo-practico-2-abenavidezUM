package sistema.biblioteca.monitoreo;

import sistema.biblioteca.gestores.GestorPrestamos;
import sistema.biblioteca.gestores.GestorReservas;
import sistema.biblioteca.modelos.Prestamo;
import sistema.biblioteca.modelos.Reserva;
import sistema.biblioteca.modelos.Usuario;
import sistema.biblioteca.servicios.ProcesadorNotificaciones;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Clase que implementa un monitor del sistema para detectar préstamos vencidos,
 * reservas expiradas y próximos vencimientos, enviando alertas cuando sea necesario.
 */
public class MonitorSistema implements Runnable {
    
    private final GestorPrestamos gestorPrestamos;
    private final GestorReservas gestorReservas;
    private final ProcesadorNotificaciones procesadorNotificaciones;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean ejecutando;
    
    private final int INTERVALO_VERIFICACION_MINUTOS = 60; // Verificar cada hora por defecto
    private final int DIAS_ALERTA_PREVIA = 1; // Alertar 1 día antes por defecto
    
    /**
     * Constructor del MonitorSistema
     * 
     * @param gestorPrestamos Gestor de préstamos
     * @param gestorReservas Gestor de reservas
     * @param procesadorNotificaciones Procesador de notificaciones
     */
    public MonitorSistema(
            GestorPrestamos gestorPrestamos, 
            GestorReservas gestorReservas, 
            ProcesadorNotificaciones procesadorNotificaciones) {
        this.gestorPrestamos = gestorPrestamos;
        this.gestorReservas = gestorReservas;
        this.procesadorNotificaciones = procesadorNotificaciones;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.ejecutando = new AtomicBoolean(false);
    }
    
    /**
     * Inicia el monitor del sistema con la configuración predeterminada
     */
    public void iniciar() {
        iniciar(INTERVALO_VERIFICACION_MINUTOS);
    }
    
    /**
     * Inicia el monitor del sistema con un intervalo específico
     * 
     * @param intervaloMinutos Intervalo en minutos entre verificaciones
     */
    public void iniciar(int intervaloMinutos) {
        if (ejecutando.get()) {
            return; // Ya está en ejecución
        }
        
        ejecutando.set(true);
        
        // Ejecutar inmediatamente y luego programar ejecuciones periódicas
        ejecutarVerificaciones();
        
        scheduler.scheduleAtFixedRate(
            this::ejecutarVerificaciones,
            intervaloMinutos,
            intervaloMinutos,
            TimeUnit.MINUTES
        );
        
        System.out.println("Monitor del sistema iniciado. Verificando cada " + 
                intervaloMinutos + " minutos.");
    }
    
    /**
     * Detiene el monitor del sistema
     */
    public void detener() {
        ejecutando.set(false);
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Monitor del sistema detenido.");
    }
    
    /**
     * Método principal que se ejecuta en cada iteración
     */
    @Override
    public void run() {
        try {
            if (ejecutando.get()) {
                ejecutarVerificaciones();
            }
        } catch (Exception e) {
            System.out.println("Error en el monitor del sistema: " + e.getMessage());
        }
    }
    
    /**
     * Ejecuta todas las verificaciones del sistema
     */
    private void ejecutarVerificaciones() {
        try {
            verificarPrestamosVencidos();
            verificarProximosVencimientos();
            verificarReservasExpiradas();
        } catch (Exception e) {
            System.out.println("Error al ejecutar verificaciones: " + e.getMessage());
        }
    }
    
    /**
     * Verifica préstamos vencidos y envía notificaciones
     */
    private void verificarPrestamosVencidos() {
        List<Prestamo> prestamosVencidos = obtenerPrestamosVencidos();
        
        if (!prestamosVencidos.isEmpty()) {
            System.out.println("Se encontraron " + prestamosVencidos.size() + 
                    " préstamos vencidos.");
            
            for (Prestamo prestamo : prestamosVencidos) {
                Usuario usuario = prestamo.getUsuario();
                long diasVencido = ChronoUnit.DAYS.between(
                        prestamo.getFechaDevolucionEstimada().toLocalDate(), 
                        LocalDateTime.now().toLocalDate());
                
                String mensaje = "IMPORTANTE: Tu préstamo del recurso '" + 
                        prestamo.getRecurso().getTitulo() + 
                        "' está vencido por " + diasVencido + " días. " +
                        "Por favor, devuelve el material lo antes posible.";
                
                // Enviar notificación con alta prioridad
                procesadorNotificaciones.encolarNotificacion(
                        usuario, mensaje, "email", 1);
                
                // También enviar por SMS si es posible
                if (usuario.getTelefono() != null && !usuario.getTelefono().isEmpty()) {
                    procesadorNotificaciones.encolarNotificacion(
                            usuario, mensaje, "sms", 1);
                }
            }
        }
    }
    
    /**
     * Verifica préstamos próximos a vencer y envía alertas preventivas
     */
    private void verificarProximosVencimientos() {
        List<Prestamo> proximosVencimientos = obtenerProximosVencimientos();
        
        if (!proximosVencimientos.isEmpty()) {
            System.out.println("Se encontraron " + proximosVencimientos.size() + 
                    " préstamos próximos a vencer.");
            
            for (Prestamo prestamo : proximosVencimientos) {
                Usuario usuario = prestamo.getUsuario();
                
                String mensaje = "RECORDATORIO: Tu préstamo del recurso '" + 
                        prestamo.getRecurso().getTitulo() + 
                        "' vence mañana. Puedes devolverlo o renovarlo antes de la fecha límite.";
                
                // Enviar notificación con prioridad media
                procesadorNotificaciones.encolarNotificacion(
                        usuario, mensaje, "email", 3);
            }
        }
    }
    
    /**
     * Verifica reservas expiradas y las procesa
     */
    private void verificarReservasExpiradas() {
        List<Reserva> reservasExpiradas = obtenerReservasExpiradas();
        
        if (!reservasExpiradas.isEmpty()) {
            System.out.println("Se encontraron " + reservasExpiradas.size() + 
                    " reservas expiradas.");
            
            for (Reserva reserva : reservasExpiradas) {
                Usuario usuario = reserva.getUsuario();
                
                String mensaje = "Tu reserva para el recurso '" + 
                        reserva.getRecurso().getTitulo() + 
                        "' ha expirado debido a que no fue reclamada a tiempo.";
                
                // Notificar al usuario
                procesadorNotificaciones.encolarNotificacion(
                        usuario, mensaje, "email", 5);
                
                try {
                    // Marcar la reserva como expirada
                    if (reserva.estaPendiente()) {
                        reserva.expirar();
                    }
                } catch (Exception e) {
                    System.out.println("Error al marcar reserva como expirada: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Obtiene la lista de préstamos vencidos
     * 
     * @return Lista de préstamos vencidos
     */
    private List<Prestamo> obtenerPrestamosVencidos() {
        return gestorPrestamos.listarPrestamosVencidos();
    }
    
    /**
     * Obtiene la lista de préstamos próximos a vencer (según DIAS_ALERTA_PREVIA)
     * 
     * @return Lista de préstamos próximos a vencer
     */
    private List<Prestamo> obtenerProximosVencimientos() {
        List<Prestamo> proximosVencimientos = new ArrayList<>();
        List<Prestamo> prestamosActivos = gestorPrestamos.listarPrestamosActivos();
        
        LocalDateTime fechaReferencia = LocalDateTime.now().plusDays(DIAS_ALERTA_PREVIA);
        
        for (Prestamo prestamo : prestamosActivos) {
            LocalDateTime fechaDevolucion = prestamo.getFechaDevolucionEstimada();
            
            // Verificar si vence exactamente en DIAS_ALERTA_PREVIA
            if (fechaDevolucion.toLocalDate().isEqual(fechaReferencia.toLocalDate())) {
                proximosVencimientos.add(prestamo);
            }
        }
        
        return proximosVencimientos;
    }
    
    /**
     * Obtiene la lista de reservas expiradas
     * 
     * @return Lista de reservas expiradas
     */
    private List<Reserva> obtenerReservasExpiradas() {
        List<Reserva> reservasExpiradas = new ArrayList<>();
        
        // Obtener todas las reservas activas
        List<Reserva> reservasActivas = gestorReservas.listarReservasActivas();
        
        // Filtrar las que han expirado
        for (Reserva reserva : reservasActivas) {
            if (reserva.haExpirado()) {
                reservasExpiradas.add(reserva);
            }
        }
        
        return reservasExpiradas;
    }
    
    /**
     * Cambia el intervalo de verificación
     * 
     * @param intervaloMinutos Nuevo intervalo en minutos
     */
    public void cambiarIntervaloVerificacion(int intervaloMinutos) {
        if (intervaloMinutos <= 0) {
            throw new IllegalArgumentException("El intervalo debe ser mayor que cero");
        }
        
        detener();
        iniciar(intervaloMinutos);
    }
    
    /**
     * Cambia el número de días de alerta previa
     * 
     * @param dias Días de anticipación para alertar
     */
    public void cambiarDiasAlertaPrevia(int dias) {
        if (dias < 0) {
            throw new IllegalArgumentException("Los días de alerta previa no pueden ser negativos");
        }
    }
    
    /**
     * Ejecuta una verificación manual fuera del ciclo programado
     */
    public void ejecutarVerificacionManual() {
        System.out.println("Ejecutando verificación manual del sistema...");
        ejecutarVerificaciones();
    }
} 