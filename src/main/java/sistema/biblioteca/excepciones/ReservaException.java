package sistema.biblioteca.excepciones;

public class ReservaException extends Exception {
    
    public ReservaException(String mensaje) {
        super(mensaje);
    }
    
    public ReservaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
} 