package sistema.biblioteca.modelos;

import java.time.LocalDateTime;

/**
 * Clase que representa una entrada en el historial de renovaciones de un préstamo.
 * Almacena información sobre la fecha anterior, la nueva fecha, los días extendidos y el motivo.
 */
public class HistorialRenovacion {
    private LocalDateTime fechaRenovacion;
    private LocalDateTime fechaAnterior;
    private LocalDateTime nuevaFecha;
    private int diasExtendidos;
    private String motivo;
    
    public HistorialRenovacion(LocalDateTime fechaAnterior, LocalDateTime nuevaFecha, 
                              int diasExtendidos, String motivo) {
        this.fechaRenovacion = LocalDateTime.now();
        this.fechaAnterior = fechaAnterior;
        this.nuevaFecha = nuevaFecha;
        this.diasExtendidos = diasExtendidos;
        this.motivo = motivo;
    }
    
    public LocalDateTime getFechaRenovacion() {
        return fechaRenovacion;
    }
    
    public LocalDateTime getFechaAnterior() {
        return fechaAnterior;
    }
    
    public LocalDateTime getNuevaFecha() {
        return nuevaFecha;
    }
    
    public int getDiasExtendidos() {
        return diasExtendidos;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    @Override
    public String toString() {
        return "Renovación [" + 
               "Fecha: " + fechaRenovacion + 
               ", De: " + fechaAnterior + 
               ", A: " + nuevaFecha + 
               ", Días: " + diasExtendidos + 
               ", Motivo: '" + motivo + "'" +
               "]";
    }
} 