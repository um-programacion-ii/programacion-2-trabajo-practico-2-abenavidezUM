package sistema.biblioteca.reportes;

import sistema.biblioteca.gestores.GestorPrestamos;
import sistema.biblioteca.gestores.GestorRecursos;
import sistema.biblioteca.gestores.GestorUsuarios;
import sistema.biblioteca.modelos.CategoriaRecurso;
import sistema.biblioteca.modelos.Prestamo;
import sistema.biblioteca.modelos.RecursoBase;
import sistema.biblioteca.modelos.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase que genera reportes sobre préstamos, recursos y usuarios del sistema
 */
public class GeneradorReportes {
    
    private final GestorPrestamos gestorPrestamos;
    private final GestorRecursos gestorRecursos;
    private final GestorUsuarios gestorUsuarios;
    
    public GeneradorReportes(GestorPrestamos gestorPrestamos, GestorRecursos gestorRecursos, GestorUsuarios gestorUsuarios) {
        this.gestorPrestamos = gestorPrestamos;
        this.gestorRecursos = gestorRecursos;
        this.gestorUsuarios = gestorUsuarios;
    }
    
    /**
     * Genera un informe con las estadísticas generales de préstamos
     * @return Mapa con las estadísticas
     */
    public Map<String, Object> generarEstadisticasPrestamos() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        List<Prestamo> todosLosPrestamos = gestorPrestamos.listarTodosLosPrestamos();
        List<Prestamo> prestamosActivos = gestorPrestamos.listarPrestamosActivos();
        
        // Contar préstamos por estado
        int totalPrestamos = todosLosPrestamos.size();
        int prestamosDevueltos = (int) todosLosPrestamos.stream()
                .filter(p -> p.getFechaDevolucionReal() != null)
                .count();
        int prestamosAtrasados = (int) prestamosActivos.stream()
                .filter(p -> p.getFechaDevolucionEstimada().isBefore(LocalDateTime.now()))
                .count();
        
        // Calcular tiempos promedio
        double promedioDiasPrestamo = todosLosPrestamos.stream()
                .filter(p -> p.getFechaDevolucionReal() != null)
                .mapToLong(p -> java.time.Duration.between(
                        p.getFechaPrestamo(), 
                        p.getFechaDevolucionReal()).toDays())
                .average()
                .orElse(0);
        
        // Guardar estadísticas
        estadisticas.put("totalPrestamos", totalPrestamos);
        estadisticas.put("prestamosActivos", prestamosActivos.size());
        estadisticas.put("prestamosDevueltos", prestamosDevueltos);
        estadisticas.put("prestamosAtrasados", prestamosAtrasados);
        estadisticas.put("promedioDiasPrestamo", promedioDiasPrestamo);
        
