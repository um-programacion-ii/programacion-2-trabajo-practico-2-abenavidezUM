package sistema.biblioteca.demo;

import sistema.biblioteca.colas.ColaReservasSimple;
import sistema.biblioteca.excepciones.RecursoNoDisponibleException;
import sistema.biblioteca.excepciones.UsuarioNoEncontradoException;
import sistema.biblioteca.gestores.GestorPrestamos;
import sistema.biblioteca.gestores.GestorRecursos;
import sistema.biblioteca.gestores.GestorReglaRenovacion;
import sistema.biblioteca.gestores.GestorUsuarios;
import sistema.biblioteca.modelos.*;
import sistema.biblioteca.reportes.ReportesPrestamos;
import sistema.biblioteca.servicios.ServicioNotificaciones;
import sistema.biblioteca.servicios.ValidadorRenovaciones;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Clase para demostrar las funcionalidades del sistema de biblioteca
 * con diferentes escenarios de uso.
 */
public class DemoSistemaBiblioteca {
    
    private GestorUsuarios gestorUsuarios;
    private GestorRecursos gestorRecursos;
    private GestorPrestamos gestorPrestamos;
    private ServicioNotificaciones servicioNotificaciones;
    private ColaReservasSimple colaReservas;
    private ValidadorRenovaciones validadorRenovaciones;
    private ReportesPrestamos reportesPrestamos;
    
    private Scanner scanner;
    
    public DemoSistemaBiblioteca() {
        inicializarComponentes();
        cargarDatosDePrueba();
        scanner = new Scanner(System.in);
    }
    
    private void inicializarComponentes() {
        // Inicializar componentes del sistema
        gestorUsuarios = new GestorUsuarios();
        gestorRecursos = new GestorRecursos();
        servicioNotificaciones = new ServicioNotificaciones();
        gestorPrestamos = new GestorPrestamos(gestorRecursos, gestorUsuarios, servicioNotificaciones);
        
        // Inicializar componentes avanzados
        colaReservas = new ColaReservasSimple();
        GestorReglaRenovacion gestorReglas = gestorPrestamos.getGestorReglaRenovacion();
        validadorRenovaciones = new ValidadorRenovaciones(gestorReglas, colaReservas);
        gestorPrestamos.setValidadorRenovaciones(validadorRenovaciones);
        
        // Inicializar reportes
        reportesPrestamos = new ReportesPrestamos(gestorPrestamos);
    }
    
    private void cargarDatosDePrueba() {
        // Crear usuarios
        gestorUsuarios.registrarUsuario(new Usuario("U001", "Juan Pérez", "juan@mail.com", "555-1234"));
        gestorUsuarios.registrarUsuario(new Usuario("U002", "Ana García", "ana@mail.com", "555-5678"));
        gestorUsuarios.registrarUsuario(new Usuario("U003", "Carlos López", "carlos@mail.com", "555-9012"));
        gestorUsuarios.registrarUsuario(new Usuario("U004", "María Rodríguez", "maria@mail.com", "555-3456"));
        
        // Crear libros de diferentes categorías
        Libro libro1 = new Libro("L001", "El Principito", "Antoine de Saint-Exupéry", 
                                "9783140464079", CategoriaRecurso.LITERATURA);
        Libro libro2 = new Libro("L002", "Física Universitaria", "Sears Zemansky", 
                                "9786073221900", CategoriaRecurso.ACADEMICO);
        Libro libro3 = new Libro("L003", "Historia Argentina", "Felipe Pigna", 
                                "9789504939005", CategoriaRecurso.HISTORICO);
        Libro libro4 = new Libro("L004", "Diccionario de la RAE", "Real Academia Española", 
                                "9788423968145", CategoriaRecurso.REFERENCIA);
        Libro libro5 = new Libro("L005", "Java: Cómo Programar", "Deitel & Deitel", 
                                "9786073227391", CategoriaRecurso.ACADEMICO);
        
        // Agregar libros al gestor de recursos
        gestorRecursos.agregarRecurso(libro1);
        gestorRecursos.agregarRecurso(libro2);
        gestorRecursos.agregarRecurso(libro3);
        gestorRecursos.agregarRecurso(libro4);
        gestorRecursos.agregarRecurso(libro5);
        
        // Crear recursos multimedia
        RecursoDigital multimedia1 = new RecursoMultimedia("M001", "Curso de Python", 
                                CategoriaRecurso.MULTIMEDIA, "Video", 120);
        RecursoDigital multimedia2 = new RecursoMultimedia("M002", "Documental Historia", 
                                CategoriaRecurso.MULTIMEDIA, "Video", 90);
        
        gestorRecursos.agregarRecurso((RecursoBase)multimedia1);
        gestorRecursos.agregarRecurso((RecursoBase)multimedia2);
    }
    
