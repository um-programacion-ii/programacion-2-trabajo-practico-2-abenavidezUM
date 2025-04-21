package sistema.biblioteca.reportes;

import sistema.biblioteca.gestores.GestorPrestamos;
import sistema.biblioteca.modelos.CategoriaRecurso;
import sistema.biblioteca.modelos.Prestamo;
import sistema.biblioteca.modelos.RecursoBase;
import sistema.biblioteca.modelos.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase especializada en generar reportes específicos relacionados con préstamos
 */
public class ReportesPrestamos {
    
    private final GestorPrestamos gestorPrestamos;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public ReportesPrestamos(GestorPrestamos gestorPrestamos) {
        this.gestorPrestamos = gestorPrestamos;
    }
    
    /**
     * Genera un reporte detallado de préstamos activos con información adicional
     * @return Lista de mapas con información detallada de cada préstamo activo
     */
    public List<Map<String, Object>> generarReportePrestamosActivos() {
        List<Prestamo> prestamosActivos = gestorPrestamos.listarPrestamosActivos();
        return formatearPrestamosDetallados(prestamosActivos);
    }
    
    /**
     * Genera un reporte detallado de préstamos vencidos con días de atraso
     * @return Lista de mapas con información detallada de cada préstamo vencido
     */
    public List<Map<String, Object>> generarReportePrestamosVencidos() {
        List<Prestamo> prestamosVencidos = gestorPrestamos.listarPrestamosVencidos();
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        LocalDateTime ahora = LocalDateTime.now();
        
        for (Prestamo prestamo : prestamosVencidos) {
            Map<String, Object> prestamoInfo = new HashMap<>();
            prestamoInfo.put("id", prestamo.getId());
            prestamoInfo.put("recurso", prestamo.getRecurso());
            prestamoInfo.put("usuario", prestamo.getUsuario());
            prestamoInfo.put("fechaPrestamo", prestamo.getFechaPrestamo().format(FORMATO_FECHA));
            prestamoInfo.put("fechaVencimiento", prestamo.getFechaDevolucionEstimada().format(FORMATO_FECHA));
            
            // Calcular días de atraso
            long diasAtraso = ChronoUnit.DAYS.between(prestamo.getFechaDevolucionEstimada(), ahora);
            prestamoInfo.put("diasAtraso", diasAtraso);
            
            // Categorizar el nivel de atraso
            String nivelAtraso;
            if (diasAtraso <= 3) {
                nivelAtraso = "Leve";
            } else if (diasAtraso <= 7) {
                nivelAtraso = "Moderado";
            } else {
                nivelAtraso = "Severo";
            }
            prestamoInfo.put("nivelAtraso", nivelAtraso);
            
            resultado.add(prestamoInfo);
        }
        
        return resultado;
    }
    
