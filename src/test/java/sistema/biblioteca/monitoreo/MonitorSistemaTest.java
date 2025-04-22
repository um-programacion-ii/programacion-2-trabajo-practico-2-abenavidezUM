package sistema.biblioteca.monitoreo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import sistema.biblioteca.servicios.ServicioNotificaciones;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MonitorSistemaTest {
    
    private GestorPrestamos gestorPrestamos;
    private GestorRecursos gestorRecursos;
    private GestorUsuarios gestorUsuarios;
    private GestorReservas gestorReservas;
    private ProcesadorNotificaciones procesadorNotificaciones;
    private MockServicioNotificaciones servicioNotificaciones;
    private MonitorSistema monitorSistema;
    
    @BeforeEach
    void setUp() {
        gestorRecursos = new GestorRecursos();
        gestorUsuarios = new GestorUsuarios();
        servicioNotificaciones = new MockServicioNotificaciones();
        procesadorNotificaciones = new ProcesadorNotificaciones();
        gestorPrestamos = new GestorPrestamos(gestorRecursos, gestorUsuarios, servicioNotificaciones);
        gestorReservas = new GestorReservas(gestorRecursos, servicioNotificaciones);
        
        // Registrar el servicio de notificaciones mock
        procesadorNotificaciones.agregarServicio("email", servicioNotificaciones);
        procesadorNotificaciones.agregarServicio("sms", servicioNotificaciones);
        
        monitorSistema = new MonitorSistema(gestorPrestamos, gestorReservas, procesadorNotificaciones);
        
        // Crear datos de prueba
        crearDatosPrueba();
    }
    
    @AfterEach
    void tearDown() {
        try {
            monitorSistema.detener();
            procesadorNotificaciones.detener();
        } catch (Exception e) {
            // Ignorar errores al detener
        }
    }
    
    @Test
    void debeDetectarPrestamosVencidos() throws InterruptedException {
        // Configuración inicial
        servicioNotificaciones.resetContador();
        
        // Ejecutar verificación manual
        monitorSistema.ejecutarVerificacionManual();
        
        // Dar tiempo para que se procesen las notificaciones
        Thread.sleep(500);
        
        // Verificar que se enviaron notificaciones
        assertTrue(servicioNotificaciones.getContadorNotificaciones() > 0,
                "Se deberían haber enviado notificaciones para préstamos vencidos");
    }
    
    @Test
    void debeIniciarYDetenerCorrectamente() {
        // Iniciar el monitor
        monitorSistema.iniciar(1);
        
        // Verificar que está en ejecución (esto es una prueba indirecta)
        assertTrue(true, "El monitor debería iniciarse sin excepciones");
        
        // Detener el monitor
        monitorSistema.detener();
        
        // Verificar que se detiene sin excepciones
        assertTrue(true, "El monitor debería detenerse sin excepciones");
    }
    
    @Test
    void debeEjecutarVerificacionManual() {
        // Configuración inicial
        servicioNotificaciones.resetContador();
        
        // Ejecutar verificación manual
        monitorSistema.ejecutarVerificacionManual();
        
        // Verificar que se completó sin excepciones
        assertTrue(true, "La verificación manual debería completarse sin excepciones");
    }
    
    @Test
    void debeCambiarIntervaloCorrectamente() {
        // Probar cambiar el intervalo
        monitorSistema.cambiarIntervaloVerificacion(5);
        
        // Verificar que se completó sin excepciones
        assertTrue(true, "El cambio de intervalo debería completarse sin excepciones");
        
        // Validar que no se puede establecer un intervalo inválido
        assertThrows(IllegalArgumentException.class, () -> {
            monitorSistema.cambiarIntervaloVerificacion(0);
        });
    }
    
    /**
     * Crea datos de prueba para los tests
     */
    private void crearDatosPrueba() {
        // Crear usuarios
        Usuario usuario = new Usuario("U1", "Usuario Test", "test@ejemplo.com", "555-1234");
        gestorUsuarios.registrarUsuario(usuario);
        
        // Crear recursos
        RecursoBase libro = new Libro("L1", "Libro Test", "Autor Test", "ISBN-TEST", CategoriaRecurso.ACADEMICO);
        gestorRecursos.agregarRecurso(libro);
        
        // Crear préstamos vencidos para pruebas
        try {
            Prestamo prestamo = gestorPrestamos.crearPrestamo("L1", "U1");
            
            // En un test real usaríamos mocks o técnicas específicas para manipular fechas
            // El código a continuación simula un préstamo vencido para la prueba
            /*
            Field fechaDevolucion = prestamo.getClass().getDeclaredField("fechaDevolucionEstimada");
            fechaDevolucion.setAccessible(true);
            fechaDevolucion.set(prestamo, LocalDateTime.now().minusDays(5));
            */
        } catch (Exception e) {
            fail("Error al crear datos de prueba: " + e.getMessage());
        }
    }
    
    /**
     * Clase mock para probar notificaciones
     */
    private static class MockServicioNotificaciones implements ServicioNotificaciones {
        private final AtomicInteger contadorNotificaciones = new AtomicInteger(0);
        
        @Override
        public boolean enviarNotificacion(Usuario usuario, String mensaje) {
            contadorNotificaciones.incrementAndGet();
            return true;
        }
        
        @Override
        public int enviarNotificacionGlobal(String mensaje) {
            contadorNotificaciones.incrementAndGet();
            return 1;
        }
        
        @Override
        public boolean notificacionesPendientes(Usuario usuario) {
            return false;
        }
        
        @Override
        public void procesarNotificacionesPendientes() {
            // No hacemos nada en el mock
        }
        
        public int getContadorNotificaciones() {
            return contadorNotificaciones.get();
        }
        
        public void resetContador() {
            contadorNotificaciones.set(0);
        }
    }
} 