package sistema.biblioteca.reportes;

import sistema.biblioteca.modelos.CategoriaRecurso;
import sistema.biblioteca.modelos.Prestamo;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Clase que exporta los reportes específicos de préstamos generados por ReportesPrestamos
 */
public class ExportadorReportesPrestamos {
    
    private static final DateTimeFormatter FORMATO_FECHA_ARCHIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ReportesPrestamos generador;
    
    public ExportadorReportesPrestamos(ReportesPrestamos generador) {
        this.generador = generador;
    }
    
    /**
     * Muestra por consola un reporte de los préstamos activos
     */
    public void mostrarReportePrestamosActivos() {
        List<Map<String, Object>> prestamosActivos = generador.generarReportePrestamosActivos();
        
        System.out.println("=== REPORTE DE PRÉSTAMOS ACTIVOS ===");
        System.out.println("Total de préstamos activos: " + prestamosActivos.size());
        System.out.println("----------------------------------");
        
        for (Map<String, Object> prestamo : prestamosActivos) {
            System.out.println("ID: " + prestamo.get("id"));
            System.out.println("Recurso: " + prestamo.get("tituloRecurso") + " (" + prestamo.get("idRecurso") + ")");
            System.out.println("Usuario: " + prestamo.get("nombreUsuario") + " (" + prestamo.get("idUsuario") + ")");
            System.out.println("Fecha préstamo: " + prestamo.get("fechaPrestamo"));
            System.out.println("Fecha vencimiento: " + prestamo.get("fechaVencimiento"));
            
            if ((boolean) prestamo.getOrDefault("vencido", false)) {
                System.out.println("Estado: VENCIDO");
                System.out.println("Días de atraso: " + prestamo.get("diasAtraso"));
            } else {
                System.out.println("Estado: Vigente");
                System.out.println("Días restantes: " + prestamo.get("diasRestantes"));
            }
            
            System.out.println("----------------------------------");
        }
    }
    
    /**
     * Muestra por consola un reporte de los préstamos vencidos
     */
    public void mostrarReportePrestamosVencidos() {
        List<Map<String, Object>> prestamosVencidos = generador.generarReportePrestamosVencidos();
        
        System.out.println("=== REPORTE DE PRÉSTAMOS VENCIDOS ===");
        System.out.println("Total de préstamos vencidos: " + prestamosVencidos.size());
        System.out.println("----------------------------------");
        
        for (Map<String, Object> prestamo : prestamosVencidos) {
            System.out.println("ID: " + prestamo.get("id"));
            System.out.println("Recurso: " + ((sistema.biblioteca.modelos.RecursoBase) prestamo.get("recurso")).getTitulo());
            System.out.println("Usuario: " + ((sistema.biblioteca.modelos.Usuario) prestamo.get("usuario")).getNombre());
            System.out.println("Fecha préstamo: " + prestamo.get("fechaPrestamo"));
            System.out.println("Fecha vencimiento: " + prestamo.get("fechaVencimiento"));
            System.out.println("Días de atraso: " + prestamo.get("diasAtraso"));
            System.out.println("Nivel de atraso: " + prestamo.get("nivelAtraso"));
            System.out.println("----------------------------------");
        }
    }
    
    /**
     * Muestra por consola un análisis de cumplimiento de plazos
     */
    public void mostrarAnalisisCumplimientoPlazos() {
        Map<String, Object> analisis = generador.generarAnalisisCumplimientoPlazos();
        
        System.out.println("=== ANÁLISIS DE CUMPLIMIENTO DE PLAZOS ===");
        System.out.println("Total préstamos devueltos: " + analisis.get("totalDevueltos"));
        System.out.println("Devueltos a tiempo: " + analisis.get("devueltosATiempo"));
        System.out.println("Devueltos con atraso: " + analisis.get("devueltosTarde"));
        System.out.println("Tasa de cumplimiento: " + String.format("%.2f%%", analisis.get("tasaCumplimiento")));
        System.out.println("Promedio días de atraso: " + String.format("%.1f", analisis.get("promedioDiasAtraso")));
        System.out.println("----------------------------------");
    }
    
    /**
     * Muestra estadísticas de duración de préstamos por categoría
     */
    public void mostrarEstadisticasDuracionPrestamos() {
        Map<CategoriaRecurso, Double> estadisticas = generador.generarEstadisticasDuracionPrestamos();
        
        System.out.println("=== DURACIÓN PROMEDIO DE PRÉSTAMOS POR CATEGORÍA ===");
        for (Map.Entry<CategoriaRecurso, Double> entry : estadisticas.entrySet()) {
            System.out.println(entry.getKey() + ": " + String.format("%.1f días", entry.getValue()));
        }
        System.out.println("----------------------------------");
    }
    
