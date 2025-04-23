package sistema.biblioteca.modelos;

import java.time.LocalDateTime;

public class Audiolibro extends RecursoBase {
    private String autor;
    private String narrador;
    private int duracionMinutos;
    private String formatoArchivo;
    
    public Audiolibro(String identificador, String titulo, String autor, String narrador,
                     int duracionMinutos, String formatoArchivo, CategoriaRecurso categoria) {
        super(identificador, titulo, categoria);
        this.autor = autor;
        this.narrador = narrador;
        this.duracionMinutos = duracionMinutos;
        this.formatoArchivo = formatoArchivo;
    }
    
    public String getAutor() {
        return autor;
    }
    
    public String getNarrador() {
        return narrador;
    }
    
    public int getDuracionMinutos() {
        return duracionMinutos;
    }
    
    public String getFormatoArchivo() {
        return formatoArchivo;
    }
    
    @Override
    protected LocalDateTime calcularFechaDevolucion() {
        // Los audiolibros se prestan por 10 días
        return LocalDateTime.now().plusDays(10);
    }
    
    @Override
    public String toString() {
        return "Audiolibro{" +
                "id='" + identificador + '\'' +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", narrador='" + narrador + '\'' +
                ", duracion=" + duracionMinutos + " min" +
                ", formato='" + formatoArchivo + '\'' +
                ", categoria=" + categoria +
                ", estado=" + estado +
                '}';
    }
} 