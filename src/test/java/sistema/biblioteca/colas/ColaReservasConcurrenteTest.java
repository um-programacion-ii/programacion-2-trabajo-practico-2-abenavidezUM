package sistema.biblioteca.colas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.biblioteca.modelos.CategoriaRecurso;
import sistema.biblioteca.modelos.Libro;
import sistema.biblioteca.modelos.Reserva;
import sistema.biblioteca.modelos.Usuario;
import sistema.biblioteca.servicios.ServicioNotificacionesEmail;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ColaReservasConcurrenteTest {
    
    private ColaReservasConcurrente colaReservas;
    private ServicioNotificacionesEmail servicioNotificaciones;
    private Libro libro;
    private Usuario usuario1;
    private Usuario usuario2;
    
    @BeforeEach
    void setUp() {
        servicioNotificaciones = new ServicioNotificacionesEmail();
        colaReservas = new ColaReservasConcurrente(servicioNotificaciones);
        
        libro = new Libro("L001", "Libro Test", "Autor Test", "1234567890", 200, 2020, CategoriaRecurso.FICCION);
        usuario1 = new Usuario("U001", "Usuario 1", "usuario1@test.com");
        usuario2 = new Usuario("U002", "Usuario 2", "usuario2@test.com");
    }
    
    @Test
    void agregarAColaEspera() {
        // Crear reserva
        Reserva reserva = new Reserva(generarIdReserva(), libro, usuario1);
        
        // Agregar a cola de espera
        colaReservas.agregarAColaEspera(reserva);
        
        // Verificar que hay una reserva en cola
        assertTrue(colaReservas.hayReservasEnCola(libro));
        assertEquals(1, colaReservas.getTamañoCola(libro));
        
        // Verificar posición en cola
        assertEquals(1, colaReservas.obtenerPosicionEnCola(libro, usuario1));
    }
    
    @Test
    void obtenerSiguienteReserva() {
        // Crear y agregar una reserva
        Reserva reserva = new Reserva(generarIdReserva(), libro, usuario1);
        colaReservas.agregarAColaEspera(reserva);
        
        // Obtener la siguiente reserva
        Reserva siguiente = colaReservas.obtenerSiguienteReserva(libro);
        
        // Verificar que es la misma reserva
        assertEquals(reserva, siguiente);
        
        // Verificar que ya no hay reservas en la cola
        assertFalse(colaReservas.hayReservasEnCola(libro));
    }
    
    @Test
    void removerReservasDeUsuario() {
        // Crear y agregar varias reservas del mismo usuario para el mismo recurso
        Reserva r1 = new Reserva(generarIdReserva(), libro, usuario1);
        Reserva r2 = new Reserva(generarIdReserva(), libro, usuario1);
        Reserva r3 = new Reserva(generarIdReserva(), libro, usuario2);
        
        colaReservas.agregarAColaEspera(r1);
        colaReservas.agregarAColaEspera(r2);
        colaReservas.agregarAColaEspera(r3);
        
        // Verificar el número inicial de reservas
        assertEquals(3, colaReservas.getTamañoCola(libro));
        
        // Remover reservas del usuario1
        int removidas = colaReservas.removerReservasDeUsuario(libro, usuario1);
        
        // Verificar que se removieron 2 reservas
        assertEquals(2, removidas);
        assertEquals(1, colaReservas.getTamañoCola(libro));
        
        // Verificar que solo queda la reserva del usuario2
        assertEquals(1, colaReservas.obtenerPosicionEnCola(libro, usuario2));
        assertEquals(-1, colaReservas.obtenerPosicionEnCola(libro, usuario1));
    }
    
    @Test
    void notificarDisponibilidad() {
        // Crear y agregar una reserva
        Reserva reserva = new Reserva(generarIdReserva(), libro, usuario1);
        colaReservas.agregarAColaEspera(reserva);
        
        // Notificar disponibilidad
        colaReservas.notificarDisponibilidad(libro);
        
        // No podemos verificar directamente que la notificación se envió,
        // pero podemos verificar que la reserva sigue en la cola
        assertEquals(1, colaReservas.getTamañoCola(libro));
    }
    
    @Test
    void obtenerTodasReservasEnCola() {
        // Crear y agregar varias reservas
        Reserva r1 = new Reserva(generarIdReserva(), libro, usuario1);
        Reserva r2 = new Reserva(generarIdReserva(), libro, usuario2);
        
        colaReservas.agregarAColaEspera(r1);
        colaReservas.agregarAColaEspera(r2);
        
        // Obtener todas las reservas
        List<Reserva> reservas = colaReservas.obtenerTodasReservasEnCola(libro);
        
        // Verificar que la lista contiene ambas reservas
        assertEquals(2, reservas.size());
        assertTrue(reservas.contains(r1));
        assertTrue(reservas.contains(r2));
    }
    
    @Test
    void concurrenciaAgregarYObtenerReservas() throws InterruptedException {
        // Número de operaciones y hilos
        int numeroReservas = 100;
        int numeroHilos = 10;
        
        // CountDownLatch para sincronizar
        CountDownLatch latchAgregar = new CountDownLatch(numeroHilos);
        CountDownLatch latchObtener = new CountDownLatch(1);
        
        // Contador para verificar resultados
        AtomicInteger reservasAgregadas = new AtomicInteger(0);
        AtomicInteger reservasObtenidas = new AtomicInteger(0);
        
        // Ejecutor para operaciones concurrentes
        ExecutorService executorService = Executors.newFixedThreadPool(numeroHilos + 1);
        
        // Hilos para agregar reservas
        for (int i = 0; i < numeroHilos; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < numeroReservas / numeroHilos; j++) {
                        Reserva r = new Reserva(
                            generarIdReserva(), 
                            libro, 
                            new Usuario("U" + UUID.randomUUID().toString().substring(0, 8), 
                                       "Usuario Test", "test@example.com")
                        );
                        colaReservas.agregarAColaEspera(r);
                        reservasAgregadas.incrementAndGet();
                    }
                } finally {
                    latchAgregar.countDown();
                }
            });
        }
        
        // Hilo para obtener reservas
        executorService.submit(() -> {
            try {
                // Esperar a que se hayan agregado todas las reservas
                latchAgregar.await(5, TimeUnit.SECONDS);
                
                // Obtener reservas
                while (colaReservas.hayReservasEnCola(libro)) {
                    Reserva r = colaReservas.obtenerSiguienteReserva(libro);
                    if (r != null) {
                        reservasObtenidas.incrementAndGet();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latchObtener.countDown();
            }
        });
        
        // Esperar a que terminen todas las operaciones
        latchObtener.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        
        // Verificar resultados
        assertEquals(reservasAgregadas.get(), reservasObtenidas.get());
        assertFalse(colaReservas.hayReservasEnCola(libro));
    }
    
    // Método auxiliar para generar IDs únicos
    private String generarIdReserva() {
        return "R-" + UUID.randomUUID().toString().substring(0, 8);
    }
} 