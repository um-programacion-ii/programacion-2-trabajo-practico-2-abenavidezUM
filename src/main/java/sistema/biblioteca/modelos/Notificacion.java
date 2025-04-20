package sistema.biblioteca.modelos;

import java.time.LocalDateTime;

public class Notificacion {
    private String mensaje;
    private Usuario destinatario;
    private LocalDateTime fechaCreacion;
    private boolean leida;

    public Notificacion(String mensaje, Usuario destinatario) {
        this.mensaje = mensaje;
        this.destinatario = destinatario;
        this.fechaCreacion = LocalDateTime.now();
        this.leida = false;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public boolean isLeida() {
        return leida;
    }

    public void marcarComoLeida() {
        this.leida = true;
    }

    @Override
    public String toString() {
        return "Notificacion{" +
                "mensaje='" + mensaje + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", leida=" + leida +
                '}';
    }
}
