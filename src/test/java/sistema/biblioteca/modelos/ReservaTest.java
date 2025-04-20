package sistema.biblioteca.modelos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReservaTest {
    
    private Reserva reserva;
    private RecursoBase recurso;
    private Usuario usuario;
    
    @BeforeEach
    public void setUp() {
        usuario = new Usuario("U001", "Usuario Test", "test@ejemplo.com");
        recurso = new Libro("L001", "Libro Test", "Autor Test", "1234567890", CategoriaRecurso.FICCION);
        reserva = new Reserva("R001", recurso, usuario);
    }
    
    @Test
    public void testCreacionReserva() {
        assertNotNull(reserva);
        assertEquals("R001", reserva.getId());
        assertEquals(recurso, reserva.getRecurso());
        assertEquals(usuario, reserva.getUsuario());
        assertEquals(Reserva.EstadoReserva.PENDIENTE, reserva.getEstado());
        assertNotNull(reserva.getFechaReserva());
        assertNotNull(reserva.getFechaExpiracion());
    }
    
    @Test
    public void testCambiosEstado() {
        // Estado inicial
        assertTrue(reserva.estaPendiente());
        assertFalse(reserva.estaCompletada());
        assertFalse(reserva.estaExpirada());
        assertFalse(reserva.estaCancelada());
        
        // Completar reserva
        reserva.completar();
        assertFalse(reserva.estaPendiente());
        assertTrue(reserva.estaCompletada());
        assertFalse(reserva.estaExpirada());
        assertFalse(reserva.estaCancelada());
        
        // Crear una nueva reserva para probar otros estados
        Reserva reserva2 = new Reserva("R002", recurso, usuario);
        
        // Cancelar reserva
        reserva2.cancelar();
        assertFalse(reserva2.estaPendiente());
        assertFalse(reserva2.estaCompletada());
        assertFalse(reserva2.estaExpirada());
        assertTrue(reserva2.estaCancelada());
        
        // Crear una nueva reserva para probar expiración
        Reserva reserva3 = new Reserva("R003", recurso, usuario);
        
        // Expirar reserva
        reserva3.expirar();
        assertFalse(reserva3.estaPendiente());
        assertFalse(reserva3.estaCompletada());
        assertTrue(reserva3.estaExpirada());
        assertFalse(reserva3.estaCancelada());
    }
    
    @Test
    public void testExtenderExpiracion() {
        // Obtener la fecha de expiración original
        java.time.LocalDateTime fechaExpiracionOriginal = reserva.getFechaExpiracion();
        
        // Extender 5 días
        reserva.extenderExpiracion(5);
        
        // Verificar que la fecha se extendió correctamente
        java.time.LocalDateTime nuevaFechaExpiracion = reserva.getFechaExpiracion();
        assertTrue(nuevaFechaExpiracion.isAfter(fechaExpiracionOriginal));
        
        // La diferencia debe ser de 5 días
        long diferenciaDias = java.time.temporal.ChronoUnit.DAYS.between(
            fechaExpiracionOriginal.toLocalDate(), nuevaFechaExpiracion.toLocalDate());
        assertEquals(5, diferenciaDias);
    }
    
    @Test
    public void testNoSePuedeExtenderExpiracionSiNoEstaPendiente() {
        // Completar la reserva
        reserva.completar();
        
        // Obtener la fecha de expiración original
        java.time.LocalDateTime fechaExpiracionOriginal = reserva.getFechaExpiracion();
        
        // Intentar extender 5 días
        reserva.extenderExpiracion(5);
        
        // Verificar que la fecha NO cambió
        assertEquals(fechaExpiracionOriginal, reserva.getFechaExpiracion());
    }
    
    @Test
    public void testConstructorConDiasPersonalizados() {
        // Crear reserva con 7 días de expiración
        Reserva reservaPersonalizada = new Reserva("R004", recurso, usuario, 7);
        
        // La fecha de expiración debe ser aproximadamente 7 días después de la fecha de reserva
        long diferenciaDias = java.time.temporal.ChronoUnit.DAYS.between(
            reservaPersonalizada.getFechaReserva().toLocalDate(), 
            reservaPersonalizada.getFechaExpiracion().toLocalDate());
        
        assertEquals(7, diferenciaDias);
    }
} 