        return estadisticas;
    }
    
    /**
     * Lista los recursos más prestados, ordenados por cantidad de préstamos
     * @param limite Cantidad máxima de recursos a listar
     * @return Lista de recursos con su contador de préstamos
     */
    public List<Map<String, Object>> listarRecursosMasPrestados(int limite) {
        Map<String, Integer> contadorPrestamos = new HashMap<>();
        
        // Contar préstamos por recurso
        for (Prestamo prestamo : gestorPrestamos.listarTodosLosPrestamos()) {
            String idRecurso = prestamo.getRecurso().getIdentificador();
            contadorPrestamos.put(idRecurso, contadorPrestamos.getOrDefault(idRecurso, 0) + 1);
        }
        
        // Convertir a una lista para poder ordenar
        List<Map.Entry<String, Integer>> listaOrdenada = new ArrayList<>(contadorPrestamos.entrySet());
        
        // Ordenar de mayor a menor por número de préstamos
        Collections.sort(listaOrdenada, (e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        
        // Crear lista de resultados
        List<Map<String, Object>> resultado = new ArrayList<>();
        int count = 0;
        
        for (Map.Entry<String, Integer> entry : listaOrdenada) {
            if (count >= limite) break;
            
            RecursoBase recurso = gestorRecursos.buscarRecursoPorId(entry.getKey());
            if (recurso != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("recurso", recurso);
                item.put("cantidadPrestamos", entry.getValue());
                resultado.add(item);
                count++;
            }
        }
        
        return resultado;
    }
    
    /**
     * Lista los usuarios más activos según cantidad de préstamos realizados
     * @param limite Cantidad máxima de usuarios a listar
     * @return Lista de usuarios con su contador de préstamos
     */
    public List<Map<String, Object>> listarUsuariosMasActivos(int limite) {
        Map<String, Integer> contadorUsuarios = new HashMap<>();
        
        // Contar préstamos por usuario
        for (Prestamo prestamo : gestorPrestamos.listarTodosLosPrestamos()) {
            String idUsuario = prestamo.getUsuario().getId();
            contadorUsuarios.put(idUsuario, contadorUsuarios.getOrDefault(idUsuario, 0) + 1);
        }
        
        // Ordenar de mayor a menor
        Map<String, Integer> usuariosOrdenados = contadorUsuarios.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limite)
                .collect(Collectors.toMap(
                    Map.Entry::getKey, 
                    Map.Entry::getValue, 
                    (oldValue, newValue) -> oldValue, 
                    LinkedHashMap::new
                ));
        
        // Crear lista de resultados
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : usuariosOrdenados.entrySet()) {
            try {
                Usuario usuario = gestorUsuarios.buscarUsuarioPorId(entry.getKey());
                if (usuario != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("usuario", usuario);
                    item.put("cantidadPrestamos", entry.getValue());
                    resultado.add(item);
                }
            } catch (Exception e) {
                System.out.println("Error al buscar usuario por ID: " + e.getMessage());
            }
        }
        
        return resultado;
    }
    
    /**
     * Genera un informe de distribución de recursos por categoría
     * @return Mapa con la cantidad de recursos por categoría
     */
    public Map<CategoriaRecurso, Integer> obtenerDistribucionPorCategoria() {
        Map<CategoriaRecurso, Integer> distribucion = new HashMap<>();
        
        // Para cada categoría, contar cuántos recursos hay
        for (CategoriaRecurso categoria : CategoriaRecurso.values()) {
            int cantidad = gestorRecursos.listarRecursosPorCategoria(categoria).size();
            distribucion.put(categoria, cantidad);
        }
        
        // Ordenar el mapa por cantidad (de mayor a menor)
        return distribucion.entrySet()
                .stream()
                .sorted(Map.Entry.<CategoriaRecurso, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                    Map.Entry::getKey, 
                    Map.Entry::getValue, 
                    (oldValue, newValue) -> oldValue, 
                    LinkedHashMap::new
                ));
    }
    
    /**
     * Genera un informe sobre el estado actual de disponibilidad de recursos
     * @return Mapa con la cantidad de recursos por estado
     */
    public Map<String, Integer> obtenerEstadisticasDisponibilidad() {
        Map<String, Integer> estadisticas = new HashMap<>();
        List<RecursoBase> todosLosRecursos = gestorRecursos.listarRecursos();
        
        int disponibles = 0;
        int prestados = 0;
        int reservados = 0;
        int otros = 0;
        
        for (RecursoBase recurso : todosLosRecursos) {
            if (recurso.estaDisponible()) {
                disponibles++;
            } else {
                switch (recurso.getEstado()) {
                    case PRESTADO:
                        prestados++;
                        break;
                    case RESERVADO:
                        reservados++;
                        break;
                    default:
                        otros++;
                        break;
                }
            }
        }
        
        estadisticas.put("disponibles", disponibles);
        estadisticas.put("prestados", prestados);
        estadisticas.put("reservados", reservados);
        estadisticas.put("otros", otros);
        estadisticas.put("total", todosLosRecursos.size());
        
        return estadisticas;
    }
    
    /**
     * Genera un informe de rendimiento del sistema de préstamos
     * @return Mapa con estadísticas de rendimiento
     */
    public Map<String, Object> generarEstadisticasRendimiento() {
        Map<String, Object> estadisticas = new HashMap<>();
        List<Prestamo> prestamos = gestorPrestamos.listarTodosLosPrestamos();
        
        // Total de préstamos
        estadisticas.put("totalPrestamos", prestamos.size());
        
        // Préstamos por mes (últimos 6 meses)
        Map<String, Integer> prestamosPorMes = new HashMap<>();
        LocalDateTime seisAtras = LocalDateTime.now().minusMonths(6);
        
        for (Prestamo p : prestamos) {
            if (p.getFechaPrestamo().isAfter(seisAtras)) {
                String mesAño = p.getFechaPrestamo().getMonth() + " " + p.getFechaPrestamo().getYear();
                prestamosPorMes.put(mesAño, prestamosPorMes.getOrDefault(mesAño, 0) + 1);
            }
        }
        estadisticas.put("prestamosPorMes", prestamosPorMes);
        
        // Tiempo promedio de procesamiento
        // (En una implementación real, esto vendría de mediciones del sistema)
        estadisticas.put("tiempoPromedioProcesamiento", "0.5 segundos");
        
        return estadisticas;
    }
} 