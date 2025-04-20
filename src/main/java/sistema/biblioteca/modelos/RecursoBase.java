package sistema.biblioteca.modelos;

import sistema.biblioteca.interfaces.Prestable;
import sistema.biblioteca.interfaces.RecursoDigital;
import java.time.LocalDateTime;

public abstract class RecursoBase implements RecursoDigital, Prestable {
    protected String identificador;
    protected String titulo;
    protected CategoriaRecurso categoria;
    protected EstadoRecurso estado;
    protected LocalDateTime fechaCreacion;
    protected Usuario usuarioPrestamo;
    protected LocalDateTime fechaDevolucion;

    public RecursoBase(String identificador, String titulo, CategoriaRecurso categoria) {
        this.identificador = identificador;
        this.titulo = titulo;
        this.categoria = categoria;
        this.estado = EstadoRecurso.DISPONIBLE;
        this.fechaCreacion = LocalDateTime.now();
    }

    @Override
    public String getIdentificador() {
        return identificador;
    }

    public String getTitulo() {
        return titulo;
    }

    public CategoriaRecurso getCategoria() {
        return categoria;
    }

    @Override
    public EstadoRecurso getEstado() {
        return estado;
    }

    @Override
    public void actualizarEstado(EstadoRecurso estado) {
        this.estado = estado;
    }

    @Override
    public boolean estaDisponible() {
        return this.estado == EstadoRecurso.DISPONIBLE;
    }

    @Override
    public LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }

    @Override
    public void prestar(Usuario usuario) {
        if (!estaDisponible()) {
            return;
        }
        
        this.usuarioPrestamo = usuario;
        this.estado = EstadoRecurso.PRESTADO;
        this.fechaDevolucion = calcularFechaDevolucion();
        usuario.incrementarPrestamos();
        usuario.enviarNotificacion("Has tomado prestado: " + this.titulo);
    }
    
    public void devolver() {
        if (this.estado != EstadoRecurso.PRESTADO) {
            return;
        }
        
        this.estado = EstadoRecurso.DISPONIBLE;
        if (this.usuarioPrestamo != null) {
            this.usuarioPrestamo.decrementarPrestamos();
            this.usuarioPrestamo.enviarNotificacion("Has devuelto: " + this.titulo);
            this.usuarioPrestamo = null;
        }
        this.fechaDevolucion = null;
    }
    
    protected abstract LocalDateTime calcularFechaDevolucion();
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public Usuario getUsuarioPrestamo() {
        return usuarioPrestamo;
    }
    
    @Override
    public String toString() {
        return "Recurso{" +
                "id='" + identificador + '\'' +
                ", titulo='" + titulo + '\'' +
                ", categoria=" + categoria +
                ", estado=" + estado +
                '}';
    }
} 