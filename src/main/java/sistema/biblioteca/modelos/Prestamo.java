package sistema.biblioteca.modelos;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Prestamo {
    private String id;
    private RecursoBase recurso;
    private Usuario usuario;
    private LocalDateTime fechaPrestamo;
    private LocalDateTime fechaDevolucionEstimada;
    private LocalDateTime fechaDevolucionReal;
    private boolean activo;
    
    public Prestamo(String id, RecursoBase recurso, Usuario usuario) {
        this.id = id;
        this.recurso = recurso;
        this.usuario = usuario;
        this.fechaPrestamo = LocalDateTime.now();
        this.fechaDevolucionEstimada = calcularFechaDevolucionEstimada(recurso);
        this.activo = true;
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
    
    public LocalDateTime getFechaPrestamo() {
        return fechaPrestamo;
    }
    
    public LocalDateTime getFechaDevolucionEstimada() {
        return fechaDevolucionEstimada;
    }
    
    public LocalDateTime getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void registrarDevolucion() {
        this.fechaDevolucionReal = LocalDateTime.now();
        this.activo = false;
    }
    
    public boolean estaVencido() {
        if (!activo) {
            return false;
        }
        return LocalDateTime.now().isAfter(fechaDevolucionEstimada);
    }
    
    public long getDiasRestantes() {
        if (!activo) {
            return 0;
        }
        
        LocalDateTime hoy = LocalDateTime.now();
        if (hoy.isAfter(fechaDevolucionEstimada)) {
            return -ChronoUnit.DAYS.between(fechaDevolucionEstimada.toLocalDate(), hoy.toLocalDate());
        } else {
            return ChronoUnit.DAYS.between(hoy.toLocalDate(), fechaDevolucionEstimada.toLocalDate());
        }
    }
    
    private LocalDateTime calcularFechaDevolucionEstimada(RecursoBase recurso) {
        // Aquí podríamos tener lógica diferente según el tipo de recurso
        // Pero para simplificar, usamos la lógica que ya existe en RecursoBase
        return recurso.calcularFechaDevolucion();
    }
    
    @Override
    public String toString() {
        return "Prestamo{" +
                "id='" + id + '\'' +
                ", recurso=" + recurso.getTitulo() +
                ", usuario=" + usuario.getNombre() +
                ", fechaPrestamo=" + fechaPrestamo +
                ", fechaDevolucionEstimada=" + fechaDevolucionEstimada +
                ", fechaDevolucionReal=" + fechaDevolucionReal +
                ", activo=" + activo +
                '}';
    }
} 