    /**
     * Genera un reporte de préstamos por período de tiempo
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return Lista de préstamos en el período especificado
     */
    public List<Map<String, Object>> generarReportePrestamosEnPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior o igual a la fecha de fin");
        }
        
        LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
        LocalDateTime finDateTime = fechaFin.atTime(23, 59, 59);
        
        List<Prestamo> prestamosEnPeriodo = gestorPrestamos.listarTodosLosPrestamos().stream()
                .filter(p -> {
                    LocalDateTime fechaPrestamo = p.getFechaPrestamo();
                    return !fechaPrestamo.isBefore(inicioDateTime) && !fechaPrestamo.isAfter(finDateTime);
                })
                .collect(Collectors.toList());
        
        return formatearPrestamosDetallados(prestamosEnPeriodo);
    }
    
    /**
     * Genera un reporte histórico de préstamos de un recurso específico
     * @param idRecurso Identificador del recurso
     * @return Lista con el historial de préstamos del recurso
     */
    public List<Map<String, Object>> generarHistorialPrestamosPorRecurso(String idRecurso) {
        if (idRecurso == null || idRecurso.isEmpty()) {
            throw new IllegalArgumentException("El ID del recurso es obligatorio");
        }
        
        List<Prestamo> prestamosPorRecurso = gestorPrestamos.buscarPrestamosPorRecurso(idRecurso);
        return formatearPrestamosDetallados(prestamosPorRecurso);
    }
    
    /**
     * Genera un reporte histórico de préstamos de un usuario específico
     * @param idUsuario Identificador del usuario
     * @return Lista con el historial de préstamos del usuario
     */
    public List<Map<String, Object>> generarHistorialPrestamosPorUsuario(String idUsuario) {
        if (idUsuario == null || idUsuario.isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        }
        
        List<Prestamo> prestamosPorUsuario = gestorPrestamos.buscarPrestamosPorUsuario(idUsuario);
        return formatearPrestamosDetallados(prestamosPorUsuario);
    }
    
    /**
     * Genera un reporte de préstamos por categoría de recurso
     * @param categoria Categoría de recursos a filtrar
     * @return Lista de préstamos de recursos en la categoría especificada
     */
    public List<Map<String, Object>> generarReportePrestamosPorCategoria(CategoriaRecurso categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        
        List<Prestamo> prestamosPorCategoria = gestorPrestamos.listarTodosLosPrestamos().stream()
                .filter(p -> p.getRecurso().getCategoria() == categoria)
                .collect(Collectors.toList());
        
        return formatearPrestamosDetallados(prestamosPorCategoria);
    }
    
    /**
     * Genera estadísticas de duración de préstamos por categoría
     * @return Mapa con categorías y duración promedio de préstamos
     */
    public Map<CategoriaRecurso, Double> generarEstadisticasDuracionPrestamos() {
        Map<CategoriaRecurso, List<Long>> duracionesPorCategoria = new HashMap<>();
        
        // Solo considerar préstamos devueltos (con fecha de devolución real)
        List<Prestamo> prestamosDevueltos = gestorPrestamos.listarTodosLosPrestamos().stream()
                .filter(p -> p.getFechaDevolucionReal() != null)
                .collect(Collectors.toList());
        
        // Agrupar duraciones por categoría
        for (Prestamo prestamo : prestamosDevueltos) {
            CategoriaRecurso categoria = prestamo.getRecurso().getCategoria();
            long duracionDias = ChronoUnit.DAYS.between(prestamo.getFechaPrestamo(), prestamo.getFechaDevolucionReal());
            
            if (!duracionesPorCategoria.containsKey(categoria)) {
                duracionesPorCategoria.put(categoria, new ArrayList<>());
            }
            duracionesPorCategoria.get(categoria).add(duracionDias);
        }
        
        // Calcular duración promedio por categoría
        Map<CategoriaRecurso, Double> promediosPorCategoria = new HashMap<>();
        for (Map.Entry<CategoriaRecurso, List<Long>> entry : duracionesPorCategoria.entrySet()) {
            List<Long> duraciones = entry.getValue();
            double promedio = duraciones.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0);
            promediosPorCategoria.put(entry.getKey(), promedio);
        }
        
        // Ordenar de mayor a menor por duración promedio
        return promediosPorCategoria.entrySet().stream()
                .sorted(Map.Entry.<CategoriaRecurso, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                    Map.Entry::getKey, 
                    Map.Entry::getValue, 
                    (oldValue, newValue) -> oldValue, 
                    LinkedHashMap::new
                ));
    }
    
    /**
     * Calcula la tasa de renovación de préstamos
     * @return Mapa con estadísticas de renovación
     */
    public Map<String, Object> generarEstadisticasRenovaciones() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Aquí iría la lógica para calcular renovaciones
        // Como aún no tenemos esa información en el modelo, devolvemos datos simulados
        
        estadisticas.put("totalPrestamos", gestorPrestamos.getCantidadPrestamosTotales());
        estadisticas.put("prestamosRenovados", 0);
        estadisticas.put("tasaRenovacion", 0.0);
        estadisticas.put("tipoRecursoMasRenovado", "Libro");
        
        return estadisticas;
    }
    
    /**
     * Genera un análisis de cumplimiento de plazos de devolución
     * @return Mapa con estadísticas de cumplimiento
     */
    public Map<String, Object> generarAnalisisCumplimientoPlazos() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        List<Prestamo> prestamosDevueltos = gestorPrestamos.listarTodosLosPrestamos().stream()
                .filter(p -> p.getFechaDevolucionReal() != null)
                .collect(Collectors.toList());
        
        int devueltosATiempo = 0;
        int devueltosTarde = 0;
        long totalDiasAtraso = 0;
        
        for (Prestamo prestamo : prestamosDevueltos) {
            if (prestamo.getFechaDevolucionReal().isAfter(prestamo.getFechaDevolucionEstimada())) {
                devueltosTarde++;
                totalDiasAtraso += ChronoUnit.DAYS.between(
                        prestamo.getFechaDevolucionEstimada(), 
                        prestamo.getFechaDevolucionReal());
            } else {
                devueltosATiempo++;
            }
        }
        
        int totalDevueltos = prestamosDevueltos.size();
        double tasaCumplimiento = totalDevueltos > 0 ? 
                ((double) devueltosATiempo / totalDevueltos) * 100 : 0;
        double promedioDiasAtraso = devueltosTarde > 0 ? 
                ((double) totalDiasAtraso / devueltosTarde) : 0;
        
        estadisticas.put("totalDevueltos", totalDevueltos);
        estadisticas.put("devueltosATiempo", devueltosATiempo);
        estadisticas.put("devueltosTarde", devueltosTarde);
        estadisticas.put("tasaCumplimiento", tasaCumplimiento);
        estadisticas.put("promedioDiasAtraso", promedioDiasAtraso);
        
        return estadisticas;
    }
    
    // Método auxiliar para formatear préstamos con información detallada
    private List<Map<String, Object>> formatearPrestamosDetallados(List<Prestamo> prestamos) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();
        
        for (Prestamo prestamo : prestamos) {
            Map<String, Object> prestamoInfo = new HashMap<>();
            RecursoBase recurso = prestamo.getRecurso();
            Usuario usuario = prestamo.getUsuario();
            
            prestamoInfo.put("id", prestamo.getId());
            prestamoInfo.put("recurso", recurso);
            prestamoInfo.put("tituloRecurso", recurso.getTitulo());
            prestamoInfo.put("idRecurso", recurso.getIdentificador());
            prestamoInfo.put("categoriaRecurso", recurso.getCategoria());
            prestamoInfo.put("usuario", usuario);
            prestamoInfo.put("nombreUsuario", usuario.getNombre());
            prestamoInfo.put("idUsuario", usuario.getId());
            prestamoInfo.put("fechaPrestamo", prestamo.getFechaPrestamo().format(FORMATO_FECHA));
            prestamoInfo.put("fechaVencimiento", prestamo.getFechaDevolucionEstimada().format(FORMATO_FECHA));
            
            if (prestamo.getFechaDevolucionReal() != null) {
                prestamoInfo.put("estado", "Devuelto");
                prestamoInfo.put("fechaDevolucion", prestamo.getFechaDevolucionReal().format(FORMATO_FECHA));
                
                long duracionDias = ChronoUnit.DAYS.between(
                        prestamo.getFechaPrestamo(), 
                        prestamo.getFechaDevolucionReal());
                prestamoInfo.put("duracionDias", duracionDias);
                
                boolean devueltoATiempo = !prestamo.getFechaDevolucionReal().isAfter(prestamo.getFechaDevolucionEstimada());
                prestamoInfo.put("devueltoATiempo", devueltoATiempo);
                
                if (!devueltoATiempo) {
                    long diasAtraso = ChronoUnit.DAYS.between(
                            prestamo.getFechaDevolucionEstimada(), 
                            prestamo.getFechaDevolucionReal());
                    prestamoInfo.put("diasAtraso", diasAtraso);
                }
            } else {
                prestamoInfo.put("estado", "Activo");
                
                boolean estaVencido = prestamo.getFechaDevolucionEstimada().isBefore(ahora);
                prestamoInfo.put("vencido", estaVencido);
                
                if (estaVencido) {
                    long diasAtraso = ChronoUnit.DAYS.between(
                            prestamo.getFechaDevolucionEstimada(), 
                            ahora);
                    prestamoInfo.put("diasAtraso", diasAtraso);
                } else {
                    long diasRestantes = ChronoUnit.DAYS.between(
                            ahora, 
                            prestamo.getFechaDevolucionEstimada());
                    prestamoInfo.put("diasRestantes", diasRestantes);
                }
            }
            
            resultado.add(prestamoInfo);
        }
        
        return resultado;
    }
} 