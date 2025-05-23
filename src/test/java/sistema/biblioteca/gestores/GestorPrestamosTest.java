package sistema.biblioteca.gestores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.biblioteca.excepciones.RecursoNoDisponibleException;
import sistema.biblioteca.excepciones.UsuarioNoEncontradoException;
import sistema.biblioteca.modelos.CategoriaRecurso;
import sistema.biblioteca.modelos.Libro;
import sistema.biblioteca.modelos.Prestamo;
import sistema.biblioteca.modelos.Usuario;
import sistema.biblioteca.servicios.ServicioNotificacionesEmail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class GestorPrestamosTest {
    
    private GestorPrestamos gestorPrestamos;
    private GestorRecursos gestorRecursos;
    private GestorUsuarios gestorUsuarios;
    private ServicioNotificacionesEmail servicioNotificaciones;
    
    private Usuario usuario;
    private Libro libro;
    
    @BeforeEach
    public void setUp() {
        gestorRecursos = new GestorRecursos();
        gestorUsuarios = new GestorUsuarios();
        servicioNotificaciones = new ServicioNotificacionesEmail();
        gestorPrestamos = new GestorPrestamos(gestorRecursos, gestorUsuarios, servicioNotificaciones);
        
        // Crear usuario y libro de prueba
        usuario = new Usuario("U001", "Usuario Test", "test@ejemplo.com");
        libro = new Libro("L001", "Libro Test", "Autor Test", "1234567890", CategoriaRecurso.FICCION);
        
        gestorUsuarios.registrarUsuario(usuario);
        gestorRecursos.agregarRecurso(libro);
    }
    
    @Test
    public void testCrearPrestamo() throws RecursoNoDisponibleException, UsuarioNoEncontradoException {
        // Verificar condiciones iniciales
        assertEquals(0, gestorPrestamos.getCantidadPrestamosActivos());
        assertTrue(libro.estaDisponible());
        
        // Crear préstamo
        Prestamo prestamo = gestorPrestamos.crearPrestamo("L001", "U001");
        
        // Verificar que el préstamo se creó correctamente
        assertNotNull(prestamo);
        assertEquals(1, gestorPrestamos.getCantidadPrestamosActivos());
        assertFalse(libro.estaDisponible());
        assertEquals(usuario, prestamo.getUsuario());
        assertEquals(libro, prestamo.getRecurso());
        assertTrue(prestamo.isActivo());
        
        // Verificar que la lista de préstamos activos contiene el préstamo
        assertEquals(1, gestorPrestamos.listarPrestamosActivos().size());
        assertTrue(gestorPrestamos.listarPrestamosActivos().contains(prestamo));
    }
    
    @Test
    public void testDevolverPrestamo() throws RecursoNoDisponibleException, UsuarioNoEncontradoException {
        // Crear préstamo
        Prestamo prestamo = gestorPrestamos.crearPrestamo("L001", "U001");
        String idPrestamo = prestamo.getId();
        
        // Verificar que el préstamo se creó correctamente
        assertEquals(1, gestorPrestamos.getCantidadPrestamosActivos());
        assertFalse(libro.estaDisponible());
        
        // Devolver préstamo
        gestorPrestamos.devolverPrestamo(idPrestamo);
        
        // Verificar que el préstamo se devolvió correctamente
        assertEquals(0, gestorPrestamos.getCantidadPrestamosActivos());
        assertTrue(libro.estaDisponible());
        assertNotNull(prestamo.getFechaDevolucionReal());
        assertFalse(prestamo.isActivo());
    }
    
    @Test
    public void testPrestamoRecursoNoDisponible() throws RecursoNoDisponibleException, UsuarioNoEncontradoException {
        // Crear primer préstamo
        gestorPrestamos.crearPrestamo("L001", "U001");
        
        // Intentar prestar el mismo libro nuevamente - debería fallar
        Exception exception = assertThrows(RecursoNoDisponibleException.class, () -> {
            gestorPrestamos.crearPrestamo("L001", "U001");
        });
        
        assertTrue(exception.getMessage().contains("no está disponible"));
    }
    
    @Test
    public void testPrestamoUsuarioNoEncontrado() {
        // Intentar prestar a un usuario que no existe
        Exception exception = assertThrows(UsuarioNoEncontradoException.class, () -> {
            gestorPrestamos.crearPrestamo("L001", "U999");
        });
        
        assertTrue(exception.getMessage().contains("No se encontró usuario"));
    }
    
    @Test
    public void testBuscarPrestamosPorUsuario() throws RecursoNoDisponibleException, UsuarioNoEncontradoException {
        // Crear usuario adicional
        Usuario usuario2 = new Usuario("U002", "Usuario 2", "usuario2@test.com");
        gestorUsuarios.registrarUsuario(usuario2);
        
        // Crear recurso adicional
        Libro libro2 = new Libro("L002", "Libro 2", "Autor 2", "9876543210", CategoriaRecurso.ACADEMICO);
        gestorRecursos.agregarRecurso(libro2);
        
        // Crear préstamos
        Prestamo p1 = gestorPrestamos.crearPrestamo("L001", "U001");
        Prestamo p2 = gestorPrestamos.crearPrestamo("L002", "U002");
        
        // Buscar préstamos por usuario
        assertEquals(1, gestorPrestamos.buscarPrestamosPorUsuario("U001").size());
        assertEquals(1, gestorPrestamos.buscarPrestamosPorUsuario("U002").size());
        assertEquals(p1, gestorPrestamos.buscarPrestamosPorUsuario("U001").get(0));
        assertEquals(p2, gestorPrestamos.buscarPrestamosPorUsuario("U002").get(0));
    }
    
    @Test
    public void testRenovarPrestamo() throws RecursoNoDisponibleException, UsuarioNoEncontradoException {
        // Crear préstamo
        Prestamo prestamo = gestorPrestamos.crearPrestamo("L001", "U001");
        String idPrestamo = prestamo.getId();
        
        // Guardar fecha inicial de devolución
        var fechaDevolucionOriginal = prestamo.getFechaDevolucionEstimada();
        
        // Renovar préstamo
        gestorPrestamos.renovarPrestamo(idPrestamo, 7);
        
        // Verificar que la fecha de devolución se ha extendido
        assertTrue(prestamo.getFechaDevolucionEstimada().isAfter(fechaDevolucionOriginal));
        
        // Verificar que el préstamo sigue activo
        assertTrue(prestamo.isActivo());
    }
    
    @Test
    public void testRenovarPrestamoInexistente() {
        // Intentar renovar un préstamo que no existe
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gestorPrestamos.renovarPrestamo("ID-INEXISTENTE", 7);
        });
        
        assertTrue(exception.getMessage().contains("no existe"));
    }
    
    @Test
    public void testBuscarPrestamoPorId() throws RecursoNoDisponibleException, UsuarioNoEncontradoException {
        // Crear préstamo
        Prestamo prestamo = gestorPrestamos.crearPrestamo("L001", "U001");
        String idPrestamo = prestamo.getId();
        
        // Buscar préstamo por ID
        Prestamo encontrado = gestorPrestamos.buscarPrestamoPorId(idPrestamo);
        
        // Verificar que el préstamo encontrado es el correcto
        assertNotNull(encontrado);
        assertEquals(prestamo, encontrado);
    }
    
    @Test
    public void testConcurrenciaCrearPrestamos() throws InterruptedException {
        // Preparar múltiples recursos y usuarios
        int numeroRecursos = 10;
        int numeroHilos = 5;
        
        for (int i = 1; i <= numeroRecursos; i++) {
            Libro libroTemp = new Libro(
                "L" + (1000 + i), 
                "Libro " + i, 
                "Autor " + i, 
                "ISBN-" + i, 
                CategoriaRecurso.FICCION
            );
            gestorRecursos.agregarRecurso(libroTemp);
            
            Usuario usuarioTemp = new Usuario(
                "U" + (1000 + i), 
                "Usuario " + i, 
                "usuario" + i + "@test.com"
            );
            gestorUsuarios.registrarUsuario(usuarioTemp);
        }
        
        // Usar CountDownLatch para sincronizar hilos
        CountDownLatch latch = new CountDownLatch(numeroHilos);
        AtomicInteger prestamosCreados = new AtomicInteger(0);
        
        // Crear un pool de hilos
        ExecutorService executorService = Executors.newFixedThreadPool(numeroHilos);
        
        for (int i = 0; i < numeroHilos; i++) {
            final int hiloId = i;
            executorService.submit(() -> {
                try {
                    // Cada hilo intenta crear 2 préstamos
                    for (int j = 0; j < 2; j++) {
                        int resourceIndex = hiloId * 2 + j + 1;
                        if (resourceIndex <= numeroRecursos) {
                            try {
                                Prestamo p = gestorPrestamos.crearPrestamo(
                                    "L" + (1000 + resourceIndex), 
                                    "U" + (1000 + resourceIndex)
                                );
                                if (p != null) {
                                    prestamosCreados.incrementAndGet();
                                }
                            } catch (Exception e) {
                                // Ignorar errores
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Esperar a que todos los hilos terminen
        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();
        
        // Verificar el número total de préstamos creados
        assertEquals(prestamosCreados.get(), gestorPrestamos.getCantidadPrestamosActivos());
        assertTrue(prestamosCreados.get() <= numeroRecursos);
    }
    
    @Test
    public void testConcurrenciaDevolverPrestamos() throws Exception {
        // Preparar préstamos
        List<Prestamo> prestamos = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Libro libroTemp = new Libro(
                "L" + (2000 + i), 
                "Libro Devolución " + i, 
                "Autor " + i, 
                "ISBN-" + i, 
                CategoriaRecurso.FICCION
            );
            gestorRecursos.agregarRecurso(libroTemp);
            
            Usuario usuarioTemp = new Usuario(
                "U" + (2000 + i), 
                "Usuario Devolución " + i, 
                "usuario.dev" + i + "@test.com"
            );
            gestorUsuarios.registrarUsuario(usuarioTemp);
            
            Prestamo p = gestorPrestamos.crearPrestamo(
                "L" + (2000 + i), 
                "U" + (2000 + i)
            );
            prestamos.add(p);
        }
        
        // Verificar que se han creado 5 préstamos
        assertEquals(5, prestamos.size());
        
        // Devolver préstamos concurrentemente
        CountDownLatch latch = new CountDownLatch(prestamos.size());
        ExecutorService executorService = Executors.newFixedThreadPool(prestamos.size());
        
        for (Prestamo p : prestamos) {
            executorService.submit(() -> {
                try {
                    gestorPrestamos.devolverPrestamo(p.getId());
                } catch (Exception e) {
                    // Ignorar errores
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Esperar a que todas las devoluciones terminen
        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();
        
        // Verificar que no hay préstamos activos
        assertEquals(0, gestorPrestamos.getCantidadPrestamosActivos());
        
        // Verificar que todos los recursos están disponibles nuevamente
        for (int i = 1; i <= 5; i++) {
            assertTrue(gestorRecursos.estaDisponible("L" + (2000 + i)));
        }
    }
    
    @Test
    public void testListarTodosLosPrestamos() throws RecursoNoDisponibleException, UsuarioNoEncontradoException {
        // Verificar que inicialmente no hay préstamos
        assertEquals(0, gestorPrestamos.listarTodosLosPrestamos().size());
        
        // Crear usuario adicional y libro adicional
        Usuario usuario2 = new Usuario("U002", "Usuario 2", "usuario2@test.com");
        gestorUsuarios.registrarUsuario(usuario2);
        
        Libro libro2 = new Libro("L002", "Libro 2", "Autor 2", "9876543210", CategoriaRecurso.ACADEMICO);
        gestorRecursos.agregarRecurso(libro2);
        
        // Crear dos préstamos
        Prestamo p1 = gestorPrestamos.crearPrestamo("L001", "U001");
        Prestamo p2 = gestorPrestamos.crearPrestamo("L002", "U002");
        
        // Verificar que hay dos préstamos en total
        List<Prestamo> todosLosPrestamos = gestorPrestamos.listarTodosLosPrestamos();
        assertEquals(2, todosLosPrestamos.size());
        assertTrue(todosLosPrestamos.contains(p1));
        assertTrue(todosLosPrestamos.contains(p2));
        
        // Devolver un préstamo
        gestorPrestamos.devolverPrestamo(p1.getId());
        
        // Verificar que siguen existiendo dos préstamos en total (uno activo y uno devuelto)
        todosLosPrestamos = gestorPrestamos.listarTodosLosPrestamos();
        assertEquals(2, todosLosPrestamos.size());
        assertEquals(1, gestorPrestamos.listarPrestamosActivos().size());
    }
    
    @Test
    public void testRenovarPrestamoConMotivo() throws RecursoNoDisponibleException, UsuarioNoEncontradoException {
        // Crear préstamo
        Prestamo prestamo = gestorPrestamos.crearPrestamo("L001", "U001");
        String idPrestamo = prestamo.getId();
        
        // Guardar fecha inicial de devolución
        var fechaDevolucionOriginal = prestamo.getFechaDevolucionEstimada();
        
        // Renovar préstamo con motivo
        String motivo = "Necesidad de más tiempo para leer";
        gestorPrestamos.renovarPrestamo(idPrestamo, 7, motivo);
        
        // Verificar que la fecha de devolución se ha extendido
        assertTrue(prestamo.getFechaDevolucionEstimada().isAfter(fechaDevolucionOriginal));
        
        // Verificar historial
        assertEquals(1, prestamo.getCantidadRenovaciones());
        
        // Verificar que el motivo se guardó correctamente
        var historial = gestorPrestamos.obtenerHistorialRenovaciones(idPrestamo);
        assertEquals(1, historial.size());
        assertEquals(motivo, historial.get(0).getMotivo());
        assertEquals(7, historial.get(0).getDiasExtendidos());
    }
    
    @Test
    public void testMultiplesRenovaciones() throws RecursoNoDisponibleException, UsuarioNoEncontradoException {
        // Crear préstamo
        Prestamo prestamo = gestorPrestamos.crearPrestamo("L001", "U001");
        String idPrestamo = prestamo.getId();
        
        // Realizar múltiples renovaciones
        gestorPrestamos.renovarPrestamo(idPrestamo, 3, "Primera renovación");
        gestorPrestamos.renovarPrestamo(idPrestamo, 5, "Segunda renovación");
        gestorPrestamos.renovarPrestamo(idPrestamo, 2, "Tercera renovación");
        
        // Verificar cantidad de renovaciones
        assertEquals(3, prestamo.getCantidadRenovaciones());
        assertEquals(3, gestorPrestamos.obtenerCantidadRenovaciones(idPrestamo));
        
        // Verificar historial completo
        var historial = gestorPrestamos.obtenerHistorialRenovaciones(idPrestamo);
        assertEquals(3, historial.size());
        
        // Verificar que los motivos y días se guardaron correctamente
        assertEquals("Primera renovación", historial.get(0).getMotivo());
        assertEquals(3, historial.get(0).getDiasExtendidos());
        
        assertEquals("Segunda renovación", historial.get(1).getMotivo());
        assertEquals(5, historial.get(1).getDiasExtendidos());
        
        assertEquals("Tercera renovación", historial.get(2).getMotivo());
        assertEquals(2, historial.get(2).getDiasExtendidos());
    }
    
    @Test
    public void testListarPrestamosRenovados() throws RecursoNoDisponibleException, UsuarioNoEncontradoException {
        // Crear varios préstamos
        Prestamo p1 = gestorPrestamos.crearPrestamo("L001", "U001");
        
        // Crear usuario adicional y recurso adicional
        Usuario usuario2 = new Usuario("U002", "Usuario 2", "usuario2@test.com");
        gestorUsuarios.registrarUsuario(usuario2);
        
        Libro libro2 = new Libro("L002", "Libro 2", "Autor 2", "9876543210", CategoriaRecurso.ACADEMICO);
        gestorRecursos.agregarRecurso(libro2);
        
        Prestamo p2 = gestorPrestamos.crearPrestamo("L002", "U002");
        
        // Renovar solo uno de los préstamos
        gestorPrestamos.renovarPrestamo(p1.getId(), 5, "Test renovación");
        
        // Verificar que solo aparece un préstamo en la lista de renovados
        List<Prestamo> renovados = gestorPrestamos.listarPrestamosRenovados();
        assertEquals(1, renovados.size());
        assertEquals(p1.getId(), renovados.get(0).getId());
    }
    
    @Test
    public void testNoSePuedeRenovarPrestamoDevuelto() throws RecursoNoDisponibleException, UsuarioNoEncontradoException {
        // Crear préstamo
        Prestamo prestamo = gestorPrestamos.crearPrestamo("L001", "U001");
        String idPrestamo = prestamo.getId();
        
        // Devolver el préstamo
        gestorPrestamos.devolverPrestamo(idPrestamo);
        
        // Intentar renovar un préstamo ya devuelto
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gestorPrestamos.renovarPrestamo(idPrestamo, 7, "No debería funcionar");
        });
        
        assertTrue(exception.getMessage().contains("ya ha sido devuelto"));
    }
} 