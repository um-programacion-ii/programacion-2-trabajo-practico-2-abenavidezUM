package sistema.biblioteca.monitoreo;

import sistema.biblioteca.modelos.Prestamo;
import sistema.biblioteca.modelos.Usuario;
import sistema.biblioteca.servicios.ProcesadorNotificaciones;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Clase para gestionar alertas específicas de vencimiento de préstamos
 */
public class AlertaVencimiento {
    
    private final ProcesadorNotificaciones procesadorNotificaciones;
    
    // Configuración de días para las alertas
    private int diasAlertaPreventiva1 = 3; // Primera alerta preventiva (3 días antes)
    private int diasAlertaPreventiva2 = 1; // Segunda alerta preventiva (1 día antes)
    private int diasRecordatorioVencido = 1; // Recordatorio después de vencido (cada día)
    
    public AlertaVencimiento(ProcesadorNotificaciones procesadorNotificaciones) {
        this.procesadorNotificaciones = procesadorNotificaciones;
    }
    
    /**
     * Verifica un préstamo y envía las alertas correspondientes según su estado
     * 
     * @param prestamo El préstamo a verificar
     * @return true si se envió alguna alerta, false en caso contrario
     */
    public boolean verificarYAlertarSiNecesario(Prestamo prestamo) {
        if (prestamo == null || prestamo.getFechaDevolucionReal() != null) {
            return false; // Ya devuelto, no requiere alertas
        }
        
        LocalDateTime fechaVencimiento = prestamo.getFechaDevolucionEstimada();
        LocalDateTime ahora = LocalDateTime.now();
        
        // Calcular días hasta vencimiento (o desde vencimiento si es negativo)
        long diasHastaVencimiento = ChronoUnit.DAYS.between(ahora.toLocalDate(), fechaVencimiento.toLocalDate());
        
        if (diasHastaVencimiento < 0) {
            // Préstamo vencido
            return alertarPrestamoVencido(prestamo, Math.abs(diasHastaVencimiento));
        } else if (diasHastaVencimiento == diasAlertaPreventiva2) {
            // Alerta preventiva 2 (día anterior)
            return alertarProximoVencimiento(prestamo, (int)diasHastaVencimiento, NivelAlerta.MEDIA);
        } else if (diasHastaVencimiento == diasAlertaPreventiva1) {
            // Alerta preventiva 1 (3 días antes)
            return alertarProximoVencimiento(prestamo, (int)diasHastaVencimiento, NivelAlerta.BAJA);
        }
        
        return false; // No se enviaron alertas
    }
    
    /**
     * Envía una alerta preventiva para un préstamo próximo a vencer
     * 
     * @param prestamo El préstamo próximo a vencer
     * @param diasRestantes Días restantes hasta el vencimiento
     * @param nivel Nivel de alerta a utilizar
     * @return true si se envió la alerta correctamente
     */
    private boolean alertarProximoVencimiento(Prestamo prestamo, int diasRestantes, NivelAlerta nivel) {
        Usuario usuario = prestamo.getUsuario();
        
        String pluralDias = diasRestantes == 1 ? "día" : "días";
        String mensaje = "Tu préstamo del recurso '" + prestamo.getRecurso().getTitulo() + 
                "' vence en " + diasRestantes + " " + pluralDias + ". " +
                "Puedes devolverlo o solicitar una renovación antes de la fecha límite.";
        
        mensaje = nivel.formatearMensaje(mensaje);
        
        // Enviar alerta por email
        procesadorNotificaciones.encolarNotificacion(
                usuario, 
                mensaje,
                "email", 
                nivel.getPrioridadNotificacion());
        
        return true;
    }
    
    /**
     * Envía una alerta para un préstamo que ya está vencido
     * 
     * @param prestamo El préstamo vencido
     * @param diasVencido Días transcurridos desde el vencimiento
     * @return true si se envió la alerta correctamente
     */
    private boolean alertarPrestamoVencido(Prestamo prestamo, long diasVencido) {
        // Solo enviar recordatorio cada X días configurados
        if (diasVencido % diasRecordatorioVencido != 0) {
            return false; // No toca enviar recordatorio hoy
        }
        
        Usuario usuario = prestamo.getUsuario();
        NivelAlerta nivel = determinarNivelAlertaVencido(diasVencido);
        
        String pluralDias = diasVencido == 1 ? "día" : "días";
        String mensaje = "Tu préstamo del recurso '" + prestamo.getRecurso().getTitulo() + 
                "' está vencido por " + diasVencido + " " + pluralDias + ". " +
                "Por favor, devuelve el material lo antes posible para evitar sanciones adicionales.";
        
        mensaje = nivel.formatearMensaje(mensaje);
        
        // Enviar por email
        procesadorNotificaciones.encolarNotificacion(
                usuario, 
                mensaje,
                "email", 
                nivel.getPrioridadNotificacion());
        
        // Si el vencimiento es grave, enviar también por SMS si hay teléfono
        if (nivel == NivelAlerta.ALTA || nivel == NivelAlerta.CRITICA) {
            if (usuario.getTelefono() != null && !usuario.getTelefono().isEmpty()) {
                String mensajeCorto = "Biblioteca: Préstamo vencido por " + diasVencido + 
                        " días. Devuelve '" + prestamo.getRecurso().getTitulo() + "' urgentemente.";
                
                procesadorNotificaciones.encolarNotificacion(
                        usuario, 
                        mensajeCorto,
                        "sms", 
                        nivel.getPrioridadNotificacion());
            }
        }
        
        return true;
    }
    
    /**
     * Determina el nivel de alerta según los días de vencimiento
     * 
     * @param diasVencido Días transcurridos desde el vencimiento
     * @return Nivel de alerta apropiado
     */
    private NivelAlerta determinarNivelAlertaVencido(long diasVencido) {
        if (diasVencido >= 30) {
            return NivelAlerta.CRITICA; // Más de un mes de vencimiento
        } else if (diasVencido >= 10) {
            return NivelAlerta.ALTA; // Entre 10 y 29 días
        } else if (diasVencido >= 3) {
            return NivelAlerta.MEDIA; // Entre 3 y 9 días
        } else {
            return NivelAlerta.BAJA; // Menos de 3 días
        }
    }
    
    // Getters y setters para la configuración de días
    
    public int getDiasAlertaPreventiva1() {
        return diasAlertaPreventiva1;
    }
    
    public void setDiasAlertaPreventiva1(int diasAlertaPreventiva1) {
        if (diasAlertaPreventiva1 < 0) {
            throw new IllegalArgumentException("Los días de alerta no pueden ser negativos");
        }
        this.diasAlertaPreventiva1 = diasAlertaPreventiva1;
    }
    
    public int getDiasAlertaPreventiva2() {
        return diasAlertaPreventiva2;
    }
    
    public void setDiasAlertaPreventiva2(int diasAlertaPreventiva2) {
        if (diasAlertaPreventiva2 < 0) {
            throw new IllegalArgumentException("Los días de alerta no pueden ser negativos");
        }
        this.diasAlertaPreventiva2 = diasAlertaPreventiva2;
    }
    
    public int getDiasRecordatorioVencido() {
        return diasRecordatorioVencido;
    }
    
    public void setDiasRecordatorioVencido(int diasRecordatorioVencido) {
        if (diasRecordatorioVencido <= 0) {
            throw new IllegalArgumentException("Los días de recordatorio deben ser positivos");
        }
        this.diasRecordatorioVencido = diasRecordatorioVencido;
    }
} 