    /**
     * Ejecuta la demostración interactiva
     */
    public void ejecutar() {
        System.out.println("=============================================");
        System.out.println("    DEMOSTRACIÓN DEL SISTEMA DE BIBLIOTECA");
        System.out.println("=============================================");
        
        boolean salir = false;
        
        while (!salir) {
            mostrarMenu();
            int opcion = obtenerOpcion();
            
            switch (opcion) {
                case 1: 
                    mostrarUsuarios();
                    break;
                case 2: 
                    mostrarRecursos();
                    break;
                case 3: 
                    realizarPrestamo();
                    break;
                case 4: 
                    devolverPrestamo();
                    break;
                case 5: 
                    renovarPrestamo();
                    break;
                case 6: 
                    mostrarPrestamos();
                    break;
                case 7: 
                    realizarReserva();
                    break;
                case 8: 
                    mostrarEstadisticas();
                    break;
                case 9: 
                    ejecutarEscenarioCompleto();
                    break;
                case 0: 
                    salir = true;
                    System.out.println("¡Gracias por utilizar el sistema!");
                    break;
                default: 
                    System.out.println("Opción inválida. Intente nuevamente.");
            }
            
            if (!salir) {
                System.out.println("\nPresione ENTER para continuar...");
                scanner.nextLine();
            }
        }
        
        scanner.close();
    }
    
    private void mostrarMenu() {
        System.out.println("\n--- MENÚ PRINCIPAL ---");
        System.out.println("1. Mostrar Usuarios");
        System.out.println("2. Mostrar Recursos");
        System.out.println("3. Realizar Préstamo");
        System.out.println("4. Devolver Préstamo");
        System.out.println("5. Renovar Préstamo");
        System.out.println("6. Mostrar Préstamos");
        System.out.println("7. Realizar Reserva");
        System.out.println("8. Mostrar Estadísticas");
        System.out.println("9. Ejecutar Escenario Completo (demostración automática)");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }
    
    private int obtenerOpcion() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private void mostrarUsuarios() {
        System.out.println("\n--- USUARIOS REGISTRADOS ---");
        List<Usuario> usuarios = gestorUsuarios.listarUsuarios();
        
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
            return;
        }
        
