package sistema.biblioteca.gestores;

import sistema.biblioteca.excepciones.RecursoNoDisponibleException;
import sistema.biblioteca.excepciones.UsuarioNoEncontradoException;
import sistema.biblioteca.modelos.EstadoRecurso;
import sistema.biblioteca.modelos.Prestamo;
import sistema.biblioteca.modelos.RecursoBase;
import sistema.biblioteca.modelos.Usuario;
import sistema.biblioteca.servicios.ServicioNotificaciones;
import sistema.biblioteca.servicios.ValidadorRenovaciones;

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
    private ValidadorRenovaciones validadorRenovaciones;
    private GestorReglaRenovacion gestorReglaRenovacion;
    
    public GestorPrestamos(GestorRecursos gestorRecursos, GestorUsuarios gestorUsuarios,
                          ServicioNotificaciones servicioNotificaciones) {
        this.prestamos = new HashMap<>();
        this.gestorRecursos = gestorRecursos;
        this.gestorUsuarios = gestorUsuarios;
        this.servicioNotificaciones = servicioNotificaciones;
        
        // Inicializar gestor de reglas y validador de renovaciones
        this.gestorReglaRenovacion = new GestorReglaRenovacion();
        this.validadorRenovaciones = null; // Se configura con setValidadorRenovaciones
    }
    
    /**
     * Configura el validador de renovaciones para este gestor
     * 
     * @param validadorRenovaciones El validador a utilizar
     */
    public void setValidadorRenovaciones(ValidadorRenovaciones validadorRenovaciones) {
        this.validadorRenovaciones = validadorRenovaciones;
    }
    
    /**
     * Obtiene el gestor de reglas de renovación
     * 
     * @return El gestor de reglas
     */
    public GestorReglaRenovacion getGestorReglaRenovacion() {
        return gestorReglaRenovacion;
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
     * @param motivo Motivo de la renovación (opcional)
     * @param forzarRenovacion Si es true, ignora algunas validaciones (solo para admin)
     * @throws IllegalArgumentException si el préstamo no existe o no se puede renovar
     */
    public void renovarPrestamo(String idPrestamo, int diasExtension, String motivo, boolean forzarRenovacion) {
        if (diasExtension <= 0) {
            throw new IllegalArgumentException("Los días de extensión deben ser positivos");
        }
        
        Prestamo prestamo = prestamos.get(idPrestamo);
        
        if (prestamo == null) {
            throw new IllegalArgumentException("El préstamo con ID " + idPrestamo + " no existe");
        }
        
        // Si no hay validador configurado, usar validación simple
        if (validadorRenovaciones == null) {
            if (!prestamo.isActivo()) {
                throw new IllegalArgumentException("El préstamo ya ha sido devuelto y no puede renovarse");
            }
        } else if (!forzarRenovacion) {
            // Usar el validador para verificar reglas complejas
            ValidadorRenovaciones.ResultadoValidacion resultado = 
                    validadorRenovaciones.validarRenovacion(prestamo);
            
            if (!resultado.isRenovacionPermitida()) {
                throw new IllegalArgumentException(
                    "No se puede renovar el préstamo por los siguientes motivos:\n" + 
                    resultado.getMensajesRechazo());
            }
            
            // Usar los días sugeridos si no se forzó un valor específico
            if (diasExtension <= 0) {
                diasExtension = resultado.getDiasSugeridos();
            }
        }
        
        // Aplicar la renovación utilizando el nuevo método
        prestamo.renovar(diasExtension, motivo != null ? motivo : "Renovación estándar");
        
        // Notificar al usuario
        String mensaje = "Tu préstamo para " + prestamo.getRecurso().getTitulo() + 
                " ha sido renovado. Nueva fecha de devolución: " + 
                prestamo.getFechaDevolucionEstimada().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        servicioNotificaciones.enviarNotificacion(prestamo.getUsuario(), mensaje);
    }
    
    /**
     * Sobrecarga del método renovarPrestamo sin motivo y sin forzar
     */
    public void renovarPrestamo(String idPrestamo, int diasExtension) {
        renovarPrestamo(idPrestamo, diasExtension, null, false);
    }
    
    /**
     * Sobrecarga del método renovarPrestamo con motivo pero sin forzar
     */
    public void renovarPrestamo(String idPrestamo, int diasExtension, String motivo) {
        renovarPrestamo(idPrestamo, diasExtension, motivo, false);
    }
    
    /**
     * Obtiene el historial de renovaciones de un préstamo
     * @param idPrestamo ID del préstamo
     * @return Lista con el historial de renovaciones
     * @throws IllegalArgumentException si el préstamo no existe
     */
    public List<HistorialRenovacion> obtenerHistorialRenovaciones(String idPrestamo) {
        Prestamo prestamo = prestamos.get(idPrestamo);
        
        if (prestamo == null) {
            throw new IllegalArgumentException("El préstamo con ID " + idPrestamo + " no existe");
        }
        
        return prestamo.getHistorialRenovaciones();
    }
    
    /**
     * Obtiene la cantidad de renovaciones de un préstamo
     * @param idPrestamo ID del préstamo
     * @return Cantidad de renovaciones
     * @throws IllegalArgumentException si el préstamo no existe
     */
    public int obtenerCantidadRenovaciones(String idPrestamo) {
        Prestamo prestamo = prestamos.get(idPrestamo);
        
        if (prestamo == null) {
            throw new IllegalArgumentException("El préstamo con ID " + idPrestamo + " no existe");
        }
        
        return prestamo.getCantidadRenovaciones();
    }
    
    /**
     * Devuelve una lista de todos los préstamos que han sido renovados al menos una vez
     * @return Lista de préstamos renovados
     */
    public List<Prestamo> listarPrestamosRenovados() {
        List<Prestamo> renovados = new ArrayList<>();
        
        for (Prestamo prestamo : prestamos.values()) {
            if (prestamo.getCantidadRenovaciones() > 0) {
                renovados.add(prestamo);
            }
        }
        
        return renovados;
    }
} 