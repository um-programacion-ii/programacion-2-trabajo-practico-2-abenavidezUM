package sistema.biblioteca.servicios;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.biblioteca.colas.ColaReservasSimple;
import sistema.biblioteca.gestores.GestorReglaRenovacion;
import sistema.biblioteca.modelos.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ValidadorRenovacionesTest {
    
    private GestorReglaRenovacion gestorReglas;
    private ColaReservasSimple colaReservas;
    private ValidadorRenovaciones validador;
    
    @BeforeEach
    public void setUp() {
        gestorReglas = new GestorReglaRenovacion();
        colaReservas = new ColaReservasSimple();
        validador = new ValidadorRenovaciones(gestorReglas, colaReservas);
    }
    
    @Test
    public void testValidarRenovacionExitosa() {
        // Crear un préstamo activo con un libro de literatura
        Usuario usuario = new Usuario("U001", "Usuario Test", "usuario@test.com");
        Libro libro = new Libro("L001", "Libro Test", "Autor Test", "123456789", CategoriaRecurso.LITERATURA);
        
        Prestamo prestamo = new Prestamo("P-001", libro, usuario);
        
        // Validar que puede renovarse
        ValidadorRenovaciones.ResultadoValidacion resultado = validador.validarRenovacion(prestamo);
        
        assertTrue(resultado.isRenovacionPermitida());
        assertEquals(0, resultado.getMotivos().size());
        assertEquals(7, resultado.getDiasSugeridos()); // Según las reglas para LITERATURA
    }
    
    @Test
    public void testValidarRenovacionPrestamoDevuelto() {
        // Crear un préstamo que ya fue devuelto
        Usuario usuario = new Usuario("U001", "Usuario Test", "usuario@test.com");
        Libro libro = new Libro("L001", "Libro Test", "Autor Test", "123456789", CategoriaRecurso.LITERATURA);
        
        Prestamo prestamo = new Prestamo("P-001", libro, usuario);
        prestamo.registrarDevolucion(); // Marcar como devuelto
        
        ValidadorRenovaciones.ResultadoValidacion resultado = validador.validarRenovacion(prestamo);
        
        assertFalse(resultado.isRenovacionPermitida());
        assertTrue(resultado.getMotivos().contains(
            ValidadorRenovaciones.MotivoRechazoRenovacion.PRESTAMO_INACTIVO));
    }
    
    @Test
    public void testValidarRenovacionPrestamoVencido() {
        // Crear un préstamo activo pero vencido
        Usuario usuario = new Usuario("U001", "Usuario Test", "usuario@test.com");
        Libro libro = new Libro("L001", "Libro Test", "Autor Test", "123456789", CategoriaRecurso.LITERATURA);
        
        Prestamo prestamo = new Prestamo("P-001", libro, usuario) {
            @Override
            public boolean estaVencido() {
                return true; // Forzar a que esté vencido
            }
        };
        
        ValidadorRenovaciones.ResultadoValidacion resultado = validador.validarRenovacion(prestamo);
        
        assertFalse(resultado.isRenovacionPermitida());
        assertTrue(resultado.getMotivos().contains(
            ValidadorRenovaciones.MotivoRechazoRenovacion.PRESTAMO_VENCIDO));
    }
    
    @Test
    public void testValidarRenovacionConRenovacionesMaximas() {
        // Crear un préstamo con el máximo de renovaciones ya alcanzado
        Usuario usuario = new Usuario("U001", "Usuario Test", "usuario@test.com");
        Libro libro = new Libro("L001", "Libro Test", "Autor Test", "123456789", CategoriaRecurso.LITERATURA);
        
        Prestamo prestamo = new Prestamo("P-001", libro, usuario);
        
        // Simular que ya se ha renovado 2 veces (máximo para literatura según las reglas)
        for (int i = 0; i < 2; i++) {
            prestamo.renovar(7, "Test renovación " + (i + 1));
        }
        
        ValidadorRenovaciones.ResultadoValidacion resultado = validador.validarRenovacion(prestamo);
        
        assertFalse(resultado.isRenovacionPermitida());
        assertTrue(resultado.getMotivos().contains(
            ValidadorRenovaciones.MotivoRechazoRenovacion.LIMITE_RENOVACIONES));
    }
    
    @Test
    public void testValidarRenovacionConReservasPendientes() {
        // Crear un préstamo para un libro con reservas pendientes
        Usuario usuario = new Usuario("U001", "Usuario Test", "usuario@test.com");
        Libro libro = new Libro("L001", "Libro Test", "Autor Test", "123456789", CategoriaRecurso.LITERATURA);
        
        Prestamo prestamo = new Prestamo("P-001", libro, usuario);
        
        // Agregar una reserva para este libro
        colaReservas.agregarReserva("L001", "U002");
        
        ValidadorRenovaciones.ResultadoValidacion resultado = validador.validarRenovacion(prestamo);
        
        assertFalse(resultado.isRenovacionPermitida());
        assertTrue(resultado.getMotivos().contains(
            ValidadorRenovaciones.MotivoRechazoRenovacion.RESERVAS_PENDIENTES));
    }
    
    @Test
    public void testValidarRenovacionConAutoridadRequerida() {
        // Crear un préstamo para un recurso que requiere autorización para renovar
        Usuario usuario = new Usuario("U001", "Usuario Test", "usuario@test.com");
        Libro libro = new Libro("L001", "Libro Histórico", "Autor Test", "123456789", CategoriaRecurso.HISTORICO);
        
        Prestamo prestamo = new Prestamo("P-001", libro, usuario);
        
        ValidadorRenovaciones.ResultadoValidacion resultado = validador.validarRenovacion(prestamo);
        
        assertFalse(resultado.isRenovacionPermitida());
        assertTrue(resultado.getMotivos().contains(
            ValidadorRenovaciones.MotivoRechazoRenovacion.REQUIERE_AUTORIZACION));
    }
    
    @Test
    public void testDiasSugeridosSegunCategoria() {
        // Crear préstamos con diferentes categorías
        Usuario usuario = new Usuario("U001", "Usuario Test", "usuario@test.com");
        
        Libro libroAcademico = new Libro("L001", "Libro Académico", "Autor", "123", CategoriaRecurso.ACADEMICO);
        Prestamo prestamoAcademico = new Prestamo("P-001", libroAcademico, usuario);
        
        Libro libroReferencia = new Libro("L002", "Libro Referencia", "Autor", "456", CategoriaRecurso.REFERENCIA);
        Prestamo prestamoReferencia = new Prestamo("P-002", libroReferencia, usuario);
        
        // Validar días sugeridos según categoría
        ValidadorRenovaciones.ResultadoValidacion resultadoAcademico = 
                validador.validarRenovacion(prestamoAcademico);
        ValidadorRenovaciones.ResultadoValidacion resultadoReferencia = 
                validador.validarRenovacion(prestamoReferencia);
        
        // Según las reglas definidas en GestorReglaRenovacion
        assertEquals(10, resultadoAcademico.getDiasSugeridos());
        assertEquals(3, resultadoReferencia.getDiasSugeridos());
    }
} 