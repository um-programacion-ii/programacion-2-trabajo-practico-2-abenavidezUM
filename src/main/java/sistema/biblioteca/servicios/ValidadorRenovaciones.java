package sistema.biblioteca.servicios;

import sistema.biblioteca.colas.ColaReservas;
import sistema.biblioteca.gestores.GestorReglaRenovacion;
import sistema.biblioteca.modelos.Prestamo;
import sistema.biblioteca.modelos.RecursoBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para validar si una renovación de préstamo es posible
 * según las reglas definidas y otras condiciones del sistema.
 */
public class ValidadorRenovaciones {
    private GestorReglaRenovacion gestorReglas;
    private ColaReservas colaReservas;
    
    public ValidadorRenovaciones(GestorReglaRenovacion gestorReglas, ColaReservas colaReservas) {
        this.gestorReglas = gestorReglas;
        this.colaReservas = colaReservas;
    }
    
    /**
     * Representa un motivo por el cual no se puede renovar un préstamo
     */
    public enum MotivoRechazoRenovacion {
        PRESTAMO_INACTIVO("El préstamo ya ha sido devuelto"),
        PRESTAMO_VENCIDO("El préstamo está vencido y no puede renovarse"),
        LIMITE_RENOVACIONES("Se ha alcanzado el límite de renovaciones para este recurso"),
        RESERVAS_PENDIENTES("Hay reservas pendientes para este recurso"),
        REQUIERE_AUTORIZACION("Este tipo de recurso requiere autorización para renovarse");
        
        private final String mensaje;
        
        MotivoRechazoRenovacion(String mensaje) {
            this.mensaje = mensaje;
        }
        
        public String getMensaje() {
            return mensaje;
        }
    }
    
    /**
     * Resultado de la validación de una renovación
     */
    public static class ResultadoValidacion {
        private boolean renovacionPermitida;
        private List<MotivoRechazoRenovacion> motivos;
        private int diasSugeridos;
        
        public ResultadoValidacion() {
            this.renovacionPermitida = true;
            this.motivos = new ArrayList<>();
            this.diasSugeridos = 0;
        }
        
        public boolean isRenovacionPermitida() {
            return renovacionPermitida;
        }
        
        public void setRenovacionPermitida(boolean renovacionPermitida) {
            this.renovacionPermitida = renovacionPermitida;
        }
        
        public List<MotivoRechazoRenovacion> getMotivos() {
            return motivos;
        }
        
        public void agregarMotivo(MotivoRechazoRenovacion motivo) {
            if (motivo != null) {
                this.motivos.add(motivo);
                this.renovacionPermitida = false;
            }
        }
        
        public int getDiasSugeridos() {
            return diasSugeridos;
        }
        
        public void setDiasSugeridos(int diasSugeridos) {
            this.diasSugeridos = diasSugeridos;
        }
        
        public String getMensajesRechazo() {
            StringBuilder mensaje = new StringBuilder();
            for (MotivoRechazoRenovacion motivo : motivos) {
                mensaje.append("- ").append(motivo.getMensaje()).append("\n");
            }
            return mensaje.toString();
        }
    }
    
    /**
     * Valida si un préstamo puede ser renovado según las reglas y condiciones actuales.
     * 
     * @param prestamo El préstamo a renovar
     * @return Resultado de la validación con información detallada
     */
    public ResultadoValidacion validarRenovacion(Prestamo prestamo) {
        ResultadoValidacion resultado = new ResultadoValidacion();
        
        if (prestamo == null) {
            resultado.setRenovacionPermitida(false);
            return resultado;
        }
        
        // Verificar si el préstamo está activo
        if (!prestamo.isActivo()) {
            resultado.agregarMotivo(MotivoRechazoRenovacion.PRESTAMO_INACTIVO);
        }
        
        // Verificar si el préstamo está vencido
        if (prestamo.estaVencido()) {
            resultado.agregarMotivo(MotivoRechazoRenovacion.PRESTAMO_VENCIDO);
        }
        
        RecursoBase recurso = prestamo.getRecurso();
        
        // Verificar número máximo de renovaciones según la categoría
        if (!gestorReglas.puedeRenovarse(recurso, prestamo.getCantidadRenovaciones())) {
            resultado.agregarMotivo(MotivoRechazoRenovacion.LIMITE_RENOVACIONES);
        }
        
        // Verificar si hay reservas pendientes para este recurso
        if (hayReservasPendientes(recurso.getIdentificador())) {
            resultado.agregarMotivo(MotivoRechazoRenovacion.RESERVAS_PENDIENTES);
        }
        
        // Verificar si requiere autorización especial
        if (gestorReglas.requiereAutorizacion(recurso)) {
            resultado.agregarMotivo(MotivoRechazoRenovacion.REQUIERE_AUTORIZACION);
        }
        
        // Si la renovación es permitida, sugerir días según la categoría
        if (resultado.isRenovacionPermitida()) {
            resultado.setDiasSugeridos(gestorReglas.getDiasParaRenovacion(recurso));
        }
        
        return resultado;
    }
    
    /**
     * Verifica si hay reservas pendientes para un recurso específico.
     * 
     * @param idRecurso Identificador del recurso
     * @return true si hay reservas pendientes, false en caso contrario
     */
    private boolean hayReservasPendientes(String idRecurso) {
        // Si no hay cola de reservas configurada, asumir que no hay reservas
        if (colaReservas == null) {
            return false;
        }
        
        try {
            // Verificar si hay reservas pendientes para este recurso
            return colaReservas.tieneReservasPendientes(idRecurso);
        } catch (Exception e) {
            // En caso de error, por seguridad asumimos que no hay reservas
            System.out.println("Error al verificar reservas pendientes: " + e.getMessage());
            return false;
        }
    }
} 