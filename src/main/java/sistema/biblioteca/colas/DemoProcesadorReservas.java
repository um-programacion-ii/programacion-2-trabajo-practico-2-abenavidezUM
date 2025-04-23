package sistema.biblioteca.colas;

import sistema.biblioteca.excepciones.ReservaException;
import sistema.biblioteca.gestores.GestorRecursos;
import sistema.biblioteca.gestores.GestorReservas;
import sistema.biblioteca.gestores.GestorUsuarios;
import sistema.biblioteca.modelos.CategoriaRecurso;
import sistema.biblioteca.modelos.Libro;
import sistema.biblioteca.modelos.Reserva;
import sistema.biblioteca.modelos.Usuario;
import sistema.biblioteca.servicios.ServicioNotificacionesConsola;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Clase para demostrar el funcionamiento del procesador de reservas concurrente
 */
public class DemoProcesadorReservas {
    
    private final GestorRecursos gestorRecursos;
    private final GestorUsuarios gestorUsuarios;
    private final GestorReservas gestorReservas;
    private final ProcesadorReservas procesadorReservas;
    private final Random random;
    
    public DemoProcesadorReservas() {
        this.gestorRecursos = new GestorRecursos();
        this.gestorUsuarios = new GestorUsuarios();
        ServicioNotificacionesConsola servicioNotificaciones = new ServicioNotificacionesConsola();
        this.gestorReservas = new GestorReservas(gestorRecursos, servicioNotificaciones);
        this.procesadorReservas = new ProcesadorReservas(gestorReservas, 3);
        this.random = new Random();
        
        crearDatosEjemplo();
    }
    
    /**
     * Crea datos de ejemplo para la demo
     */
    private void crearDatosEjemplo() {
        // Crear recursos
        for (int i = 1; i <= 10; i++) {
            Libro libro = new Libro(
                "L" + i,
                "Libro Demo " + i,
                "Autor " + i,
                "ISBN-" + i,
                CategoriaRecurso.values()[i % CategoriaRecurso.values().length]
            );
            gestorRecursos.agregarRecurso(libro);
        }
        
        // Crear usuarios
        for (int i = 1; i <= 20; i++) {
            Usuario usuario = new Usuario(
                "U" + i,
                "Usuario Demo " + i,
                "usuario" + i + "@ejemplo.com"
            );
            gestorUsuarios.registrarUsuario(usuario);
        }
    }
    
