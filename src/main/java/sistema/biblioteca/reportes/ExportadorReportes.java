package sistema.biblioteca.reportes;

import sistema.biblioteca.modelos.CategoriaRecurso;
import sistema.biblioteca.modelos.Prestamo;
import sistema.biblioteca.modelos.RecursoBase;
import sistema.biblioteca.modelos.Usuario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Clase para exportar y mostrar los reportes generados
 */
public class ExportadorReportes {
    
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private final GeneradorReportes generador;
    
    public ExportadorReportes(GeneradorReportes generador) {
        this.generador = generador;
    }
    
    /**
     * Muestra por consola las estadísticas generales de préstamos
     */
    public void mostrarEstadisticasPrestamos() {
        Map<String, Object> estadisticas = generador.generarEstadisticasPrestamos();
        
        System.out.println("=== ESTADÍSTICAS DE PRÉSTAMOS ===");
        System.out.println("Total de préstamos: " + estadisticas.get("totalPrestamos"));
        System.out.println("Préstamos activos: " + estadisticas.get("prestamosActivos"));
        System.out.println("Préstamos devueltos: " + estadisticas.get("prestamosDevueltos"));
        System.out.println("Préstamos atrasados: " + estadisticas.get("prestamosAtrasados"));
        System.out.println("Promedio de días en préstamo: " + estadisticas.get("promedioDiasPrestamo"));
        System.out.println("===================================");
    }
    
    /**
     * Muestra por consola los recursos más prestados
     * @param limite Cantidad máxima de recursos a mostrar
     */
    public void mostrarRecursosMasPrestados(int limite) {
        List<Map<String, Object>> recursos = generador.listarRecursosMasPrestados(limite);
        
        System.out.println("=== RECURSOS MÁS PRESTADOS ===");
        System.out.println("Top " + limite + " recursos:");
        
        int contador = 1;
        for (Map<String, Object> item : recursos) {
            RecursoBase recurso = (RecursoBase) item.get("recurso");
            Integer cantidad = (Integer) item.get("cantidadPrestamos");
            
            System.out.println(contador + ". " + recurso.getTitulo() + 
                    " (ID: " + recurso.getIdentificador() + ") - " + 
                    cantidad + " préstamos");
            contador++;
        }
        System.out.println("===================================");
    }
    
    /**
     * Muestra por consola los usuarios más activos
     * @param limite Cantidad máxima de usuarios a mostrar
     */
    public void mostrarUsuariosMasActivos(int limite) {
        List<Map<String, Object>> usuarios = generador.listarUsuariosMasActivos(limite);
        
        System.out.println("=== USUARIOS MÁS ACTIVOS ===");
        System.out.println("Top " + limite + " usuarios:");
        
        int contador = 1;
        for (Map<String, Object> item : usuarios) {
            Usuario usuario = (Usuario) item.get("usuario");
            Integer cantidad = (Integer) item.get("cantidadPrestamos");
            
            System.out.println(contador + ". " + usuario.getNombre() + 
                    " (ID: " + usuario.getId() + ") - " + 
                    cantidad + " préstamos");
            contador++;
        }
        System.out.println("===================================");
    }
    
    /**
     * Muestra por consola la distribución de recursos por categoría
     */
    public void mostrarDistribucionPorCategoria() {
        Map<CategoriaRecurso, Integer> distribucion = generador.obtenerDistribucionPorCategoria();
        
        System.out.println("=== DISTRIBUCIÓN POR CATEGORÍA ===");
        distribucion.forEach((categoria, cantidad) -> 
            System.out.println(categoria + ": " + cantidad + " recursos")
        );
        System.out.println("===================================");
    }
    
