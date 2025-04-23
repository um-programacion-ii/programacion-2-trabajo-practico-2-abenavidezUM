package sistema.biblioteca.reportes;

import sistema.biblioteca.gestores.GestorPrestamos;
import sistema.biblioteca.gestores.GestorRecursos;
import sistema.biblioteca.gestores.GestorUsuarios;
import sistema.biblioteca.modelos.CategoriaRecurso;
import sistema.biblioteca.modelos.Libro;
import sistema.biblioteca.modelos.Prestamo;
import sistema.biblioteca.modelos.Usuario;
import sistema.biblioteca.servicios.ServicioNotificacionesImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase de demostración para los reportes específicos de préstamos
 */
public class DemoReportesPrestamos {
    
    /**
     * Método principal para ejecutar la demostración
     */
    public static void main(String[] args) {
        System.out.println("=== DEMOSTRACIÓN DE REPORTES ESPECÍFICOS DE PRÉSTAMOS ===\n");
        
        // Crear gestores necesarios
        GestorUsuarios gestorUsuarios = new GestorUsuarios();
        GestorRecursos gestorRecursos = new GestorRecursos();
        ServicioNotificacionesImpl servicioNotificaciones = new ServicioNotificacionesImpl(gestorUsuarios);
        GestorPrestamos gestorPrestamos = new GestorPrestamos(gestorRecursos, gestorUsuarios, servicioNotificaciones);
        
        // Crear generadores de reportes
        ReportesPrestamos reportesPrestamos = new ReportesPrestamos(gestorPrestamos);
        ExportadorReportesPrestamos exportador = new ExportadorReportesPrestamos(reportesPrestamos);
        
        // Crear datos de ejemplo
        crearDatosEjemploPrestamos(gestorUsuarios, gestorRecursos, gestorPrestamos);
        
        // Mostrar reportes en consola
        System.out.println("\n----- REPORTES EN CONSOLA -----\n");
        
        // Reporte de préstamos activos
        exportador.mostrarReportePrestamosActivos();
        System.out.println();
        
        // Reporte de préstamos vencidos
        exportador.mostrarReportePrestamosVencidos();
        System.out.println();
        
        // Análisis de cumplimiento de plazos
        exportador.mostrarAnalisisCumplimientoPlazos();
        System.out.println();
        
        // Estadísticas de duración de préstamos por categoría
        exportador.mostrarEstadisticasDuracionPrestamos();
        System.out.println();
        
        // Exportar reportes a archivos
        System.out.println("\n----- EXPORTACIÓN DE REPORTES A ARCHIVOS -----\n");
        
        // Exportar reporte de préstamos activos
        exportador.exportarReportePrestamosActivos();
        
        // Exportar análisis de cumplimiento
        exportador.exportarAnalisisCumplimientoPlazos();
        
        // Exportar historial de préstamos de un usuario
        exportador.exportarHistorialPrestamosPorUsuario("U001");
        
        // Exportar reporte de préstamos por período
        LocalDate inicio = LocalDate.now().minusMonths(6);
        LocalDate fin = LocalDate.now();
        exportador.exportarReportePrestamosEnPeriodo(inicio, fin);
        
        System.out.println("\n=== FIN DE LA DEMOSTRACIÓN ===");
    }
    
    /**
     * Crea datos de ejemplo para la demostración
     */
    private static void crearDatosEjemploPrestamos(GestorUsuarios gestorUsuarios, GestorRecursos gestorRecursos, 
                                               GestorPrestamos gestorPrestamos) {
        try {
            // Crear usuarios
            Usuario u1 = new Usuario("U001", "Ana García", "ana@ejemplo.com", "555-1234");
            Usuario u2 = new Usuario("U002", "Carlos López", "carlos@ejemplo.com", "555-5678");
            Usuario u3 = new Usuario("U003", "Marta Rodríguez", "marta@ejemplo.com", "555-9012");
            
            gestorUsuarios.registrarUsuario(u1);
            gestorUsuarios.registrarUsuario(u2);
            gestorUsuarios.registrarUsuario(u3);
            
            // Crear recursos
            Libro libro1 = new Libro("L001", "Cien años de soledad", "Gabriel García Márquez", 
                    "9780307474728", 432, 1967, CategoriaRecurso.FICCION);
            
            Libro libro2 = new Libro("L002", "Patrones de Diseño", "Erich Gamma et al.", 
                    "9780201633610", 395, 1994, CategoriaRecurso.ACADEMICO);
            
            Libro libro3 = new Libro("L003", "El Quijote", "Miguel de Cervantes", 
                    "9788420412146", 863, 1605, CategoriaRecurso.FICCION);
            
            Libro libro4 = new Libro("L004", "Python para Todos", "Raúl González", 
                    "9788441538114", 320, 2019, CategoriaRecurso.INFORMATICA);
            
            Libro libro5 = new Libro("L005", "Historia del Arte", "Ernst Gombrich", 
                    "9780714898704", 688, 1950, CategoriaRecurso.ACADEMICO);
            
            gestorRecursos.agregarRecurso(libro1);
            gestorRecursos.agregarRecurso(libro2);
            gestorRecursos.agregarRecurso(libro3);
            gestorRecursos.agregarRecurso(libro4);
            gestorRecursos.agregarRecurso(libro5);
            
            // Crear préstamos
            // 1. Préstamo activo a tiempo (Ana)
            Prestamo p1 = gestorPrestamos.crearPrestamo("L001", "U001");
            
            // 2. Préstamo activo vencido (Carlos)
            Prestamo p2 = gestorPrestamos.crearPrestamo("L002", "U002");
            // Manipular la fecha para que esté vencido
            LocalDateTime fechaVencidaP2 = LocalDateTime.now().minusDays(5);
            p2.setFechaDevolucionEstimada(fechaVencidaP2);
            
            // 3. Préstamo devuelto a tiempo (Ana)
            Prestamo p3 = gestorPrestamos.crearPrestamo("L003", "U001");
            // Devolver el préstamo antes del vencimiento
            p3.registrarDevolucion();
            
            // 4. Préstamo devuelto con atraso (Marta)
            Prestamo p4 = gestorPrestamos.crearPrestamo("L004", "U003");
            // Manipular las fechas para que esté devuelto con atraso
            LocalDateTime fechaVencidaP4 = LocalDateTime.now().minusDays(10);
            p4.setFechaDevolucionEstimada(fechaVencidaP4);
            // Devolver con atraso de 2 días
            p4.registrarDevolucion();
            p4.setFechaDevolucionReal(fechaVencidaP4.plusDays(2));
            
            // 5. Préstamo activo vencido con mucho atraso (Ana)
            Prestamo p5 = gestorPrestamos.crearPrestamo("L005", "U001");
            // Manipular la fecha para que esté muy vencido
            LocalDateTime fechaMuyVencidaP5 = LocalDateTime.now().minusDays(30);
            p5.setFechaDevolucionEstimada(fechaMuyVencidaP5);
            
            System.out.println("Datos de ejemplo creados: " + gestorUsuarios.getCantidadUsuarios() + 
                    " usuarios, " + gestorRecursos.getCantidadTotalRecursos() + " recursos y " + 
                    gestorPrestamos.getCantidadPrestamosTotales() + " préstamos.");
        } catch (Exception e) {
            System.out.println("Error al crear datos de ejemplo: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 