package sistema.biblioteca.gestores;

import sistema.biblioteca.modelos.CategoriaRecurso;
import sistema.biblioteca.modelos.RecursoBase;
import sistema.biblioteca.modelos.ReglaRenovacion;
import sistema.biblioteca.modelos.Usuario;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestor para administrar las reglas de renovación de préstamos.
 * Permite definir reglas específicas por categoría de recurso 
 * y verificar si una renovación es posible.
 */
public class GestorReglaRenovacion {
    private Map<CategoriaRecurso, ReglaRenovacion> reglasRenovacion;
    
    // Valores por defecto
    private static final int MAXIMO_RENOVACIONES_DEFAULT = 2;
    private static final int DIAS_POR_RENOVACION_DEFAULT = 7;
    
    public GestorReglaRenovacion() {
        this.reglasRenovacion = new HashMap<>();
        inicializarReglasDefault();
    }
    
    /**
     * Inicializa reglas por defecto para cada categoría
     */
    private void inicializarReglasDefault() {
        // Académicos: máximo 3 renovaciones de 10 días cada una
        agregarRegla(new ReglaRenovacion(CategoriaRecurso.ACADEMICO, 3, 10, false));
        
        // Literatura: máximo 2 renovaciones de 7 días cada una
        agregarRegla(new ReglaRenovacion(CategoriaRecurso.LITERATURA, 2, 7, false));
        
        // Histórico: máximo 1 renovación de 5 días, requiere autorización
        agregarRegla(new ReglaRenovacion(CategoriaRecurso.HISTORICO, 1, 5, true));
        
        // Referencia: máximo 1 renovación de 3 días
        agregarRegla(new ReglaRenovacion(CategoriaRecurso.REFERENCIA, 1, 3, false));
        
        // Default para otras categorías (multimedia, investigación, etc.)
        agregarRegla(new ReglaRenovacion(CategoriaRecurso.MULTIMEDIA, 2, 5, false));
        agregarRegla(new ReglaRenovacion(CategoriaRecurso.INVESTIGACION, 3, 14, true));
    }
    
    /**
     * Agrega una nueva regla de renovación para una categoría.
     * Si ya existe una regla para esa categoría, la sobrescribe.
     * 
     * @param regla La regla de renovación a agregar
     */
    public void agregarRegla(ReglaRenovacion regla) {
        if (regla != null) {
            reglasRenovacion.put(regla.getCategoriaRecurso(), regla);
        }
    }
    
    /**
     * Obtiene la regla de renovación para una categoría específica.
     * 
     * @param categoria La categoría de recurso
     * @return La regla de renovación correspondiente o una regla por defecto
     */
    public ReglaRenovacion obtenerRegla(CategoriaRecurso categoria) {
        ReglaRenovacion regla = reglasRenovacion.get(categoria);
        
        if (regla == null) {
            // Si no hay regla definida para esta categoría, crear una por defecto
            regla = new ReglaRenovacion(
                categoria, 
                MAXIMO_RENOVACIONES_DEFAULT, 
                DIAS_POR_RENOVACION_DEFAULT,
                false
            );
            agregarRegla(regla);
        }
        
        return regla;
    }
    
    /**
     * Verifica si un recurso puede ser renovado según sus renovaciones previas.
     * 
     * @param recurso El recurso a verificar
     * @param cantidadRenovacionesActual Número actual de renovaciones del préstamo
     * @return true si puede ser renovado, false en caso contrario
     */
    public boolean puedeRenovarse(RecursoBase recurso, int cantidadRenovacionesActual) {
        if (recurso == null) {
            return false;
        }
        
        ReglaRenovacion regla = obtenerRegla(recurso.getCategoria());
        return cantidadRenovacionesActual < regla.getMaximoRenovaciones();
    }
    
    /**
     * Determina el número de días a extender en una renovación según la categoría.
     * 
     * @param recurso El recurso a renovar
     * @return Número de días para la renovación
     */
    public int getDiasParaRenovacion(RecursoBase recurso) {
        if (recurso == null) {
            return DIAS_POR_RENOVACION_DEFAULT;
        }
        
        return obtenerRegla(recurso.getCategoria()).getDiasPorRenovacion();
    }
    
    /**
     * Verifica si la renovación requiere autorización especial.
     * 
     * @param recurso El recurso a renovar
     * @return true si requiere autorización, false en caso contrario
     */
    public boolean requiereAutorizacion(RecursoBase recurso) {
        if (recurso == null) {
            return false;
        }
        
        return obtenerRegla(recurso.getCategoria()).requiereAutorizacion();
    }
    
    /**
     * Obtiene el máximo número de renovaciones permitidas para un recurso.
     * 
     * @param recurso El recurso a consultar
     * @return El número máximo de renovaciones permitidas
     */
    public int getMaximoRenovacionesPermitidas(RecursoBase recurso) {
        if (recurso == null) {
            return MAXIMO_RENOVACIONES_DEFAULT;
        }
        
        return obtenerRegla(recurso.getCategoria()).getMaximoRenovaciones();
    }
} 