    /**
     * Exporta estadísticas generales a un archivo de texto
     * @return Ruta del archivo generado
     */
    public String exportarEstadisticasAArchivo() {
        String nombreArchivo = "estadisticas_" + 
                LocalDateTime.now().format(FORMATO_FECHA) + ".txt";
        
        try (FileWriter escritor = new FileWriter(nombreArchivo)) {
            // Estadísticas de préstamos
            Map<String, Object> estadisticas = generador.generarEstadisticasPrestamos();
            escritor.write("=== ESTADÍSTICAS DE PRÉSTAMOS ===\n");
            escritor.write("Total de préstamos: " + estadisticas.get("totalPrestamos") + "\n");
            escritor.write("Préstamos activos: " + estadisticas.get("prestamosActivos") + "\n");
            escritor.write("Préstamos devueltos: " + estadisticas.get("prestamosDevueltos") + "\n");
            escritor.write("Préstamos atrasados: " + estadisticas.get("prestamosAtrasados") + "\n");
            escritor.write("Promedio de días en préstamo: " + estadisticas.get("promedioDiasPrestamo") + "\n\n");
            
            // Recursos más prestados
            List<Map<String, Object>> recursos = generador.listarRecursosMasPrestados(5);
            escritor.write("=== RECURSOS MÁS PRESTADOS ===\n");
            int contador = 1;
            for (Map<String, Object> item : recursos) {
                RecursoBase recurso = (RecursoBase) item.get("recurso");
                Integer cantidad = (Integer) item.get("cantidadPrestamos");
                
                escritor.write(contador + ". " + recurso.getTitulo() + 
                        " (ID: " + recurso.getIdentificador() + ") - " + 
                        cantidad + " préstamos\n");
                contador++;
            }
            escritor.write("\n");
            
            // Distribución por categoría
            Map<CategoriaRecurso, Integer> distribucion = generador.obtenerDistribucionPorCategoria();
            escritor.write("=== DISTRIBUCIÓN POR CATEGORÍA ===\n");
            for (Map.Entry<CategoriaRecurso, Integer> entry : distribucion.entrySet()) {
                escritor.write(entry.getKey() + ": " + entry.getValue() + " recursos\n");
            }
            
            System.out.println("Estadísticas exportadas correctamente a: " + nombreArchivo);
            return nombreArchivo;
            
        } catch (IOException e) {
            System.out.println("Error al exportar estadísticas: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Exporta un reporte específico para préstamos atrasados
     * @return Ruta del archivo generado
     */
    public String exportarReportePrestamosAtrasados(List<Prestamo> prestamosAtrasados) {
        String nombreArchivo = "prestamos_atrasados_" + 
                LocalDateTime.now().format(FORMATO_FECHA) + ".txt";
        
        try (FileWriter escritor = new FileWriter(nombreArchivo)) {
            escritor.write("=== REPORTE DE PRÉSTAMOS ATRASADOS ===\n");
            escritor.write("Generado el: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
            
            if (prestamosAtrasados.isEmpty()) {
                escritor.write("No hay préstamos atrasados actualmente.\n");
            } else {
                escritor.write("Total de préstamos atrasados: " + prestamosAtrasados.size() + "\n\n");
                
                for (Prestamo prestamo : prestamosAtrasados) {
                    escritor.write("ID Préstamo: " + prestamo.getId() + "\n");
                    escritor.write("Recurso: " + prestamo.getRecurso().getTitulo() + 
                            " (ID: " + prestamo.getRecurso().getIdentificador() + ")\n");
                    escritor.write("Usuario: " + prestamo.getUsuario().getNombre() + 
                            " (ID: " + prestamo.getUsuario().getId() + ")\n");
                    escritor.write("Fecha préstamo: " + prestamo.getFechaPrestamo() + "\n");
                    escritor.write("Fecha devolución estimada: " + prestamo.getFechaDevolucionEstimada() + "\n");
                    escritor.write("Días de atraso: " + 
                            java.time.Duration.between(
                                prestamo.getFechaDevolucionEstimada(), 
                                LocalDateTime.now()).toDays() + "\n");
                    escritor.write("------------------------------------\n");
                }
            }
            
            System.out.println("Reporte de préstamos atrasados exportado correctamente a: " + nombreArchivo);
            return nombreArchivo;
            
        } catch (IOException e) {
            System.out.println("Error al exportar reporte: " + e.getMessage());
            return null;
        }
    }
} 