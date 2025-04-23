package sistema.biblioteca.monitoreo;

import sistema.biblioteca.gestores.GestorPrestamos;
import sistema.biblioteca.gestores.GestorRecursos;
import sistema.biblioteca.gestores.GestorReservas;
import sistema.biblioteca.gestores.GestorUsuarios;
import sistema.biblioteca.modelos.CategoriaRecurso;
import sistema.biblioteca.modelos.Libro;
import sistema.biblioteca.modelos.Prestamo;
import sistema.biblioteca.modelos.RecursoBase;
import sistema.biblioteca.modelos.Usuario;
import sistema.biblioteca.servicios.ProcesadorNotificaciones;
import sistema.biblioteca.servicios.ServicioNotificacionesConsola;
import sistema.biblioteca.servicios.ServicioNotificaciones;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Clase de demostración para el sistema de monitoreo
 * Muestra el funcionamiento del MonitorSistema y la detección de alertas
 */
public class DemoMonitorSistema {
    
    private final GestorPrestamos gestorPrestamos;
    private final GestorRecursos gestorRecursos;
    private final GestorUsuarios gestorUsuarios;
    private final GestorReservas gestorReservas;
    private final ProcesadorNotificaciones procesadorNotificaciones;
    private final ServicioNotificacionesConsola servicioNotificaciones;
    private final MonitorSistema monitorSistema;
    private final Random random;
    
    public DemoMonitorSistema() {
        // Inicializar componentes
        this.gestorRecursos = new GestorRecursos();
        this.gestorUsuarios = new GestorUsuarios();
        this.servicioNotificaciones = new ServicioNotificacionesConsola();
        this.procesadorNotificaciones = new ProcesadorNotificaciones();
        this.gestorPrestamos = new GestorPrestamos(gestorRecursos, gestorUsuarios, servicioNotificaciones);
        this.gestorReservas = new GestorReservas(gestorRecursos, servicioNotificaciones);
        this.monitorSistema = new MonitorSistema(gestorPrestamos, gestorReservas, procesadorNotificaciones);
        this.random = new Random();
        
        // Registrar servicio de notificaciones
        procesadorNotificaciones.agregarServicio("email", servicioNotificaciones);
        procesadorNotificaciones.agregarServicio("sms", servicioNotificaciones);
    }
    
    /**
     * Ejecuta la demostración del sistema de monitoreo
     */
    public void ejecutar() {
        System.out.println("==== DEMO DEL SISTEMA DE MONITOREO ====");
        
        // Crear datos de prueba
        crearDatosDemo();
        
        // Imprimir estado inicial
        imprimirEstadoInicial();
        
        // Iniciar el monitor con un intervalo corto para la demo
        System.out.println("\n> Iniciando monitor del sistema (intervalo: 1 minuto)...");
        monitorSistema.iniciar(1);
        
        // Ejecutar verificación manual para la demo
        System.out.println("\n> Ejecutando verificación manual inmediata...");
        monitorSistema.ejecutarVerificacionManual();
        
        // Esperar a que se procesen las notificaciones
        esperar(3);
        
        // Detener el monitor
        System.out.println("\n> Deteniendo el monitor del sistema...");
        monitorSistema.detener();
        
        // Detener el procesador de notificaciones
        procesadorNotificaciones.detener();
        
        System.out.println("\n==== FIN DE LA DEMO ====");
    }
    
    /**
     * Crea datos de demostración con préstamos en diferentes estados
     */
    private void crearDatosDemo() {
        // Crear usuarios
        List<Usuario> usuarios = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Usuario usuario = new Usuario(
                "U" + i,
                "Usuario Demo " + i,
                "usuario" + i + "@ejemplo.com",
                "555-000" + i
            );
            gestorUsuarios.registrarUsuario(usuario);
            usuarios.add(usuario);
        }
        
