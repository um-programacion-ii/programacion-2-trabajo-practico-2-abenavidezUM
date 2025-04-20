package sistema.biblioteca.servicios;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.biblioteca.modelos.Usuario;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ProcesadorNotificacionesTest {
    
    private ProcesadorNotificaciones procesador;
    private MockServicioNotificaciones mockServicio;
    private Usuario usuario;
    
    @BeforeEach
    void setUp() {
        procesador = new ProcesadorNotificaciones();
        mockServicio = new MockServicioNotificaciones();
        procesador.agregarServicio("test", mockServicio);
        
        usuario = new Usuario("U001", "Usuario de Prueba", "test@ejemplo.com", "555-1234");
    }
    
    @AfterEach
    void tearDown() {
        procesador.detener();
    }
    
    @Test
    void debeEnviarNotificacion() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        mockServicio.setLatch(latch);
        
        // Act
        procesador.encolarNotificacion(usuario, "Mensaje de prueba", "test");
        
        // Assert - Esperar a que el procesador envíe la notificación
        assertTrue(latch.await(2, TimeUnit.SECONDS), "La notificación debería enviarse en 2 segundos");
        assertEquals(1, mockServicio.getContadorNotificaciones(), "Debería haberse enviado 1 notificación");
        assertEquals("Mensaje de prueba", mockServicio.getUltimoMensaje(), "El mensaje no coincide");
    }
    
    @Test
    void debeRespetarPrioridades() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(2);
        mockServicio.setLatch(latch);
        
        // Act - Enviamos en orden inverso de prioridad
        procesador.encolarNotificacion(usuario, "Mensaje con prioridad baja", "test", 9);
        procesador.encolarNotificacion(usuario, "Mensaje con prioridad alta", "test", 1);
        
        // Assert - Esperar a que ambas notificaciones se procesen
        assertTrue(latch.await(3, TimeUnit.SECONDS), "Las notificaciones deberían enviarse en 3 segundos");
        assertEquals(2, mockServicio.getContadorNotificaciones(), "Deberían haberse enviado 2 notificaciones");
        
        // Verificar orden por prioridad - El último mensaje debería ser el de baja prioridad
        assertEquals("Mensaje con prioridad baja", mockServicio.getUltimoMensaje(), 
                "El último mensaje debería ser el de baja prioridad");
    }
    
    @Test
    void debeProcesFallosCorrectamente() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        mockServicio.setLatch(latch);
        mockServicio.setSimularFallo(true);
        
        // Act
        procesador.encolarNotificacion(usuario, "Mensaje que fallará", "test");
        
        // Assert
        // Incluso con el fallo, el hilo debería seguir funcionando sin excepciones
        // Esperamos un poco para ver si se procesan más mensajes
        TimeUnit.SECONDS.sleep(1);
        
        // Desactivar fallos y probar que sigue funcionando
        mockServicio.setSimularFallo(false);
        CountDownLatch latch2 = new CountDownLatch(1);
        mockServicio.setLatch(latch2);
        
        procesador.encolarNotificacion(usuario, "Mensaje que debería enviarse", "test");
        
        assertTrue(latch2.await(2, TimeUnit.SECONDS), "La segunda notificación debería enviarse correctamente");
        assertEquals("Mensaje que debería enviarse", mockServicio.getUltimoMensaje());
    }
    
    @Test
    void debeEncolarMultiplesNotificacionesConcurrentemente() throws InterruptedException {
        // Arrange
        int numeroNotificaciones = 20;
        CountDownLatch latch = new CountDownLatch(numeroNotificaciones);
        mockServicio.setLatch(latch);
        
        // Act - Simular múltiples hilos enviando notificaciones
        for (int i = 0; i < numeroNotificaciones; i++) {
            final int numero = i;
            new Thread(() -> {
                procesador.encolarNotificacion(usuario, "Mensaje concurrente " + numero, "test");
            }).start();
        }
        
        // Assert
        assertTrue(latch.await(5, TimeUnit.SECONDS), 
                "Todas las notificaciones deberían procesarse en 5 segundos");
        assertEquals(numeroNotificaciones, mockServicio.getContadorNotificaciones(), 
                "Deberían haberse enviado todas las notificaciones");
    }
    
    /**
     * Servicio mock para pruebas
     */
    private static class MockServicioNotificaciones implements ServicioNotificaciones {
        private final AtomicInteger contadorNotificaciones = new AtomicInteger(0);
        private volatile String ultimoMensaje;
        private CountDownLatch latch;
        private boolean simularFallo = false;
        
        @Override
        public boolean enviarNotificacion(Usuario usuario, String mensaje) {
            if (simularFallo) {
                throw new RuntimeException("Error simulado en el envío de notificación");
            }
            
            contadorNotificaciones.incrementAndGet();
            ultimoMensaje = mensaje;
            
            if (latch != null) {
                latch.countDown();
            }
            
            return true;
        }
        
        @Override
        public int enviarNotificacionGlobal(String mensaje) {
            return 0; // No implementado para pruebas
        }
        
        @Override
        public boolean notificacionesPendientes(Usuario usuario) {
            return false; // No implementado para pruebas
        }
        
        @Override
        public void procesarNotificacionesPendientes() {
            // No implementado para pruebas
        }
        
        public int getContadorNotificaciones() {
            return contadorNotificaciones.get();
        }
        
        public String getUltimoMensaje() {
            return ultimoMensaje;
        }
        
        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }
        
        public void setSimularFallo(boolean simularFallo) {
            this.simularFallo = simularFallo;
        }
    }
} 