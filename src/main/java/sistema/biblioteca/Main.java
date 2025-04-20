package sistema.biblioteca;

import sistema.biblioteca.gestores.GestorRecursos;
import sistema.biblioteca.gestores.GestorUsuarios;
import sistema.biblioteca.modelos.*;
import sistema.biblioteca.servicios.ServicioNotificacionesEmail;
import sistema.biblioteca.servicios.ServicioNotificacionesSMS;

public class Main {

    public static void main(String[] args) {
        System.out.println("Iniciando Sistema de Gestión de Biblioteca Digital");
        
        // Crear gestores
        GestorUsuarios gestorUsuarios = new GestorUsuarios();
        GestorRecursos gestorRecursos = new GestorRecursos();
        
        // Crear servicios de notificaciones
        ServicioNotificacionesEmail servicioEmail = new ServicioNotificacionesEmail();
        ServicioNotificacionesSMS servicioSMS = new ServicioNotificacionesSMS();
        
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
}
