package sistema.biblioteca.gestores;

import sistema.biblioteca.excepciones.RecursoNoDisponibleException;
import sistema.biblioteca.excepciones.UsuarioNoEncontradoException;
import sistema.biblioteca.modelos.EstadoRecurso;
import sistema.biblioteca.modelos.Prestamo;
import sistema.biblioteca.modelos.RecursoBase;
import sistema.biblioteca.modelos.Usuario;
import sistema.biblioteca.servicios.ServicioNotificaciones;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GestorPrestamos {
    private Map<String, Prestamo> prestamos;
    private GestorRecursos gestorRecursos;
    private GestorUsuarios gestorUsuarios;
    private ServicioNotificaciones servicioNotificaciones;
    
    public GestorPrestamos(GestorRecursos gestorRecursos, GestorUsuarios gestorUsuarios,
                          ServicioNotificaciones servicioNotificaciones) {
        this.prestamos = new HashMap<>();
        this.gestorRecursos = gestorRecursos;
        this.gestorUsuarios = gestorUsuarios;
        this.servicioNotificaciones = servicioNotificaciones;
    }
    
    public Prestamo crearPrestamo(String idRecurso, String idUsuario) 
            throws RecursoNoDisponibleException, UsuarioNoEncontradoException {
        RecursoBase recurso = gestorRecursos.buscarRecursoPorId(idRecurso);
        
        if (recurso == null) {
            throw new RecursoNoDisponibleException("El recurso no existe: " + idRecurso);
        }
        
        Usuario usuario = gestorUsuarios.buscarUsuarioPorId(idUsuario);
        
        if (!recurso.estaDisponible()) {
            throw new RecursoNoDisponibleException("El recurso no está disponible: " + idRecurso);
        }
        
        String idPrestamo = generarIdPrestamo();
        Prestamo prestamo = new Prestamo(idPrestamo, recurso, usuario);
        
        // Actualizar estado del recurso
        recurso.actualizarEstado(EstadoRecurso.PRESTADO);
        
        // Actualizar contador de préstamos del usuario
        usuario.incrementarPrestamos();
        
        // Registrar préstamo
        prestamos.put(idPrestamo, prestamo);
        
        // Enviar notificación
        String mensaje = "Préstamo realizado con éxito. Recurso: " + recurso.getTitulo() 
                + " - Fecha devolución: " + prestamo.getFechaDevolucionEstimada();
        servicioNotificaciones.enviarNotificacion(usuario, mensaje);
        
        return prestamo;
    }
    
    public void devolverPrestamo(String idPrestamo) {
        Prestamo prestamo = prestamos.get(idPrestamo);
        
        if (prestamo != null && prestamo.isActivo()) {
            // Registrar devolución
            prestamo.registrarDevolucion();
            
            // Actualizar estado del recurso
            RecursoBase recurso = prestamo.getRecurso();
            recurso.actualizarEstado(EstadoRecurso.DISPONIBLE);
            
            // Actualizar contador de préstamos del usuario
            Usuario usuario = prestamo.getUsuario();
            usuario.decrementarPrestamos();
            
            // Notificar al usuario
            String mensaje = "Devolución registrada con éxito. Recurso: " + recurso.getTitulo();
            servicioNotificaciones.enviarNotificacion(usuario, mensaje);
        }
    }
    
    public List<Prestamo> buscarPrestamosPorUsuario(String idUsuario) {
        List<Prestamo> resultado = new ArrayList<>();
        
        for (Prestamo prestamo : prestamos.values()) {
            if (prestamo.getUsuario().getId().equals(idUsuario)) {
                resultado.add(prestamo);
            }
        }
        
        return resultado;
    }
    
    public List<Prestamo> buscarPrestamosPorRecurso(String idRecurso) {
        List<Prestamo> resultado = new ArrayList<>();
        
        for (Prestamo prestamo : prestamos.values()) {
            if (prestamo.getRecurso().getIdentificador().equals(idRecurso)) {
                resultado.add(prestamo);
            }
        }
        
        return resultado;
    }
    
    public List<Prestamo> listarPrestamosActivos() {
        List<Prestamo> activos = new ArrayList<>();
        
        for (Prestamo prestamo : prestamos.values()) {
            if (prestamo.isActivo()) {
                activos.add(prestamo);
            }
        }
        
        return activos;
    }
    
    public List<Prestamo> listarPrestamosVencidos() {
        List<Prestamo> vencidos = new ArrayList<>();
        
        for (Prestamo prestamo : prestamos.values()) {
            if (prestamo.isActivo() && prestamo.estaVencido()) {
                vencidos.add(prestamo);
            }
        }
        
        return vencidos;
    }
    
    public Prestamo buscarPrestamoPorId(String id) {
        return prestamos.get(id);
    }
    
    // Generar ID único para el préstamo
    private String generarIdPrestamo() {
        return "P-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public int getCantidadPrestamosTotales() {
        return prestamos.size();
    }
    
    public int getCantidadPrestamosActivos() {
        return listarPrestamosActivos().size();
    }
    
    public int getCantidadPrestamosVencidos() {
        return listarPrestamosVencidos().size();
    }
    
    public List<Prestamo> listarTodosLosPrestamos() {
        return new ArrayList<>(prestamos.values());
    }
    
    /**
     * Renueva un préstamo extendiendo su fecha de devolución
     * @param idPrestamo ID del préstamo a renovar
     * @param diasExtension Cantidad de días a extender el préstamo
     * @throws IllegalArgumentException si el préstamo no existe o no está activo
     */
    public void renovarPrestamo(String idPrestamo, int diasExtension) {
        if (diasExtension <= 0) {
            throw new IllegalArgumentException("Los días de extensión deben ser positivos");
        }
        
        Prestamo prestamo = prestamos.get(idPrestamo);
        
        if (prestamo == null) {
            throw new IllegalArgumentException("El préstamo con ID " + idPrestamo + " no existe");
        }
        
        if (!prestamo.isActivo()) {
            throw new IllegalArgumentException("El préstamo ya ha sido devuelto y no puede renovarse");
        }
        
        // Calcular nueva fecha de devolución
        LocalDateTime nuevaFechaDevolucion = prestamo.getFechaDevolucionEstimada().plusDays(diasExtension);
        prestamo.setFechaDevolucionEstimada(nuevaFechaDevolucion);
        
        // Notificar al usuario
        String mensaje = "Tu préstamo para " + prestamo.getRecurso().getTitulo() + 
                " ha sido renovado. Nueva fecha de devolución: " + 
                nuevaFechaDevolucion.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        servicioNotificaciones.enviarNotificacion(prestamo.getUsuario(), mensaje);
    }
} 