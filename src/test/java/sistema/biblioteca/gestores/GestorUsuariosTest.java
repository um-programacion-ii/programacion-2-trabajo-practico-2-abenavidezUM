package sistema.biblioteca.gestores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.biblioteca.excepciones.UsuarioNoEncontradoException;
import sistema.biblioteca.modelos.Usuario;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GestorUsuariosTest {
    
    private GestorUsuarios gestorUsuarios;
    private Usuario usuario1;
    private Usuario usuario2;
    
    @BeforeEach
    public void setUp() {
        gestorUsuarios = new GestorUsuarios();
        
        // Crear usuarios de prueba
        usuario1 = new Usuario("U001", "Ana García", "ana@ejemplo.com");
        usuario2 = new Usuario("U002", "Carlos López", "carlos@ejemplo.com", "555-1234");
        
        // Registrar usuarios
        gestorUsuarios.registrarUsuario(usuario1);
        gestorUsuarios.registrarUsuario(usuario2);
    }
    
    @Test
    public void testRegistrarUsuario() {
        // Verificar que los usuarios se registraron correctamente
        assertEquals(2, gestorUsuarios.getCantidadUsuarios());
        
        // Registrar un usuario nuevo
        Usuario usuario3 = new Usuario("U003", "Marta Rodríguez", "marta@ejemplo.com");
        gestorUsuarios.registrarUsuario(usuario3);
        
        // Verificar que se incrementó la cantidad de usuarios
        assertEquals(3, gestorUsuarios.getCantidadUsuarios());
        
        // Verificar que el usuario está en la lista de usuarios
        List<Usuario> usuarios = gestorUsuarios.listarUsuarios();
        assertTrue(usuarios.contains(usuario3));
    }
    
    @Test
    public void testBuscarUsuarioPorId() throws UsuarioNoEncontradoException {
        // Buscar usuario existente
        Usuario usuarioEncontrado = gestorUsuarios.buscarUsuarioPorId("U001");
        assertEquals(usuario1, usuarioEncontrado);
        
        // Buscar usuario inexistente
        Exception exception = assertThrows(UsuarioNoEncontradoException.class, () -> {
            gestorUsuarios.buscarUsuarioPorId("U999");
        });
        
        assertTrue(exception.getMessage().contains("No se encontró usuario"));
    }
    
    @Test
    public void testBuscarUsuariosPorNombre() {
        // Buscar usuarios con "García" en el nombre
        List<Usuario> usuariosGarcia = gestorUsuarios.buscarUsuariosPorNombre("García");
        assertEquals(1, usuariosGarcia.size());
        assertEquals(usuario1, usuariosGarcia.get(0));
        
        // Buscar usuarios con un nombre que no existe
        List<Usuario> usuariosNoExistentes = gestorUsuarios.buscarUsuariosPorNombre("Pérez");
        assertTrue(usuariosNoExistentes.isEmpty());
        
        // Las búsquedas deben ser case-insensitive
        List<Usuario> usuariosLopez = gestorUsuarios.buscarUsuariosPorNombre("lópez");
        assertEquals(1, usuariosLopez.size());
        assertEquals(usuario2, usuariosLopez.get(0));
    }
    
    @Test
    public void testEliminarUsuario() throws UsuarioNoEncontradoException {
        // Verificar condición inicial
        assertEquals(2, gestorUsuarios.getCantidadUsuarios());
        
        // Eliminar un usuario
        gestorUsuarios.eliminarUsuario("U001");
        
        // Verificar que se decrementó la cantidad de usuarios
        assertEquals(1, gestorUsuarios.getCantidadUsuarios());
        
        // Verificar que el usuario ya no existe
        assertFalse(gestorUsuarios.existeUsuario("U001"));
        
        // Intentar eliminar un usuario que no existe
        Exception exception = assertThrows(UsuarioNoEncontradoException.class, () -> {
            gestorUsuarios.eliminarUsuario("U999");
        });
        
        assertTrue(exception.getMessage().contains("No se puede eliminar"));
    }
} 