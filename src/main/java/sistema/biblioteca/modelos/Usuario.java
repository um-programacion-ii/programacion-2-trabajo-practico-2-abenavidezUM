package sistema.biblioteca.modelos;

import sistema.biblioteca.interfaces.Notificable;
import java.util.ArrayList;
import java.util.List;

public class Usuario implements Notificable {
    private String id;
    private String nombre;
    private String email;
    private String telefono;
    private List<Notificacion> notificaciones;
    private int cantidadPrestamos;

    public Usuario(String id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.notificaciones = new ArrayList<>();
        this.cantidadPrestamos = 0;
    }

    public Usuario(String id, String nombre, String email, String telefono) {
        this(id, nombre, email);
        this.telefono = telefono;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getCantidadPrestamos() {
        return cantidadPrestamos;
    }

    public void incrementarPrestamos() {
        this.cantidadPrestamos++;
    }

    public void decrementarPrestamos() {
        if (this.cantidadPrestamos > 0) {
            this.cantidadPrestamos--;
        }
    }

    @Override
    public void enviarNotificacion(String mensaje) {
        Notificacion nuevaNotificacion = new Notificacion(mensaje, this);
        this.notificaciones.add(nuevaNotificacion);
        System.out.println("Notificación enviada a " + this.nombre + ": " + mensaje);
    }

    @Override
    public List<Notificacion> getNotificacionesPendientes() {
        return new ArrayList<>(notificaciones);
    }

    public void limpiarNotificaciones() {
        this.notificaciones.clear();
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", prestamos=" + cantidadPrestamos +
                '}';
    }
}
