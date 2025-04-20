package sistema.biblioteca.modelos;

import java.time.LocalDateTime;

public class Libro extends RecursoBase {
    private String autor;
    private String isbn;
    private int numeroPaginas;
    private int anioPublicacion;
    
    public Libro(String identificador, String titulo, String autor, String isbn, CategoriaRecurso categoria) {
        super(identificador, titulo, categoria);
        this.autor = autor;
        this.isbn = isbn;
    }
    
    public Libro(String identificador, String titulo, String autor, String isbn, 
                int numeroPaginas, int anioPublicacion, CategoriaRecurso categoria) {
        this(identificador, titulo, autor, isbn, categoria);
        this.numeroPaginas = numeroPaginas;
        this.anioPublicacion = anioPublicacion;
    }
    
    public String getAutor() {
        return autor;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public int getNumeroPaginas() {
        return numeroPaginas;
    }
    
    public int getAnioPublicacion() {
        return anioPublicacion;
    }
    
    @Override
    protected LocalDateTime calcularFechaDevolucion() {
        // Los libros se prestan por 15 días
        return LocalDateTime.now().plusDays(15);
    }
    
    @Override
    public String toString() {
        return "Libro{" +
                "id='" + identificador + '\'' +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", isbn='" + isbn + '\'' +
                ", categoria=" + categoria +
                ", estado=" + estado +
                '}';
    }
} 