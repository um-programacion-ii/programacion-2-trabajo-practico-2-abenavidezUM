package sistema.biblioteca.modelos;

import java.time.LocalDateTime;

/**
 * Clase que representa un recurso multimedia en la biblioteca.
 * Puede ser video, audio, software, etc.
 */
public class RecursoMultimedia extends RecursoBase {
    private String tipoMultimedia; // video, audio, software, etc.
    private int duracionMinutos;
    private String formato;
    
    public RecursoMultimedia(String identificador, String titulo, 
                          CategoriaRecurso categoria, String tipoMultimedia, int duracionMinutos) {
        super(identificador, titulo, categoria);
        this.tipoMultimedia = tipoMultimedia;
        this.duracionMinutos = duracionMinutos;
        this.formato = "MP4"; // valor por defecto
    }
    
    public RecursoMultimedia(String identificador, String titulo, CategoriaRecurso categoria,
                          String tipoMultimedia, int duracionMinutos, String formato) {
        this(identificador, titulo, categoria, tipoMultimedia, duracionMinutos);
        this.formato = formato;
    }
    
    public String getTipoMultimedia() {
        return tipoMultimedia;
    }
    
    public int getDuracionMinutos() {
        return duracionMinutos;
    }
    
    public String getFormato() {
        return formato;
    }
    
    @Override
    protected LocalDateTime calcularFechaDevolucion() {
        // Los recursos multimedia se prestan por 7 días
        return LocalDateTime.now().plusDays(7);
    }
    
    @Override
    public String toString() {
        return "Multimedia{" +
                "id='" + identificador + '\'' +
                ", titulo='" + titulo + '\'' +
                ", tipo='" + tipoMultimedia + '\'' +
                ", duración=" + duracionMinutos + " min" +
                ", formato='" + formato + '\'' +
                ", categoria=" + categoria +
                ", estado=" + estado +
                '}';
    }
} 