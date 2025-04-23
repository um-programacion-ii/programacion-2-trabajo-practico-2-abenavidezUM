package sistema.biblioteca.modelos;

import java.time.LocalDateTime;

public class Revista extends RecursoBase {
    private String editorial;
    private int numeroEdicion;
    private int mes;
    private int anio;
    
    public Revista(String identificador, String titulo, String editorial, int numeroEdicion, 
                  int mes, int anio, CategoriaRecurso categoria) {
        super(identificador, titulo, categoria);
        this.editorial = editorial;
        this.numeroEdicion = numeroEdicion;
        this.mes = mes;
        this.anio = anio;
    }
    
    public String getEditorial() {
        return editorial;
    }
    
    public int getNumeroEdicion() {
        return numeroEdicion;
    }
    
    public int getMes() {
        return mes;
    }
    
    public int getAnio() {
        return anio;
    }
    
    @Override
    protected LocalDateTime calcularFechaDevolucion() {
        // Las revistas se prestan por 7 días
        return LocalDateTime.now().plusDays(7);
    }
    
    @Override
    public String toString() {
        return "Revista{" +
                "id='" + identificador + '\'' +
                ", titulo='" + titulo + '\'' +
                ", editorial='" + editorial + '\'' +
                ", edicion=" + numeroEdicion +
                ", mes=" + mes +
                ", anio=" + anio +
                ", categoria=" + categoria +
                ", estado=" + estado +
                '}';
    }
} 