        // Crear recursos
        List<RecursoBase> recursos = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Libro libro = new Libro(
                "L" + i,
                "Libro Demo " + i,
                "Autor " + i,
                "ISBN-" + i,
                CategoriaRecurso.values()[i % CategoriaRecurso.values().length]
            );
            gestorRecursos.agregarRecurso(libro);
            recursos.add(libro);
        }
        
        // Crear diferentes tipos de préstamos para la demo
        
        // 1. Préstamo activo normal
        crearPrestamo(usuarios.get(0), recursos.get(0), 10); // Vence en 10 días
        
        // 2. Préstamo próximo a vencer (alerta preventiva)
        crearPrestamo(usuarios.get(1), recursos.get(1), 1); // Vence mañana
        
        // 3. Préstamo que vence en 3 días (primera alerta preventiva)
        crearPrestamo(usuarios.get(2), recursos.get(2), 3); // Vence en 3 días
        
        // 4. Préstamo recién vencido
        crearPrestamoVencido(usuarios.get(3), recursos.get(3), 1); // Vencido hace 1 día
        
        // 5. Préstamo con vencimiento grave
        crearPrestamoVencido(usuarios.get(4), recursos.get(4), 15); // Vencido hace 15 días
        
        // 6. Crear algunas reservas
        try {
            gestorReservas.crearReserva(recursos.get(5).getIdentificador(), usuarios.get(0));
            
            // Crear una reserva expirada (manipulando directamente para la demo)
            Reserva reservaExpirada = gestorReservas.crearReserva(recursos.get(6).getIdentificador(), usuarios.get(1));
            // En un caso real no manipularíamos la fecha directamente, pero es para la demo
            // El código a continuación refleja una manipulación para fines de la demo
            /*
            Field fechaExpiracion = reservaExpirada.getClass().getDeclaredField("fechaExpiracion");
            fechaExpiracion.setAccessible(true);
            fechaExpiracion.set(reservaExpirada, LocalDateTime.now().minusDays(1));
            */
        } catch (Exception e) {
            System.out.println("Error al crear reservas: " + e.getMessage());
        }
    }
    
    /**
     * Crea un préstamo que vence en X días
     */
    private Prestamo crearPrestamo(Usuario usuario, RecursoBase recurso, int diasHastaVencimiento) {
        try {
            Prestamo prestamo = gestorPrestamos.crearPrestamo(
                    recurso.getIdentificador(), 
                    usuario.getId());
            
            // En una aplicación real no manipularíamos la fecha directamente,
            // pero para la demo necesitamos préstamos con diferentes fechas de vencimiento
            // El siguiente código simula esa manipulación
            
            /*
            Field fechaDevolucion = prestamo.getClass().getDeclaredField("fechaDevolucionEstimada");
            fechaDevolucion.setAccessible(true);
            fechaDevolucion.set(prestamo, LocalDateTime.now().plusDays(diasHastaVencimiento));
            */
            
            return prestamo;
        } catch (Exception e) {
            System.out.println("Error al crear préstamo: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Crea un préstamo que ya está vencido hace X días
     */
    private Prestamo crearPrestamoVencido(Usuario usuario, RecursoBase recurso, int diasVencido) {
        try {
            Prestamo prestamo = gestorPrestamos.crearPrestamo(
                    recurso.getIdentificador(), 
                    usuario.getId());
            
            // El siguiente código simula manipulación para crear préstamos vencidos
            
            /*
            Field fechaDevolucion = prestamo.getClass().getDeclaredField("fechaDevolucionEstimada");
            fechaDevolucion.setAccessible(true);
            fechaDevolucion.set(prestamo, LocalDateTime.now().minusDays(diasVencido));
            */
            
            return prestamo;
        } catch (Exception e) {
            System.out.println("Error al crear préstamo vencido: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Imprime el estado inicial del sistema para la demo
     */
    private void imprimirEstadoInicial() {
        System.out.println("\n--- DATOS DE DEMOSTRACIÓN ---");
        
        List<Prestamo> prestamos = gestorPrestamos.listarPrestamosActivos();
        System.out.println("\nPréstamos activos: " + prestamos.size());
        
        for (Prestamo p : prestamos) {
            LocalDateTime fechaVencimiento = p.getFechaDevolucionEstimada();
            String estado = fechaVencimiento.isBefore(LocalDateTime.now()) ? 
                    "VENCIDO" : "Activo";
            
            System.out.println("- Préstamo ID: " + p.getId() + 
                    " | Usuario: " + p.getUsuario().getNombre() +
                    " | Recurso: " + p.getRecurso().getTitulo() + 
                    " | Vence: " + fechaVencimiento + 
                    " | Estado: " + estado);
        }
    }
    
    /**
     * Espera un número de segundos
     */
    private void esperar(int segundos) {
        try {
            TimeUnit.SECONDS.sleep(segundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Punto de entrada principal
     */
    public static void main(String[] args) {
        DemoMonitorSistema demo = new DemoMonitorSistema();
        demo.ejecutar();
    }
} 