    /**
     * Exporta el reporte de préstamos activos a un archivo
     * @return Ruta del archivo generado
     */
    public String exportarReportePrestamosActivos() {
        String nombreArchivo = "prestamos_activos_" + 
                LocalDateTime.now().format(FORMATO_FECHA_ARCHIVO) + ".txt";
        
        try (FileWriter escritor = new FileWriter(nombreArchivo)) {
            List<Map<String, Object>> prestamosActivos = generador.generarReportePrestamosActivos();
            
            escritor.write("=== REPORTE DE PRÉSTAMOS ACTIVOS ===\n");
            escritor.write("Generado el: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            escritor.write("Total de préstamos activos: " + prestamosActivos.size() + "\n");
            escritor.write("----------------------------------\n\n");
            
            for (Map<String, Object> prestamo : prestamosActivos) {
                escritor.write("ID: " + prestamo.get("id") + "\n");
                escritor.write("Recurso: " + prestamo.get("tituloRecurso") + 
                        " (" + prestamo.get("idRecurso") + ")\n");
                escritor.write("Categoría: " + prestamo.get("categoriaRecurso") + "\n");
                escritor.write("Usuario: " + prestamo.get("nombreUsuario") + 
                        " (" + prestamo.get("idUsuario") + ")\n");
                escritor.write("Fecha préstamo: " + prestamo.get("fechaPrestamo") + "\n");
                escritor.write("Fecha vencimiento: " + prestamo.get("fechaVencimiento") + "\n");
                
                if ((boolean) prestamo.getOrDefault("vencido", false)) {
                    escritor.write("Estado: VENCIDO\n");
                    escritor.write("Días de atraso: " + prestamo.get("diasAtraso") + "\n");
                } else {
                    escritor.write("Estado: Vigente\n");
                    escritor.write("Días restantes: " + prestamo.get("diasRestantes") + "\n");
                }
                
                escritor.write("----------------------------------\n");
            }
            
            System.out.println("Reporte de préstamos activos exportado correctamente a: " + nombreArchivo);
            return nombreArchivo;
            
        } catch (IOException e) {
            System.out.println("Error al exportar reporte: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Exporta el reporte de préstamos por período a un archivo
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return Ruta del archivo generado
     */
    public String exportarReportePrestamosEnPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        String nombreArchivo = "prestamos_periodo_" + 
                fechaInicio.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                fechaFin.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
        
        try (FileWriter escritor = new FileWriter(nombreArchivo)) {
            List<Map<String, Object>> prestamos = generador.generarReportePrestamosEnPeriodo(fechaInicio, fechaFin);
            
            escritor.write("=== REPORTE DE PRÉSTAMOS POR PERÍODO ===\n");
            escritor.write("Período: " + fechaInicio.format(FORMATO_FECHA) + 
                    " al " + fechaFin.format(FORMATO_FECHA) + "\n");
            escritor.write("Generado el: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            escritor.write("Total de préstamos en el período: " + prestamos.size() + "\n");
            escritor.write("----------------------------------\n\n");
            
            for (Map<String, Object> prestamo : prestamos) {
                escritor.write("ID: " + prestamo.get("id") + "\n");
                escritor.write("Recurso: " + prestamo.get("tituloRecurso") + 
                        " (" + prestamo.get("idRecurso") + ")\n");
                escritor.write("Categoría: " + prestamo.get("categoriaRecurso") + "\n");
                escritor.write("Usuario: " + prestamo.get("nombreUsuario") + 
                        " (" + prestamo.get("idUsuario") + ")\n");
                escritor.write("Fecha préstamo: " + prestamo.get("fechaPrestamo") + "\n");
                escritor.write("Fecha vencimiento: " + prestamo.get("fechaVencimiento") + "\n");
                escritor.write("Estado: " + prestamo.get("estado") + "\n");
                
                if (prestamo.get("estado").equals("Devuelto")) {
                    escritor.write("Fecha devolución: " + prestamo.get("fechaDevolucion") + "\n");
                    escritor.write("Duración (días): " + prestamo.get("duracionDias") + "\n");
                    escritor.write("Devuelto a tiempo: " + prestamo.get("devueltoATiempo") + "\n");
                    
                    if (!(boolean) prestamo.get("devueltoATiempo")) {
                        escritor.write("Días de atraso: " + prestamo.get("diasAtraso") + "\n");
                    }
                }
                
                escritor.write("----------------------------------\n");
            }
            
            System.out.println("Reporte de préstamos por período exportado correctamente a: " + nombreArchivo);
            return nombreArchivo;
            
        } catch (IOException e) {
            System.out.println("Error al exportar reporte: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Exporta el historial de préstamos de un usuario a un archivo
     * @param idUsuario Identificador del usuario
     * @return Ruta del archivo generado
     */
    public String exportarHistorialPrestamosPorUsuario(String idUsuario) {
        String nombreArchivo = "prestamos_usuario_" + idUsuario + "_" +
                LocalDateTime.now().format(FORMATO_FECHA_ARCHIVO) + ".txt";
        
        try (FileWriter escritor = new FileWriter(nombreArchivo)) {
            List<Map<String, Object>> prestamos = generador.generarHistorialPrestamosPorUsuario(idUsuario);
            
            if (prestamos.isEmpty()) {
                escritor.write("=== HISTORIAL DE PRÉSTAMOS DEL USUARIO ===\n");
                escritor.write("Usuario: " + idUsuario + "\n");
                escritor.write("Generado el: " + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
                escritor.write("El usuario no tiene préstamos registrados.\n");
                
                System.out.println("Historial de préstamos del usuario exportado correctamente a: " + nombreArchivo);
                return nombreArchivo;
            }
            
            // Obtener el nombre del usuario del primer préstamo
            String nombreUsuario = (String) prestamos.get(0).get("nombreUsuario");
            
            escritor.write("=== HISTORIAL DE PRÉSTAMOS DEL USUARIO ===\n");
            escritor.write("Usuario: " + nombreUsuario + " (" + idUsuario + ")\n");
            escritor.write("Generado el: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            escritor.write("Total de préstamos: " + prestamos.size() + "\n");
            escritor.write("----------------------------------\n\n");
            
            for (Map<String, Object> prestamo : prestamos) {
                escritor.write("ID: " + prestamo.get("id") + "\n");
                escritor.write("Recurso: " + prestamo.get("tituloRecurso") + 
                        " (" + prestamo.get("idRecurso") + ")\n");
                escritor.write("Categoría: " + prestamo.get("categoriaRecurso") + "\n");
                escritor.write("Fecha préstamo: " + prestamo.get("fechaPrestamo") + "\n");
                escritor.write("Fecha vencimiento: " + prestamo.get("fechaVencimiento") + "\n");
                escritor.write("Estado: " + prestamo.get("estado") + "\n");
                
                if (prestamo.get("estado").equals("Devuelto")) {
                    escritor.write("Fecha devolución: " + prestamo.get("fechaDevolucion") + "\n");
                    escritor.write("Duración (días): " + prestamo.get("duracionDias") + "\n");
                    escritor.write("Devuelto a tiempo: " + prestamo.get("devueltoATiempo") + "\n");
                    
                    if (!(boolean) prestamo.get("devueltoATiempo")) {
                        escritor.write("Días de atraso: " + prestamo.get("diasAtraso") + "\n");
                    }
                } else if ((boolean) prestamo.getOrDefault("vencido", false)) {
                    escritor.write("VENCIDO - Días de atraso: " + prestamo.get("diasAtraso") + "\n");
                }
                
                escritor.write("----------------------------------\n");
            }
            
            System.out.println("Historial de préstamos del usuario exportado correctamente a: " + nombreArchivo);
            return nombreArchivo;
            
        } catch (IOException e) {
            System.out.println("Error al exportar historial: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Exporta análisis de cumplimiento de plazos a un archivo
     * @return Ruta del archivo generado
     */
    public String exportarAnalisisCumplimientoPlazos() {
        String nombreArchivo = "analisis_cumplimiento_" + 
                LocalDateTime.now().format(FORMATO_FECHA_ARCHIVO) + ".txt";
        
        try (FileWriter escritor = new FileWriter(nombreArchivo)) {
            Map<String, Object> analisis = generador.generarAnalisisCumplimientoPlazos();
            
            escritor.write("=== ANÁLISIS DE CUMPLIMIENTO DE PLAZOS ===\n");
            escritor.write("Generado el: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
            escritor.write("Total préstamos devueltos: " + analisis.get("totalDevueltos") + "\n");
            escritor.write("Devueltos a tiempo: " + analisis.get("devueltosATiempo") + "\n");
            escritor.write("Devueltos con atraso: " + analisis.get("devueltosTarde") + "\n");
            escritor.write("Tasa de cumplimiento: " + String.format("%.2f%%", analisis.get("tasaCumplimiento")) + "\n");
            escritor.write("Promedio días de atraso: " + String.format("%.1f", analisis.get("promedioDiasAtraso")) + "\n\n");
            
            // Agregar recomendaciones basadas en las estadísticas
            double tasaCumplimiento = (double) analisis.get("tasaCumplimiento");
            double promedioDiasAtraso = (double) analisis.get("promedioDiasAtraso");
            
            escritor.write("=== RECOMENDACIONES ===\n");
            
            if (tasaCumplimiento < 70) {
                escritor.write("- La tasa de cumplimiento es baja. Se recomienda revisar las políticas de préstamos.\n");
                escritor.write("- Considerar implementar recordatorios automáticos antes de la fecha de vencimiento.\n");
            } else if (tasaCumplimiento < 90) {
                escritor.write("- La tasa de cumplimiento es aceptable, pero podría mejorar.\n");
                escritor.write("- Evaluar la posibilidad de ofrecer incentivos para devoluciones a tiempo.\n");
            } else {
                escritor.write("- La tasa de cumplimiento es muy buena.\n");
                escritor.write("- Mantener las políticas actuales de préstamos.\n");
            }
            
            if (promedioDiasAtraso > 7) {
                escritor.write("- El promedio de días de atraso es alto. Considerar ajustar las sanciones por retraso.\n");
            }
            
            System.out.println("Análisis de cumplimiento exportado correctamente a: " + nombreArchivo);
            return nombreArchivo;
            
        } catch (IOException e) {
            System.out.println("Error al exportar análisis: " + e.getMessage());
            return null;
        }
    }
} 