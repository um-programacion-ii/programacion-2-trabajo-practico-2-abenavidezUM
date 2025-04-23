package sistema.biblioteca.colas;

import sistema.biblioteca.excepciones.ReservaException;
import sistema.biblioteca.gestores.GestorReservas;
import sistema.biblioteca.modelos.Reserva;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Procesador de reservas que implementa un patrón productor-consumidor
 * para gestionar solicitudes de reserva de manera concurrente
 */
public class ProcesadorReservas {
    
    private final BlockingQueue<SolicitudReserva> colaSolicitudes;
    private final GestorReservas gestorReservas;
    private final ExecutorService consumidores;
    private final AtomicBoolean ejecutando;
    private final int numConsumidores;
    
    /**
     * Constructor
     * @param gestorReservas el gestor de reservas que procesará las solicitudes
     * @param numConsumidores el número de hilos consumidores
     */
    public ProcesadorReservas(GestorReservas gestorReservas, int numConsumidores) {
        this.gestorReservas = gestorReservas;
        this.colaSolicitudes = new LinkedBlockingQueue<>();
        this.ejecutando = new AtomicBoolean(true);
        this.numConsumidores = numConsumidores;
        this.consumidores = Executors.newFixedThreadPool(numConsumidores);
        
        iniciarConsumidores();
    }
    
    /**
     * Constructor que crea un solo hilo consumidor por defecto
     * @param gestorReservas el gestor de reservas que procesará las solicitudes
     */
    public ProcesadorReservas(GestorReservas gestorReservas) {
        this(gestorReservas, 1);
    }
    
    /**
     * Inicia los hilos consumidores que procesarán las solicitudes de reserva
     */
    private void iniciarConsumidores() {
        for (int i = 0; i < numConsumidores; i++) {
            consumidores.submit(this::procesarSolicitudes);
        }
    }
    
    /**
     * Método que se ejecuta en cada hilo consumidor para procesar solicitudes
     */
    private void procesarSolicitudes() {
        try {
            while (ejecutando.get()) {
                try {
                    // Intentar tomar una solicitud con timeout para poder verificar
                    // periódicamente si debemos seguir ejecutando
                    SolicitudReserva solicitud = colaSolicitudes.poll(1, TimeUnit.SECONDS);
                    if (solicitud != null) {
                        procesarSolicitud(solicitud);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    // Capturar cualquier excepción para evitar que el hilo muera
                    System.out.println("Error al procesar solicitud: " + e.getMessage());
                }
            }
        } finally {
            System.out.println("Hilo consumidor finalizado");
        }
    }
    
    /**
     * Procesa una solicitud de reserva específica
     * @param solicitud la solicitud a procesar
     */
    private void procesarSolicitud(SolicitudReserva solicitud) {
        try {
            switch (solicitud.getTipo()) {
                case CREAR:
                    gestorReservas.crearReserva(solicitud.getIdRecurso(), solicitud.getUsuario());
                    break;
                    
                case CANCELAR:
                    gestorReservas.cancelarReserva(solicitud.getIdReserva());
                    break;
                    
                case COMPLETAR:
                    gestorReservas.completarReserva(solicitud.getIdReserva());
                    break;
                    
                case EXTENDER:
                    gestorReservas.extenderReserva(solicitud.getIdReserva(), solicitud.getDiasExtension());
                    break;
                    
                case VERIFICAR_EXPIRADAS:
                    // Este caso se maneja internamente en el GestorReservas
                    break;
                    
                default:
                    System.out.println("Tipo de solicitud no reconocido: " + solicitud.getTipo());
            }
        } catch (ReservaException e) {
            System.out.println("Error al procesar solicitud " + solicitud.getTipo() + ": " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado al procesar solicitud: " + e.getMessage());
        }
    }
    
    /**
     * Añade una solicitud a la cola para ser procesada
     * @param solicitud la solicitud a procesar
     * @return true si la solicitud se añadió correctamente, false en caso contrario
     */
    public boolean agregarSolicitud(SolicitudReserva solicitud) {
        if (!ejecutando.get()) {
            return false;
        }
        
        try {
            colaSolicitudes.put(solicitud);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * Detiene el procesador de reservas
     */
    public void detener() {
        ejecutando.set(false);
        consumidores.shutdown();
        try {
            if (!consumidores.awaitTermination(10, TimeUnit.SECONDS)) {
                consumidores.shutdownNow();
            }
        } catch (InterruptedException e) {
            consumidores.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Obtiene el tamaño actual de la cola de solicitudes
     * @return el número de solicitudes en cola
     */
    public int getTamanoColaSolicitudes() {
        return colaSolicitudes.size();
    }
    
    /**
     * Verifica si el procesador está en ejecución
     * @return true si está en ejecución, false en caso contrario
     */
    public boolean estaEjecutando() {
        return ejecutando.get();
    }
} 