package sistema.biblioteca.colas;

import sistema.biblioteca.modelos.Usuario;

/**
 * Implementación concreta de SolicitudReserva
 */
public class SolicitudReservaImpl implements SolicitudReserva {
    
    private final TipoSolicitud tipo;
    private final String idRecurso;
    private final Usuario usuario;
    private final String idReserva;
    private final int diasExtension;
    
    private SolicitudReservaImpl(Builder builder) {
        this.tipo = builder.tipo;
        this.idRecurso = builder.idRecurso;
        this.usuario = builder.usuario;
        this.idReserva = builder.idReserva;
        this.diasExtension = builder.diasExtension;
    }
    
    @Override
    public TipoSolicitud getTipo() {
        return tipo;
    }
    
    @Override
    public String getIdRecurso() {
        return idRecurso;
    }
    
    @Override
    public Usuario getUsuario() {
        return usuario;
    }
    
    @Override
    public String getIdReserva() {
        return idReserva;
    }
    
    @Override
    public int getDiasExtension() {
        return diasExtension;
    }
    
    /**
     * Crea una solicitud para crear una reserva
     * @param idRecurso el ID del recurso
     * @param usuario el usuario que realiza la reserva
     * @return la solicitud
     */
    public static SolicitudReserva crearReserva(String idRecurso, Usuario usuario) {
        return new Builder()
                .tipo(TipoSolicitud.CREAR)
                .idRecurso(idRecurso)
                .usuario(usuario)
                .build();
    }
    
    /**
     * Crea una solicitud para cancelar una reserva
     * @param idReserva el ID de la reserva a cancelar
     * @return la solicitud
     */
    public static SolicitudReserva cancelarReserva(String idReserva) {
        return new Builder()
                .tipo(TipoSolicitud.CANCELAR)
                .idReserva(idReserva)
                .build();
    }
    
    /**
     * Crea una solicitud para completar una reserva
     * @param idReserva el ID de la reserva a completar
     * @return la solicitud
     */
    public static SolicitudReserva completarReserva(String idReserva) {
        return new Builder()
                .tipo(TipoSolicitud.COMPLETAR)
                .idReserva(idReserva)
                .build();
    }
    
    /**
     * Crea una solicitud para extender una reserva
     * @param idReserva el ID de la reserva a extender
     * @param dias el número de días a extender
     * @return la solicitud
     */
    public static SolicitudReserva extenderReserva(String idReserva, int dias) {
        return new Builder()
                .tipo(TipoSolicitud.EXTENDER)
                .idReserva(idReserva)
                .diasExtension(dias)
                .build();
    }
    
    /**
     * Crea una solicitud para verificar las reservas expiradas
     * @return la solicitud
     */
    public static SolicitudReserva verificarExpiradas() {
        return new Builder()
                .tipo(TipoSolicitud.VERIFICAR_EXPIRADAS)
                .build();
    }
    
    /**
     * Constructor de SolicitudReservaImpl
     */
    public static class Builder {
        private TipoSolicitud tipo;
        private String idRecurso;
        private Usuario usuario;
        private String idReserva;
        private int diasExtension;
        
        public Builder tipo(TipoSolicitud tipo) {
            this.tipo = tipo;
            return this;
        }
        
        public Builder idRecurso(String idRecurso) {
            this.idRecurso = idRecurso;
            return this;
        }
        
        public Builder usuario(Usuario usuario) {
            this.usuario = usuario;
            return this;
        }
        
        public Builder idReserva(String idReserva) {
            this.idReserva = idReserva;
            return this;
        }
        
        public Builder diasExtension(int diasExtension) {
            this.diasExtension = diasExtension;
            return this;
        }
        
        public SolicitudReservaImpl build() {
            return new SolicitudReservaImpl(this);
        }
    }
    
    @Override
    public String toString() {
        return "SolicitudReserva{" +
                "tipo=" + tipo +
                ", idRecurso='" + idRecurso + '\'' +
                ", usuario=" + (usuario != null ? usuario.getNombre() : "null") +
                ", idReserva='" + idReserva + '\'' +
                ", diasExtension=" + diasExtension +
                '}';
    }
} 