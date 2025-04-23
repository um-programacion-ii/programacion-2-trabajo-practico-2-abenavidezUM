package sistema.biblioteca.interfaces;

import sistema.biblioteca.modelos.EstadoRecurso;

public interface RecursoDigital {
    String getIdentificador();
    EstadoRecurso getEstado();
    void actualizarEstado(EstadoRecurso estado);
} 