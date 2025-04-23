package sistema.biblioteca.gestores;

import sistema.biblioteca.excepciones.RecursoNoDisponibleException;
import sistema.biblioteca.interfaces.RecursoDigital;
import sistema.biblioteca.modelos.CategoriaRecurso;
import sistema.biblioteca.modelos.EstadoRecurso;
import sistema.biblioteca.modelos.RecursoBase;
import sistema.biblioteca.modelos.Usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GestorRecursos {
    private Map<String, RecursoBase> recursos;
    
    public GestorRecursos() {
        this.recursos = new HashMap<>();
    }
    
    public void agregarRecurso(RecursoBase recurso) {
        if (recurso == null || recurso.getIdentificador() == null) {
            return;
        }
        
        recursos.put(recurso.getIdentificador(), recurso);
    }
    
    public RecursoBase buscarRecursoPorId(String id) {
        return recursos.get(id);
    }
    
    public List<RecursoBase> listarRecursos() {
        return new ArrayList<>(recursos.values());
    }
    
    public List<RecursoBase> listarRecursosPorCategoria(CategoriaRecurso categoria) {
        List<RecursoBase> resultado = new ArrayList<>();
        
        if (categoria == null) {
            return resultado;
        }
        
        for (RecursoBase recurso : recursos.values()) {
            if (recurso.getCategoria() == categoria) {
                resultado.add(recurso);
            }
        }
        
        return resultado;
    }
    
    public List<RecursoBase> buscarRecursosPorTitulo(String titulo) {
        List<RecursoBase> resultado = new ArrayList<>();
        
        if (titulo == null || titulo.isEmpty()) {
            return resultado;
        }
        
        // Buscar con for tradicional en lugar de streams
        for (RecursoBase recurso : recursos.values()) {
            if (recurso.getTitulo().toLowerCase().contains(titulo.toLowerCase())) {
                resultado.add(recurso);
            }
        }
        
        return resultado;
    }
    
    public boolean existeRecurso(String id) {
        return recursos.containsKey(id);
    }
    
    public void prestarRecurso(String idRecurso, Usuario usuario) throws RecursoNoDisponibleException {
        RecursoBase recurso = buscarRecursoPorId(idRecurso);
        
        if (recurso == null) {
            throw new RecursoNoDisponibleException("El recurso no existe: " + idRecurso);
        }
        
        if (!recurso.estaDisponible()) {
            throw new RecursoNoDisponibleException("El recurso no está disponible: " + idRecurso);
        }
        
        recurso.prestar(usuario);
    }
    
    public void devolverRecurso(String idRecurso) {
        RecursoBase recurso = buscarRecursoPorId(idRecurso);
        
        if (recurso != null) {
            recurso.devolver();
        }
    }
    
    public void actualizarEstadoRecurso(String idRecurso, EstadoRecurso nuevoEstado) {
        RecursoBase recurso = buscarRecursoPorId(idRecurso);
        
        if (recurso != null) {
            recurso.actualizarEstado(nuevoEstado);
        }
    }
    
    public List<RecursoBase> getRecursosDisponibles() {
        return recursos.values().stream()
                .filter(RecursoBase::estaDisponible)
                .collect(Collectors.toList());
    }
    
    // Para reportes estadísticos
    public Map<CategoriaRecurso, Integer> contarRecursosPorCategoria() {
        Map<CategoriaRecurso, Integer> resultado = new HashMap<>();
        
        for (RecursoBase recurso : recursos.values()) {
            CategoriaRecurso categoria = recurso.getCategoria();
            resultado.put(categoria, resultado.getOrDefault(categoria, 0) + 1);
        }
        
        return resultado;
    }
    
    // Eliminar recurso
    public void eliminarRecurso(String id) {
        recursos.remove(id);
    }
    
    public int getCantidadTotalRecursos() {
        return recursos.size();
    }
} 