package sistema.biblioteca.modelos;

/**
 * Clase que define las reglas de renovación para diferentes tipos de recursos.
 * Permite establecer límites específicos por categoría de recurso.
 */
public class ReglaRenovacion {
    private CategoriaRecurso categoriaRecurso;
    private int maximoRenovaciones;
    private int diasPorRenovacion;
    private boolean requiereAutorizacion;
    
    public ReglaRenovacion(CategoriaRecurso categoriaRecurso, int maximoRenovaciones, 
                         int diasPorRenovacion, boolean requiereAutorizacion) {
        this.categoriaRecurso = categoriaRecurso;
        this.maximoRenovaciones = maximoRenovaciones;
        this.diasPorRenovacion = diasPorRenovacion;
        this.requiereAutorizacion = requiereAutorizacion;
    }
    
    public CategoriaRecurso getCategoriaRecurso() {
        return categoriaRecurso;
    }
    
    public int getMaximoRenovaciones() {
        return maximoRenovaciones;
    }
    
    public int getDiasPorRenovacion() {
        return diasPorRenovacion;
    }
    
    public boolean requiereAutorizacion() {
        return requiereAutorizacion;
    }
    
    @Override
    public String toString() {
        return "ReglaRenovacion{" +
                "categoriaRecurso=" + categoriaRecurso +
                ", maximoRenovaciones=" + maximoRenovaciones +
                ", diasPorRenovacion=" + diasPorRenovacion +
                ", requiereAutorizacion=" + requiereAutorizacion +
                '}';
    }
} 