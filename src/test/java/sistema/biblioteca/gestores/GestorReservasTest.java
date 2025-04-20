package sistema.biblioteca.gestores;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.biblioteca.excepciones.ReservaException;
import sistema.biblioteca.modelos.CategoriaRecurso;
import sistema.biblioteca.modelos.Libro;
import sistema.biblioteca.modelos.RecursoBase;
import sistema.biblioteca.modelos.Reserva;
import sistema.biblioteca.modelos.Usuario;
import sistema.biblioteca.servicios.ServicioNotificacionesEmail;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GestorReservasTest {
    
    private GestorReservas gestorReservas;
    private GestorRecursos gestorRecursos;
    private ServicioNotificacionesEmail servicioNotificaciones;
    
    private Usuario usuario1;
    private Usuario usuario2;
    private Libro libro1;
    private Libro libro2;
    
    @BeforeEach
    void setUp() {
        gestorRecursos = new GestorRecursos();
        servicioNotificaciones = new ServicioNotificacionesEmail();
        gestorReservas = new GestorReservas(gestorRecursos, servicioNotificaciones);
        
        // Crear usuarios de prueba
        usuario1 = new Usuario("U001", "Usuario Test 1", "test1@ejemplo.com");
        usuario2 = new Usuario("U002", "Usuario Test 2", "test2@ejemplo.com");
        
        // Crear libros de prueba
        libro1 = new Libro("L001", "Libro Test 1", "Autor Test 1", "1234567890", 200, 2020, CategoriaRecurso.FICCION);
        libro2 = new Libro("L002", "Libro Test 2", "Autor Test 2", "0987654321", 300, 2021, CategoriaRecurso.ACADEMICO);
        
        // Agregar libros al gestor de recursos
        gestorRecursos.agregarRecurso(libro1);
        gestorRecursos.agregarRecurso(libro2);
    }
    
    @AfterEach
    void tearDown() {
        gestorReservas.parar();
    }
    
    @Test
    void crearReservaRecursoDisponible() throws ReservaException {
        // Verificar que el recurso está disponible inicialmente
        assertTrue(libro1.estaDisponible());
        
        // Crear reserva
        Reserva reserva = gestorReservas.crearReserva("L001", usuario1);
        
        // Verificar que la reserva se ha creado correctamente
        assertNotNull(reserva);
        assertEquals(libro1, reserva.getRecurso());
        assertEquals(usuario1, reserva.getUsuario());
        assertTrue(reserva.estaPendiente());
        
        // Verificar que la reserva está en la lista de reservas activas
        List<Reserva> reservasActivas = gestorReservas.listarReservasActivas();
        assertEquals(1, reservasActivas.size());
        assertTrue(reservasActivas.contains(reserva));
    }
    
    @Test
    void crearReservaRecursoNoDisponible() throws Exception {
        // Marcar el recurso como no disponible
        libro1.prestar(usuario2); // Prestamos el libro a otro usuario
        assertFalse(libro1.estaDisponible());
        
        // Crear reserva para un recurso no disponible
        Reserva reserva = gestorReservas.crearReserva("L001", usuario1);
        
        // Verificar que la reserva fue puesta en cola de espera
        assertEquals(-1, gestorReservas.getPosicionEnCola("L001", usuario1.getId())); // No está en posición 1 de la cola porque está en la lista de espera
        assertEquals(1, gestorReservas.longitudCola("L001"));
        
        // Verificar que la reserva no está en la lista de reservas activas
        List<Reserva> reservasActivas = gestorReservas.listarReservasActivas();
        assertEquals(0, reservasActivas.size());
    }
    
    @Test
    void cancelarReserva() throws ReservaException {
        // Crear reserva
        Reserva reserva = gestorReservas.crearReserva("L001", usuario1);
        String idReserva = reserva.getId();
        
        // Verificar que hay una reserva activa
        assertEquals(1, gestorReservas.listarReservasActivas().size());
        
        // Cancelar la reserva
        gestorReservas.cancelarReserva(idReserva);
        
        // Verificar que no hay reservas activas
        assertEquals(0, gestorReservas.listarReservasActivas().size());
    }
    
    @Test
    void cancelarReservaInexistente() {
        // Intentar cancelar una reserva que no existe
        Exception exception = assertThrows(ReservaException.class, () -> {
            gestorReservas.cancelarReserva("ID-INEXISTENTE");
        });
        
        assertTrue(exception.getMessage().contains("no existe"));
    }
    
    @Test
    void completarReserva() throws ReservaException {
        // Crear reserva
        Reserva reserva = gestorReservas.crearReserva("L001", usuario1);
        String idReserva = reserva.getId();
        
        // Completar la reserva
        gestorReservas.completarReserva(idReserva);
        
        // Verificar que no hay reservas activas
        assertEquals(0, gestorReservas.listarReservasActivas().size());
        
        // Verificar el estado de la reserva
        assertTrue(reserva.estaCompletada());
    }
    
    @Test
    void extenderReserva() throws ReservaException {
        // Crear reserva
        Reserva reserva = gestorReservas.crearReserva("L001", usuario1);
        String idReserva = reserva.getId();
        
        // Guardar fecha original de expiración
        var fechaExpiracionOriginal = reserva.getFechaExpiracion();
        
        // Extender la reserva
        gestorReservas.extenderReserva(idReserva, 5);
        
        // Verificar que la fecha de expiración ha sido extendida
        assertTrue(reserva.getFechaExpiracion().isAfter(fechaExpiracionOriginal));
    }
    
    @Test
    void listarReservasActivasPorUsuario() throws ReservaException {
        // Crear reservas para ambos usuarios
        Reserva r1 = gestorReservas.crearReserva("L001", usuario1);
        Reserva r2 = gestorReservas.crearReserva("L002", usuario2);
        
        // Verificar reservas activas por usuario
        List<Reserva> reservasUsuario1 = gestorReservas.listarReservasActivasPorUsuario(usuario1.getId());
        List<Reserva> reservasUsuario2 = gestorReservas.listarReservasActivasPorUsuario(usuario2.getId());
        
        assertEquals(1, reservasUsuario1.size());
        assertEquals(1, reservasUsuario2.size());
        assertEquals(r1, reservasUsuario1.get(0));
        assertEquals(r2, reservasUsuario2.get(0));
    }
    
    @Test
    void testConcurrenciaCrearReservas() throws InterruptedException {
        // Número de recursos y usuarios para la prueba
        int numeroRecursos = 10;
        
        // Crear recursos adicionales
        for (int i = 1; i <= numeroRecursos; i++) {
            RecursoBase recurso = new Libro(
                "L" + (1000 + i),
                "Libro Concurrencia " + i,
                "Autor " + i,
                "ISBN-" + i,
                200,
                2020,
                CategoriaRecurso.FICCION
            );
            gestorRecursos.agregarRecurso(recurso);
        }
        
        // Crear usuarios para la prueba
        Usuario[] usuarios = new Usuario[numeroRecursos];
        for (int i = 0; i < numeroRecursos; i++) {
            usuarios[i] = new Usuario(
                "U" + (1000 + i),
                "Usuario Concurrencia " + i,
                "usuario.conc" + i + "@test.com"
            );
        }
        
        // CountDownLatch para sincronizar hilos
        int numeroHilos = 5;
        CountDownLatch latch = new CountDownLatch(numeroHilos);
        AtomicInteger reservasCreadas = new AtomicInteger(0);
        
        // Ejecutar en múltiples hilos
        ExecutorService executorService = Executors.newFixedThreadPool(numeroHilos);
        
        for (int i = 0; i < numeroHilos; i++) {
            final int hiloId = i;
            executorService.submit(() -> {
                try {
                    // Cada hilo crea 2 reservas
                    for (int j = 0; j < 2; j++) {
                        int idx = hiloId * 2 + j;
                        if (idx < numeroRecursos) {
                            try {
                                Reserva r = gestorReservas.crearReserva(
                                    "L" + (1000 + idx + 1),
                                    usuarios[idx]
                                );
                                if (r != null) {
                                    reservasCreadas.incrementAndGet();
                                }
                            } catch (Exception e) {
                                System.out.println("Error al crear reserva: " + e.getMessage());
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Esperar a que terminen todos los hilos
        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();
        
        // Verificar número total de reservas creadas
        assertEquals(reservasCreadas.get(), gestorReservas.listarReservasActivas().size());
    }
    
    @Test
    void testProcesarSiguienteReservaEnCola() throws Exception {
        // Preparar: El libro1 no está disponible y hay una reserva en cola
        libro1.prestar(usuario2);
        assertFalse(libro1.estaDisponible());
        
        // Usuario1 hace una reserva que irá a la cola
        gestorReservas.crearReserva("L001", usuario1);
        assertEquals(1, gestorReservas.longitudCola("L001"));
        
        // Ahora el libro se devuelve, lo que debería activar la siguiente reserva en cola
        libro1.devolver();
        assertTrue(libro1.estaDisponible());
        
        // Esperamos un poco para que el procesador de reservas actúe
        Thread.sleep(100);
        
        // Verificar: La reserva debería haberse movido de la cola a las reservas activas
        assertEquals(0, gestorReservas.longitudCola("L001"));
        
        // Debería haber una reserva activa para el usuario1
        List<Reserva> reservasUsuario1 = gestorReservas.listarReservasActivasPorUsuario(usuario1.getId());
        assertEquals(1, reservasUsuario1.size());
        assertEquals("L001", reservasUsuario1.get(0).getRecurso().getIdentificador());
    }
    
    @Test
    void testContarReservasActivasUsuario() throws ReservaException {
        // Crear múltiples reservas para un mismo usuario
        gestorReservas.crearReserva("L001", usuario1);
        gestorReservas.crearReserva("L002", usuario1);
        
        // Verificar el conteo de reservas
        assertEquals(2, gestorReservas.contarReservasActivasUsuario(usuario1.getId()));
        assertEquals(0, gestorReservas.contarReservasActivasUsuario(usuario2.getId()));
    }
    
    @Test
    void testLimiteReservasPorUsuario() throws ReservaException {
        // Crear recursos adicionales para alcanzar el límite
        for (int i = 3; i <= 6; i++) {
            RecursoBase recurso = new Libro(
                "L00" + i,
                "Libro " + i,
                "Autor " + i,
                "ISBN-" + i,
                200,
                2020,
                CategoriaRecurso.FICCION
            );
            gestorRecursos.agregarRecurso(recurso);
        }
        
        // Crear 5 reservas (el límite permitido)
        for (int i = 1; i <= 5; i++) {
            gestorReservas.crearReserva("L00" + i, usuario1);
        }
        
        // Verificar que hay 5 reservas activas
        assertEquals(5, gestorReservas.contarReservasActivasUsuario(usuario1.getId()));
        
        // Intentar crear una sexta reserva debería fallar
        Exception exception = assertThrows(ReservaException.class, () -> {
            gestorReservas.crearReserva("L006", usuario1);
        });
        
        assertTrue(exception.getMessage().contains("máximo de reservas"));
    }
} 