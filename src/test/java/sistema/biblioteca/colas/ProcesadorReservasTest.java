package sistema.biblioteca.colas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.biblioteca.gestores.GestorRecursos;
import sistema.biblioteca.gestores.GestorReservas;
import sistema.biblioteca.gestores.GestorUsuarios;
import sistema.biblioteca.modelos.CategoriaRecurso;
import sistema.biblioteca.modelos.Libro;
import sistema.biblioteca.modelos.Reserva;
import sistema.biblioteca.modelos.Usuario;
import sistema.biblioteca.servicios.ServicioNotificacionesConsola;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ProcesadorReservasTest {
    
    private GestorRecursos gestorRecursos;
    private GestorUsuarios gestorUsuarios;
    private GestorReservas gestorReservas;
    private ProcesadorReservas procesadorReservas;
    private ServicioNotificacionesConsola servicioNotificaciones;
    
    @BeforeEach
    void setUp() {
        gestorRecursos = new GestorRecursos();
        gestorUsuarios = new GestorUsuarios();
        servicioNotificaciones = new ServicioNotificacionesConsola();
        gestorReservas = new GestorReservas(gestorRecursos, servicioNotificaciones);
        procesadorReservas = new ProcesadorReservas(gestorReservas, 3);
        
        // Crear datos de prueba
        crearDatosPrueba();
    }
    
    @AfterEach
    void tearDown() {
        // Detener el procesador de reservas
        procesadorReservas.detener();
    }
    
    private void crearDatosPrueba() {
        // Crear recursos
        for (int i = 1; i <= 5; i++) {
            Libro libro = new Libro(
                "L" + i,
                "Libro Test " + i,
                "Autor " + i,
                "ISBN-T" + i,
                CategoriaRecurso.values()[i % CategoriaRecurso.values().length]
            );
            gestorRecursos.agregarRecurso(libro);
        }
        
        // Crear usuarios
        for (int i = 1; i <= 5; i++) {
            Usuario usuario = new Usuario(
                "U" + i,
                "Usuario Test " + i,
                "test" + i + "@ejemplo.com"
            );
            gestorUsuarios.registrarUsuario(usuario);
        }
    }
    
    @Test
    void testAgregarSolicitudCrearReserva() throws InterruptedException {
        // Crear solicitud
        Usuario usuario = gestorUsuarios.buscarUsuarioPorId("U1");
        SolicitudReserva solicitud = SolicitudReservaImpl.crearReserva("L1", usuario);
        
        // Agregar solicitud
        procesadorReservas.agregarSolicitud(solicitud);
        
        // Esperar a que se procese
        Thread.sleep(1000);
        
        // Verificar que se creó la reserva
        assertEquals(1, gestorReservas.listarReservasActivas().size(), 
                "Debería haber una reserva activa");
        assertEquals("U1", gestorReservas.listarReservasActivas().get(0).getUsuario().getId(), 
                "La reserva debería ser del usuario U1");
    }
    
    @Test
    void testConcurrenciaProcesadorReservas() throws InterruptedException {
        int numSolicitudes = 50;
        int numHilos = 5;
        
        // CountDownLatch para sincronizar
        CountDownLatch latch = new CountDownLatch(numSolicitudes);
        AtomicInteger solicitudesCreadas = new AtomicInteger(0);
        
        // Crear solicitudes concurrentemente
        ExecutorService executor = Executors.newFixedThreadPool(numHilos);
        
        for (int i = 0; i < numSolicitudes; i++) {
            final int idUsuario = (i % 5) + 1;
            final int idRecurso = (i % 5) + 1;
            
            executor.submit(() -> {
                try {
                    Usuario usuario = gestorUsuarios.buscarUsuarioPorId("U" + idUsuario);
                    SolicitudReserva solicitud = SolicitudReservaImpl.crearReserva("L" + idRecurso, usuario);
                    
                    if (procesadorReservas.agregarSolicitud(solicitud)) {
                        solicitudesCreadas.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Esperar a que todas las solicitudes sean creadas
        assertTrue(latch.await(10, TimeUnit.SECONDS), 
                "Todas las solicitudes deberían haberse creado en 10 segundos");
        executor.shutdown();
        
        // Esperar a que se procesen
        Thread.sleep(2000);
        
        // Verificar resultados
        int reservasActivas = gestorReservas.listarReservasActivas().size();
        
        // Es difícil predecir exactamente cuántas reservas se crearán debido a la concurrencia
        // y las limitaciones del sistema (ej: máximo de reservas por usuario)
        // Nos aseguramos que al menos algunas se hayan creado
        assertTrue(reservasActivas > 0, "Debería haber al menos una reserva activa");
        
        // Verificar que la cola de solicitudes eventualmente se vacíe
        assertEquals(0, procesadorReservas.getTamanoColaSolicitudes(), 
                "La cola de solicitudes debería estar vacía");
    }
    
    @Test
    void testCicloCompletoProcesamiento() throws InterruptedException {
        // Crear una reserva
        Usuario usuario = gestorUsuarios.buscarUsuarioPorId("U1");
        procesadorReservas.agregarSolicitud(SolicitudReservaImpl.crearReserva("L1", usuario));
        
        // Esperar a que se procese
        Thread.sleep(1000);
        
        // Verificar que se creó
        assertEquals(1, gestorReservas.listarReservasActivas().size(), 
                "Debería haber una reserva activa");
        
        // Obtener el ID de la reserva
        String idReserva = gestorReservas.listarReservasActivas().get(0).getId();
        
        // Extender la reserva
        procesadorReservas.agregarSolicitud(SolicitudReservaImpl.extenderReserva(idReserva, 5));
        
        // Esperar a que se procese
        Thread.sleep(1000);
        
        // Completar la reserva
        procesadorReservas.agregarSolicitud(SolicitudReservaImpl.completarReserva(idReserva));
        
        // Esperar a que se procese
        Thread.sleep(1000);
        
        // Verificar que no hay reservas activas
        assertEquals(0, gestorReservas.listarReservasActivas().size(), 
                "No debería haber reservas activas después de completar");
    }
    
    @Test
    void testDetenerProcesador() throws InterruptedException {
        // Verificar que el procesador está ejecutando
        assertTrue(procesadorReservas.estaEjecutando(), 
                "El procesador debería estar ejecutando");
        
        // Detener el procesador
        procesadorReservas.detener();
        
        // Verificar que se detuvo
        assertFalse(procesadorReservas.estaEjecutando(), 
                "El procesador no debería estar ejecutando");
        
        // Intentar agregar una solicitud después de detener
        Usuario usuario = gestorUsuarios.buscarUsuarioPorId("U1");
        boolean resultado = procesadorReservas.agregarSolicitud(
                SolicitudReservaImpl.crearReserva("L1", usuario));
        
        // Verificar que no se pudo agregar
        assertFalse(resultado, 
                "No debería ser posible agregar solicitudes después de detener");
    }
} 