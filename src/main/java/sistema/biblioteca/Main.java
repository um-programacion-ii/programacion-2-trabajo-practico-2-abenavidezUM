package sistema.biblioteca;

import sistema.biblioteca.excepciones.RecursoNoDisponibleException;
import sistema.biblioteca.excepciones.UsuarioNoEncontradoException;
import sistema.biblioteca.gestores.GestorPrestamos;
import sistema.biblioteca.gestores.GestorRecursos;
import sistema.biblioteca.gestores.GestorUsuarios;
import sistema.biblioteca.modelos.*;
import sistema.biblioteca.servicios.ServicioNotificacionesEmail;
import sistema.biblioteca.servicios.ServicioNotificacionesSMS;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        System.out.println("Iniciando Sistema de Gestión de Biblioteca Digital");
        
        // Crear gestores
        GestorUsuarios gestorUsuarios = new GestorUsuarios();
        GestorRecursos gestorRecursos = new GestorRecursos();
        
        // Crear servicios de notificaciones
        ServicioNotificacionesEmail servicioEmail = new ServicioNotificacionesEmail();
        ServicioNotificacionesSMS servicioSMS = new ServicioNotificacionesSMS();
        
        // Crear gestor de préstamos
        GestorPrestamos gestorPrestamos = new GestorPrestamos(gestorRecursos, gestorUsuarios, servicioEmail);
        
        // Crear algunos datos de ejemplo
        crearDatosEjemplo(gestorUsuarios, gestorRecursos);
        
        // Mostrar usuarios y recursos
        mostrarInformacionSistema(gestorUsuarios, gestorRecursos);
        
        // Realizar una búsqueda de ejemplo
        System.out.println("\n--- Búsqueda de recursos por título 'Quijote' ---");
        for (RecursoBase recurso : gestorRecursos.buscarRecursosPorTitulo("Quijote")) {
            System.out.println(recurso);
        }
        
        System.out.println("\n--- Búsqueda de usuarios por nombre 'García' ---");
        for (Usuario usuario : gestorUsuarios.buscarUsuariosPorNombre("García")) {
            System.out.println(usuario);
            // Enviar una notificación de prueba
            servicioEmail.enviarNotificacion(usuario, "Bienvenido al sistema de biblioteca digital!");
        }
        
        // Realizar operaciones de préstamo
        try {
            realizarOperacionesPrestamo(gestorPrestamos);
        } catch (Exception e) {
            System.out.println("Error al realizar operaciones de préstamo: " + e.getMessage());
        }
        
        // Mostrar ejemplo de reservas
        mostrarEjemploReservas(gestorRecursos, gestorUsuarios);
        
        System.out.println("\nSistema finalizado.");
    }
    
    private static void crearDatosEjemplo(GestorUsuarios gestorUsuarios, GestorRecursos gestorRecursos) {
        // Crear algunos usuarios
        Usuario u1 = new Usuario("U001", "Ana García", "ana@ejemplo.com", "555-1234");
        Usuario u2 = new Usuario("U002", "Carlos López", "carlos@ejemplo.com", "555-5678");
        Usuario u3 = new Usuario("U003", "Marta Rodríguez", "marta@ejemplo.com");
        
        gestorUsuarios.registrarUsuario(u1);
        gestorUsuarios.registrarUsuario(u2);
        gestorUsuarios.registrarUsuario(u3);
        
        // Crear algunos recursos
        Libro libro1 = new Libro("L001", "Cien años de soledad", "Gabriel García Márquez", 
                "9780307474728", 432, 1967, CategoriaRecurso.FICCION);
        
        Libro libro2 = new Libro("L002", "Patrones de Diseño", "Erich Gamma et al.", 
                "9780201633610", 395, 1994, CategoriaRecurso.ACADEMICO);
        
        Revista revista1 = new Revista("R001", "National Geographic", "NG Press", 
                256, 5, 2023, CategoriaRecurso.REVISTA);
        
        Audiolibro audio1 = new Audiolibro("A001", "El Quijote", "Miguel de Cervantes", 
                "Juan Narrador", 950, "MP3", CategoriaRecurso.AUDIOLIBRO);
        
        gestorRecursos.agregarRecurso(libro1);
        gestorRecursos.agregarRecurso(libro2);
        gestorRecursos.agregarRecurso(revista1);
        gestorRecursos.agregarRecurso(audio1);
        
        System.out.println("Datos de ejemplo creados: " + gestorUsuarios.getCantidadUsuarios() + 
                " usuarios y " + gestorRecursos.getCantidadTotalRecursos() + " recursos.");
    }
    
    private static void mostrarInformacionSistema(GestorUsuarios gestorUsuarios, GestorRecursos gestorRecursos) {
        System.out.println("\n--- RECURSOS DISPONIBLES ---");
        for (RecursoBase recurso : gestorRecursos.getRecursosDisponibles()) {
            System.out.println(recurso);
        }
        
        System.out.println("\n--- USUARIOS REGISTRADOS ---");
        for (Usuario usuario : gestorUsuarios.listarUsuarios()) {
            System.out.println(usuario);
        }
        
        System.out.println("\n--- ESTADÍSTICAS POR CATEGORÍA ---");
        gestorRecursos.contarRecursosPorCategoria().forEach((categoria, cantidad) -> 
            System.out.println(categoria + ": " + cantidad + " recursos")
        );
    }
    
    private static void realizarOperacionesPrestamo(GestorPrestamos gestorPrestamos) 
            throws RecursoNoDisponibleException, UsuarioNoEncontradoException {
        
        System.out.println("\n=== OPERACIONES DE PRÉSTAMO ===");
        
        // Realizar un préstamo
        Prestamo prestamo1 = gestorPrestamos.crearPrestamo("L001", "U001");
        System.out.println("Préstamo creado: " + prestamo1);
        System.out.println("Fecha devolución: " + 
                formatearFecha(prestamo1.getFechaDevolucionEstimada()));
        
        // Realizar otro préstamo
        Prestamo prestamo2 = gestorPrestamos.crearPrestamo("R001", "U002");
        System.out.println("Préstamo creado: " + prestamo2);
        
        // Verificar préstamos activos
        System.out.println("\n--- PRÉSTAMOS ACTIVOS ---");
        for (Prestamo p : gestorPrestamos.listarPrestamosActivos()) {
            System.out.println(p);
        }
        
        // Devolver un préstamo
        System.out.println("\n--- DEVOLUCIÓN DE PRÉSTAMO ---");
        gestorPrestamos.devolverPrestamo(prestamo1.getId());
        System.out.println("Préstamo devuelto: " + prestamo1.getId());
        
        // Mostrar préstamos activos después de la devolución
        System.out.println("\n--- PRÉSTAMOS ACTIVOS DESPUÉS DE DEVOLUCIÓN ---");
        List<Prestamo> prestamosActivos = gestorPrestamos.listarPrestamosActivos();
        System.out.println("Cantidad de préstamos activos: " + prestamosActivos.size());
        for (Prestamo p : prestamosActivos) {
            System.out.println(p);
        }
        
        // Intentar prestar un recurso no disponible (debería fallar)
        try {
            gestorPrestamos.crearPrestamo("R001", "U003"); // R001 ya está prestado a U002
            System.out.println("Este código no debería ejecutarse");
        } catch (RecursoNoDisponibleException e) {
            System.out.println("\nError esperado al intentar prestar un recurso no disponible: " + e.getMessage());
        }
    }
    
    private static void mostrarEjemploReservas(GestorRecursos gestorRecursos, GestorUsuarios gestorUsuarios) {
        System.out.println("\n=== EJEMPLO DE RESERVAS ===");
        
        try {
            // Obtener recursos y usuarios
            RecursoBase recurso = gestorRecursos.buscarRecursoPorId("L002");
            Usuario usuario = gestorUsuarios.buscarUsuarioPorId("U003");
            
            if (recurso != null && usuario != null) {
                // Crear una reserva básica
                String idReserva = "R-" + UUID.randomUUID().toString().substring(0, 8);
                Reserva reserva = new Reserva(idReserva, recurso, usuario);
                
                System.out.println("Reserva creada: " + reserva);
                System.out.println("Estado: " + reserva.getEstado());
                System.out.println("Fecha de expiración: " + formatearFecha(reserva.getFechaExpiracion()));
                System.out.println("Días hasta expiración: " + reserva.diasHastaExpiracion());
                
                // Extender la reserva
                reserva.extenderExpiracion(5);
                System.out.println("\nReserva extendida 5 días:");
                System.out.println("Nueva fecha de expiración: " + formatearFecha(reserva.getFechaExpiracion()));
                System.out.println("Días hasta expiración: " + reserva.diasHastaExpiracion());
                
                // Cancelar la reserva
                reserva.cancelar();
                System.out.println("\nReserva cancelada");
                System.out.println("Estado actual: " + reserva.getEstado());
                System.out.println("¿Está pendiente? " + reserva.estaPendiente());
                System.out.println("¿Está cancelada? " + reserva.estaCancelada());
                
                // Crear otra reserva con días personalizados
                String idReserva2 = "R-" + UUID.randomUUID().toString().substring(0, 8);
                Reserva reserva2 = new Reserva(idReserva2, recurso, usuario, 7);
                
                System.out.println("\nNueva reserva con 7 días de expiración:");
                System.out.println(reserva2);
                System.out.println("Fecha de expiración: " + formatearFecha(reserva2.getFechaExpiracion()));
            }
        } catch (Exception e) {
            System.out.println("Error al realizar ejemplo de reservas: " + e.getMessage());
        }
    }
    
    private static String formatearFecha(java.time.LocalDateTime fecha) {
        if (fecha == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return fecha.format(formatter);
    }
}
