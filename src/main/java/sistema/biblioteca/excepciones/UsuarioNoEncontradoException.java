package sistema.biblioteca.excepciones;

public class UsuarioNoEncontradoException extends Exception {
    
    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
    
    public UsuarioNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
} 