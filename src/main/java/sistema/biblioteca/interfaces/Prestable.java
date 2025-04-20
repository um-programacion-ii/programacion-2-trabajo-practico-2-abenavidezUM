package sistema.biblioteca.interfaces;

import sistema.biblioteca.modelos.Usuario;
import java.time.LocalDateTime;

public interface Prestable {
    boolean estaDisponible();
    LocalDateTime getFechaDevolucion();
    void prestar(Usuario usuario);
} 