    /**
     * Ejecuta una simulación del sistema de reservas con múltiples solicitudes concurrentes
     */
    public void ejecutarSimulacion() throws InterruptedException {
        System.out.println("=== INICIO DE SIMULACIÓN DEL PROCESADOR DE RESERVAS ===");
        
        int numSolicitudes = 50;
        int numHilos = 5;
        
        CountDownLatch latch = new CountDownLatch(numSolicitudes);
        ExecutorService executorService = Executors.newFixedThreadPool(numHilos);
        
        System.out.println("Creando " + numSolicitudes + " solicitudes con " + numHilos + " hilos productores");
        
        for (int i = 0; i < numSolicitudes; i++) {
            final int solicitudId = i;
            executorService.submit(() -> {
                try {
                    // Simular carga de trabajo
                    Thread.sleep(random.nextInt(100));
                    
                    // Generar una solicitud aleatoria
                    agregarSolicitudAleatoria(solicitudId);
                } catch (Exception e) {
                    System.out.println("Error al generar solicitud: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Esperar a que todas las solicitudes sean creadas
        latch.await(30, TimeUnit.SECONDS);
        
        // Detener los hilos productores
        executorService.shutdown();
        
        // Esperar a que se procesen todas las solicitudes
        System.out.println("Esperando que se procesen todas las solicitudes...");
        Thread.sleep(5000);
        
        // Imprimir estadísticas
        mostrarEstadisticas();
        
        // Detener el procesador de reservas
        procesadorReservas.detener();
        
        System.out.println("=== FIN DE SIMULACIÓN ===");
    }
    
    /**
     * Agrega una solicitud aleatoria al procesador
     */
    private void agregarSolicitudAleatoria(int id) throws ReservaException {
        int tipo = random.nextInt(4);
        SolicitudReserva solicitud = null;
        
        switch (tipo) {
            case 0: // Crear
                String idRecurso = "L" + (random.nextInt(10) + 1);
                String idUsuario = "U" + (random.nextInt(20) + 1);
                Usuario usuario = gestorUsuarios.buscarUsuarioPorId(idUsuario);
                
                if (usuario != null) {
                    solicitud = SolicitudReservaImpl.crearReserva(idRecurso, usuario);
                    System.out.println("Solicitud " + id + ": Crear reserva para recurso " + idRecurso + 
                                      " y usuario " + usuario.getNombre());
                }
                break;
                
            case 1: // Cancelar
                List<Reserva> reservas = gestorReservas.listarReservasActivas();
                if (!reservas.isEmpty()) {
                    Reserva reserva = reservas.get(random.nextInt(reservas.size()));
                    solicitud = SolicitudReservaImpl.cancelarReserva(reserva.getId());
                    System.out.println("Solicitud " + id + ": Cancelar reserva " + reserva.getId());
                }
                break;
                
            case 2: // Completar
                reservas = gestorReservas.listarReservasActivas();
                if (!reservas.isEmpty()) {
                    Reserva reserva = reservas.get(random.nextInt(reservas.size()));
                    solicitud = SolicitudReservaImpl.completarReserva(reserva.getId());
                    System.out.println("Solicitud " + id + ": Completar reserva " + reserva.getId());
                }
                break;
                
            case 3: // Extender
                reservas = gestorReservas.listarReservasActivas();
                if (!reservas.isEmpty()) {
                    Reserva reserva = reservas.get(random.nextInt(reservas.size()));
                    int diasExtension = random.nextInt(5) + 1;
                    solicitud = SolicitudReservaImpl.extenderReserva(reserva.getId(), diasExtension);
                    System.out.println("Solicitud " + id + ": Extender reserva " + reserva.getId() + 
                                      " por " + diasExtension + " días");
                }
                break;
        }
        
        if (solicitud != null) {
            procesadorReservas.agregarSolicitud(solicitud);
        }
    }
    
    /**
     * Muestra estadísticas del sistema de reservas
     */
    private void mostrarEstadisticas() {
        List<Reserva> reservasActivas = gestorReservas.listarReservasActivas();
        
        System.out.println("\n--- ESTADÍSTICAS ---");
        System.out.println("Total de reservas activas: " + reservasActivas.size());
        System.out.println("Solicitudes pendientes en cola: " + procesadorReservas.getTamanoColaSolicitudes());
        
        // Mostrar reservas por recurso
        System.out.println("\nReservas por recurso:");
        for (int i = 1; i <= 10; i++) {
            String idRecurso = "L" + i;
            int reservasRecurso = (int) reservasActivas.stream()
                .filter(r -> r.getRecurso().getId().equals(idRecurso))
                .count();
            
            if (reservasRecurso > 0) {
                System.out.println("  - " + idRecurso + ": " + reservasRecurso + " reservas");
            }
        }
        
        // Mostrar reservas por usuario
        System.out.println("\nUsuarios con reservas activas:");
        List<String> usuariosConReservas = new ArrayList<>();
        for (Reserva r : reservasActivas) {
            String idUsuario = r.getUsuario().getId();
            if (!usuariosConReservas.contains(idUsuario)) {
                usuariosConReservas.add(idUsuario);
                int reservasUsuario = (int) reservasActivas.stream()
                    .filter(res -> res.getUsuario().getId().equals(idUsuario))
                    .count();
                
                System.out.println("  - " + r.getUsuario().getNombre() + ": " + reservasUsuario + " reservas");
            }
        }
    }
    
    /**
     * Método principal para ejecutar la demo
     */
    public static void main(String[] args) {
        try {
            DemoProcesadorReservas demo = new DemoProcesadorReservas();
            demo.ejecutarSimulacion();
        } catch (Exception e) {
            System.out.println("Error en la demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 