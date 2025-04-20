package sistema.biblioteca.modelos;

import java.time.LocalDateTime;

public class Reserva {
    private String id;
    private RecursoBase recurso;
    private Usuario usuario;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaExpiracion;
    private EstadoReserva estado;
    
    public enum EstadoReserva {
        PENDIENTE,
        COMPLETADA,
        EXPIRADA,
        CANCELADA
    }
    
    public Reserva(String id, RecursoBase recurso, Usuario usuario) {
        this.id = id;
        this.recurso = recurso;
        this.usuario = usuario;
        this.fechaReserva = LocalDateTime.now();
        // Por defecto, la reserva expira en 3 días
        this.fechaExpiracion = LocalDateTime.now().plusDays(3);
        this.estado = EstadoReserva.PENDIENTE;
    }
    
    public Reserva(String id, RecursoBase recurso, Usuario usuario, int diasExpiracion) {
        this(id, recurso, usuario);
        if (diasExpiracion > 0) {
            this.fechaExpiracion = LocalDateTime.now().plusDays(diasExpiracion);
        }
    }
    
    public String getId() {
        return id;
    }
    
    public RecursoBase getRecurso() {
        return recurso;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }
    
    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }
    
    public EstadoReserva getEstado() {
        return estado;
    }
    
    public void completar() {
        this.estado = EstadoReserva.COMPLETADA;
    }
    
    public void cancelar() {
        this.estado = EstadoReserva.CANCELADA;
    }
    
    public void expirar() {
        if (this.estado == EstadoReserva.PENDIENTE) {
            this.estado = EstadoReserva.EXPIRADA;
        }
    }
    
    public boolean estaPendiente() {
        return this.estado == EstadoReserva.PENDIENTE;
    }
    
    public boolean estaCompletada() {
        return this.estado == EstadoReserva.COMPLETADA;
    }
    
    public boolean estaExpirada() {
        return this.estado == EstadoReserva.EXPIRADA;
    }
    
    public boolean estaCancelada() {
        return this.estado == EstadoReserva.CANCELADA;
    }
    
    public boolean haExpirado() {
        // Si no está en estado pendiente, no puede expirar
        if (this.estado != EstadoReserva.PENDIENTE) {
            return false;
        }
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }
    
    public void extenderExpiracion(int dias) {
        if (dias > 0 && this.estado == EstadoReserva.PENDIENTE) {
            this.fechaExpiracion = this.fechaExpiracion.plusDays(dias);
        }
    }
    
    public long diasHastaExpiracion() {
        if (this.estado != EstadoReserva.PENDIENTE) {
            return 0;
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isAfter(fechaExpiracion)) {
            return 0;
        }
        
        return java.time.temporal.ChronoUnit.DAYS.between(ahora.toLocalDate(), fechaExpiracion.toLocalDate());
    }
    
    @Override
    public String toString() {
        return "Reserva{" +
                "id='" + id + '\'' +
                ", recurso=" + recurso.getTitulo() +
                ", usuario=" + usuario.getNombre() +
                ", fechaReserva=" + fechaReserva +
                ", fechaExpiracion=" + fechaExpiracion +
                ", estado=" + estado +
                '}';
    }
} 