        for (Usuario usuario : usuarios) {
            System.out.println("ID: " + usuario.getId() + " | Nombre: " + usuario.getNombre() + 
                              " | Email: " + usuario.getEmail() + " | Préstamos activos: " + 
                              usuario.getCantidadPrestamos());
        }
    }
    
    private void mostrarRecursos() {
        System.out.println("\n--- RECURSOS DISPONIBLES ---");
        List<RecursoBase> recursos = gestorRecursos.listarRecursos();
        
        if (recursos.isEmpty()) {
            System.out.println("No hay recursos registrados.");
            return;
        }
        
        for (RecursoBase recurso : recursos) {
            String tipoRecurso = recurso.getClass().getSimpleName();
            String infoExtra = "";
            
            if (recurso instanceof Libro) {
                Libro libro = (Libro)recurso;
                infoExtra = " | Autor: " + libro.getAutor() + " | ISBN: " + libro.getIsbn();
            } else if (recurso instanceof RecursoMultimedia) {
                RecursoMultimedia multimedia = (RecursoMultimedia)recurso;
                infoExtra = " | Tipo: " + multimedia.getTipoMultimedia() + 
                           " | Duración: " + multimedia.getDuracionMinutos() + " min";
            }
            
            System.out.println("ID: " + recurso.getIdentificador() + 
                              " | Tipo: " + tipoRecurso +
                              " | Título: " + recurso.getTitulo() + 
                              " | Categoría: " + recurso.getCategoria() + 
                              " | Estado: " + recurso.getEstado() + 
                              infoExtra);
        }
    }
    
    private void realizarPrestamo() {
        System.out.println("\n--- REALIZAR PRÉSTAMO ---");
        
        // Mostrar usuarios disponibles
        mostrarUsuarios();
        System.out.print("\nIngrese ID del Usuario: ");
        String idUsuario = scanner.nextLine();
        
        // Verificar usuario
        Usuario usuario = gestorUsuarios.buscarUsuarioPorId(idUsuario);
        if (usuario == null) {
            System.out.println("Error: Usuario no encontrado.");
            return;
        }
        
        // Mostrar recursos disponibles
        System.out.println("\nRecursos disponibles:");
        for (RecursoBase recurso : gestorRecursos.getRecursosDisponibles()) {
            System.out.println("ID: " + recurso.getIdentificador() + 
                             " | Título: " + recurso.getTitulo() + 
                             " | Categoría: " + recurso.getCategoria());
        }
        
        System.out.print("\nIngrese ID del Recurso: ");
        String idRecurso = scanner.nextLine();
        
        // Verificar recurso
        RecursoBase recurso = gestorRecursos.buscarRecursoPorId(idRecurso);
        if (recurso == null) {
            System.out.println("Error: Recurso no encontrado.");
            return;
        }
        
        // Intentar realizar el préstamo
        try {
            Prestamo prestamo = gestorPrestamos.crearPrestamo(idRecurso, idUsuario);
            System.out.println("\n¡Préstamo realizado con éxito!");
            System.out.println("ID del Préstamo: " + prestamo.getId());
            System.out.println("Recurso: " + prestamo.getRecurso().getTitulo());
            System.out.println("Usuario: " + prestamo.getUsuario().getNombre());
            System.out.println("Fecha Préstamo: " + 
                              prestamo.getFechaPrestamo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            System.out.println("Fecha Devolución: " + 
                              prestamo.getFechaDevolucionEstimada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
        } catch (RecursoNoDisponibleException | UsuarioNoEncontradoException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void devolverPrestamo() {
        System.out.println("\n--- DEVOLVER PRÉSTAMO ---");
        
        // Mostrar préstamos activos
        List<Prestamo> prestamosActivos = gestorPrestamos.listarPrestamosActivos();
        if (prestamosActivos.isEmpty()) {
            System.out.println("No hay préstamos activos para devolver.");
            return;
        }
        
        System.out.println("Préstamos activos:");
        for (Prestamo prestamo : prestamosActivos) {
            System.out.println("ID: " + prestamo.getId() + 
                             " | Recurso: " + prestamo.getRecurso().getTitulo() + 
                             " | Usuario: " + prestamo.getUsuario().getNombre() + 
                             " | Vencimiento: " + 
                             prestamo.getFechaDevolucionEstimada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        
        System.out.print("\nIngrese ID del Préstamo a devolver: ");
        String idPrestamo = scanner.nextLine();
        
        // Verificar préstamo
        Prestamo prestamo = gestorPrestamos.buscarPrestamoPorId(idPrestamo);
        if (prestamo == null) {
            System.out.println("Error: Préstamo no encontrado.");
            return;
        }
        
        if (!prestamo.isActivo()) {
            System.out.println("Error: Este préstamo ya ha sido devuelto.");
            return;
        }
        
        // Devolver el préstamo
        gestorPrestamos.devolverPrestamo(idPrestamo);
        System.out.println("\n¡Devolución registrada con éxito!");
    }
    
    private void renovarPrestamo() {
        System.out.println("\n--- RENOVAR PRÉSTAMO ---");
        
        // Mostrar préstamos activos
        List<Prestamo> prestamosActivos = gestorPrestamos.listarPrestamosActivos();
        if (prestamosActivos.isEmpty()) {
            System.out.println("No hay préstamos activos para renovar.");
            return;
        }
        
        System.out.println("Préstamos activos:");
        for (Prestamo prestamo : prestamosActivos) {
            System.out.println("ID: " + prestamo.getId() + 
                             " | Recurso: " + prestamo.getRecurso().getTitulo() + 
                             " | Usuario: " + prestamo.getUsuario().getNombre() + 
                             " | Vencimiento: " + 
                             prestamo.getFechaDevolucionEstimada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                             " | Renovaciones: " + prestamo.getCantidadRenovaciones());
        }
        
        System.out.print("\nIngrese ID del Préstamo a renovar: ");
        String idPrestamo = scanner.nextLine();
        
        // Verificar préstamo
        Prestamo prestamo = gestorPrestamos.buscarPrestamoPorId(idPrestamo);
        if (prestamo == null) {
            System.out.println("Error: Préstamo no encontrado.");
            return;
        }
        
        System.out.print("Ingrese motivo de la renovación: ");
        String motivo = scanner.nextLine();
        
        // Calcular días según categoría o usar valor por defecto
        RecursoBase recurso = prestamo.getRecurso();
        int diasSugeridos = gestorPrestamos.getGestorReglaRenovacion().getDiasParaRenovacion(recurso);
        
        System.out.println("Días sugeridos según categoría: " + diasSugeridos);
        System.out.print("Ingrese días de extensión (ENTER para usar sugerido): ");
        String diasStr = scanner.nextLine();
        
        int dias = diasSugeridos;
        if (!diasStr.trim().isEmpty()) {
            try {
                dias = Integer.parseInt(diasStr);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Se usará el valor sugerido: " + diasSugeridos);
            }
        }
        
        // Intentar renovar
        try {
            gestorPrestamos.renovarPrestamo(idPrestamo, dias, motivo);
            
            System.out.println("\n¡Préstamo renovado con éxito!");
            System.out.println("Nueva fecha de devolución: " + 
                             prestamo.getFechaDevolucionEstimada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            System.out.println("Cantidad total de renovaciones: " + prestamo.getCantidadRenovaciones());
            
        } catch (IllegalArgumentException e) {
            System.out.println("Error al renovar: " + e.getMessage());
        }
    }
    
    private void mostrarPrestamos() {
        System.out.println("\n--- INFORMACIÓN DE PRÉSTAMOS ---");
        System.out.println("1. Todos los préstamos");
        System.out.println("2. Préstamos activos");
        System.out.println("3. Préstamos vencidos");
        System.out.println("4. Préstamos por usuario");
        System.out.println("5. Préstamos renovados");
        System.out.print("Seleccione una opción: ");
        
        int opcion = obtenerOpcion();
        List<Prestamo> prestamos = null;
        
        switch (opcion) {
            case 1:
                prestamos = gestorPrestamos.listarTodosLosPrestamos();
                System.out.println("\n--- TODOS LOS PRÉSTAMOS ---");
                break;
            case 2:
                prestamos = gestorPrestamos.listarPrestamosActivos();
                System.out.println("\n--- PRÉSTAMOS ACTIVOS ---");
                break;
            case 3:
                prestamos = gestorPrestamos.listarPrestamosVencidos();
                System.out.println("\n--- PRÉSTAMOS VENCIDOS ---");
                break;
            case 4:
                mostrarUsuarios();
                System.out.print("\nIngrese ID del Usuario: ");
                String idUsuario = scanner.nextLine();
                prestamos = gestorPrestamos.buscarPrestamosPorUsuario(idUsuario);
                System.out.println("\n--- PRÉSTAMOS DEL USUARIO " + idUsuario + " ---");
                break;
            case 5:
                prestamos = gestorPrestamos.listarPrestamosRenovados();
                System.out.println("\n--- PRÉSTAMOS RENOVADOS ---");
                break;
            default:
                System.out.println("Opción inválida.");
                return;
        }
        
        if (prestamos.isEmpty()) {
            System.out.println("No hay préstamos para mostrar.");
            return;
        }
        
        for (Prestamo prestamo : prestamos) {
            String estado = prestamo.isActivo() ? "Activo" : "Devuelto";
            String vencido = prestamo.isActivo() && prestamo.estaVencido() ? " (VENCIDO)" : "";
            
            System.out.println("\nID: " + prestamo.getId());
            System.out.println("Recurso: " + prestamo.getRecurso().getTitulo());
            System.out.println("Usuario: " + prestamo.getUsuario().getNombre());
            System.out.println("Fecha Préstamo: " + 
                              prestamo.getFechaPrestamo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            System.out.println("Fecha Devolución Estimada: " + 
                              prestamo.getFechaDevolucionEstimada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            if (prestamo.getFechaDevolucionReal() != null) {
                System.out.println("Fecha Devolución Real: " + 
                                 prestamo.getFechaDevolucionReal().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            
            System.out.println("Estado: " + estado + vencido);
            System.out.println("Renovaciones: " + prestamo.getCantidadRenovaciones());
            
            if (prestamo.getCantidadRenovaciones() > 0) {
                System.out.println("Historial de renovaciones:");
                List<HistorialRenovacion> historial = prestamo.getHistorialRenovaciones();
                for (int i = 0; i < historial.size(); i++) {
                    HistorialRenovacion renovacion = historial.get(i);
                    System.out.println("  " + (i+1) + ". Fecha: " + 
                                    renovacion.getFechaRenovacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                                    " | Días: " + renovacion.getDiasExtendidos() + 
                                    " | Motivo: " + renovacion.getMotivo());
                }
            }
            
            System.out.println("------------------------------");
        }
    }
    
    private void realizarReserva() {
        System.out.println("\n--- REALIZAR RESERVA ---");
        
        // Mostrar usuarios disponibles
        mostrarUsuarios();
        System.out.print("\nIngrese ID del Usuario: ");
        String idUsuario = scanner.nextLine();
        
        // Verificar usuario
        Usuario usuario = gestorUsuarios.buscarUsuarioPorId(idUsuario);
        if (usuario == null) {
            System.out.println("Error: Usuario no encontrado.");
            return;
        }
        
        // Mostrar todos los recursos (incluso los no disponibles)
        System.out.println("\nRecursos para reservar:");
        for (RecursoBase recurso : gestorRecursos.listarRecursos()) {
            System.out.println("ID: " + recurso.getIdentificador() + 
                             " | Título: " + recurso.getTitulo() + 
                             " | Estado: " + recurso.getEstado());
        }
        
        System.out.print("\nIngrese ID del Recurso a reservar: ");
        String idRecurso = scanner.nextLine();
        
        // Verificar recurso
        RecursoBase recurso = gestorRecursos.buscarRecursoPorId(idRecurso);
        if (recurso == null) {
            System.out.println("Error: Recurso no encontrado.");
            return;
        }
        
        if (recurso.estaDisponible()) {
            System.out.println("Este recurso está disponible. Puede realizar un préstamo directamente.");
            return;
        }
        
        // Realizar la reserva
        colaReservas.agregarReserva(idRecurso, idUsuario);
        
        System.out.println("\n¡Reserva registrada con éxito!");
        System.out.println("Usuario: " + usuario.getNombre());
        System.out.println("Recurso: " + recurso.getTitulo());
        System.out.println("Posición en la cola: " + colaReservas.getCantidadReservas(idRecurso));
    }
    
    private void mostrarEstadisticas() {
        System.out.println("\n--- ESTADÍSTICAS DEL SISTEMA ---");
        
        // Estadísticas básicas
        System.out.println("Total de préstamos: " + gestorPrestamos.getCantidadPrestamosTotales());
        System.out.println("Préstamos activos: " + gestorPrestamos.getCantidadPrestamosActivos());
        System.out.println("Préstamos vencidos: " + gestorPrestamos.getCantidadPrestamosVencidos());
        
        // Recursos por categoría
        System.out.println("\nRecursos por categoría:");
        Map<CategoriaRecurso, Integer> recursosPorCategoria = gestorRecursos.contarRecursosPorCategoria();
        for (Map.Entry<CategoriaRecurso, Integer> entry : recursosPorCategoria.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        
        // Estadísticas de renovaciones
        System.out.println("\nEstadísticas de renovaciones:");
        Map<String, Object> estadisticasRenovaciones = reportesPrestamos.generarEstadisticasRenovaciones();
        System.out.println("Total préstamos renovados: " + estadisticasRenovaciones.get("prestamosRenovados"));
        System.out.println("Total renovaciones: " + estadisticasRenovaciones.get("totalRenovaciones"));
        System.out.println("Tasa de renovación: " + String.format("%.2f%%", estadisticasRenovaciones.get("tasaRenovacion")));
        System.out.println("Promedio renovaciones por préstamo: " + 
                         String.format("%.2f", estadisticasRenovaciones.get("promedioRenovacionesPorPrestamo")));
        System.out.println("Tipo recurso más renovado: " + estadisticasRenovaciones.get("tipoRecursoMasRenovado"));
        
        // Préstamos por período
        System.out.println("\nPréstamos del mes actual:");
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = LocalDate.now();
        List<Map<String, Object>> prestamosDelMes = reportesPrestamos.generarReportePrestamosEnPeriodo(inicio, fin);
        System.out.println("Total: " + prestamosDelMes.size());
    }
    
    /**
     * Ejecuta un escenario completo como demostración automática
     */
    private void ejecutarEscenarioCompleto() {
        System.out.println("\n--- ESCENARIO COMPLETO DE DEMOSTRACIÓN ---");
        
        try {
            // 1. Realizar varios préstamos
            System.out.println("\n1. Realizando préstamos...");
            Prestamo p1 = gestorPrestamos.crearPrestamo("L001", "U001");
            Prestamo p2 = gestorPrestamos.crearPrestamo("L002", "U002");
            Prestamo p3 = gestorPrestamos.crearPrestamo("M001", "U003");
            System.out.println("Préstamos creados: " + p1.getId() + ", " + p2.getId() + ", " + p3.getId());
            
            // 2. Intentar prestar un recurso no disponible
            System.out.println("\n2. Intentando prestar un recurso no disponible...");
            try {
                gestorPrestamos.crearPrestamo("L001", "U004");
            } catch (RecursoNoDisponibleException e) {
                System.out.println("Error esperado: " + e.getMessage());
            }
            
            // 3. Crear una reserva
            System.out.println("\n3. Creando reserva para un recurso no disponible...");
            colaReservas.agregarReserva("L001", "U004");
            System.out.println("Reserva creada para el recurso L001 por el usuario U004");
            
            // 4. Renovar un préstamo
            System.out.println("\n4. Renovando préstamo...");
            try {
                gestorPrestamos.renovarPrestamo(p2.getId(), 7, "Necesito más tiempo para leer");
                System.out.println("Préstamo renovado correctamente: " + p2.getId());
                System.out.println("Nueva fecha de devolución: " + 
                                 p2.getFechaDevolucionEstimada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } catch (IllegalArgumentException e) {
                System.out.println("Error al renovar: " + e.getMessage());
            }
            
            // 5. Intentar renovar un préstamo con reservas pendientes
            System.out.println("\n5. Intentando renovar un préstamo con reservas pendientes...");
            try {
                gestorPrestamos.renovarPrestamo(p1.getId(), 7, "Necesito más tiempo");
            } catch (IllegalArgumentException e) {
                System.out.println("Error esperado: " + e.getMessage());
            }
            
            // 6. Devolver un préstamo
            System.out.println("\n6. Devolviendo préstamo...");
            gestorPrestamos.devolverPrestamo(p3.getId());
            System.out.println("Préstamo devuelto: " + p3.getId());
            
            // 7. Generar estadísticas
            System.out.println("\n7. Generando estadísticas...");
            System.out.println("Total préstamos: " + gestorPrestamos.getCantidadPrestamosTotales());
            System.out.println("Préstamos activos: " + gestorPrestamos.getCantidadPrestamosActivos());
            System.out.println("Préstamos renovados: " + gestorPrestamos.listarPrestamosRenovados().size());
            
            // 8. Generar reportes
            System.out.println("\n8. Generando reportes...");
            Map<String, Object> estadisticasRenovaciones = reportesPrestamos.generarEstadisticasRenovaciones();
            System.out.println("Tasa de renovación: " + 
                             String.format("%.2f%%", estadisticasRenovaciones.get("tasaRenovacion")));
            
            System.out.println("\nEscenario completo ejecutado con éxito.");
            
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Método principal para ejecutar la demo
    public static void main(String[] args) {
        DemoSistemaBiblioteca demo = new DemoSistemaBiblioteca();
        demo.ejecutar();
    }
} 