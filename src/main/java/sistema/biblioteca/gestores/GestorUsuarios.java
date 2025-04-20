package sistema.biblioteca.gestores;

import sistema.biblioteca.excepciones.UsuarioNoEncontradoException;
import sistema.biblioteca.modelos.Usuario;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestorUsuarios {
    private Map<String, Usuario> usuarios;
    
    public GestorUsuarios() {
        this.usuarios = new HashMap<>();
    }
    
    public void registrarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            return;
        }
        
        usuarios.put(usuario.getId(), usuario);
    }
    
    public Usuario buscarUsuarioPorId(String id) throws UsuarioNoEncontradoException {
        if (!usuarios.containsKey(id)) {
            throw new UsuarioNoEncontradoException("No se encontró usuario con ID: " + id);
        }
        
        return usuarios.get(id);
    }
    
    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios.values());
    }
    
    public List<Usuario> buscarUsuariosPorNombre(String nombre) {
        List<Usuario> resultado = new ArrayList<>();
        
        if (nombre == null || nombre.isEmpty()) {
            return resultado;
        }
        
        for (Usuario usuario : usuarios.values()) {
            if (usuario.getNombre().toLowerCase().contains(nombre.toLowerCase())) {
                resultado.add(usuario);
            }
        }
        
        return resultado;
    }
    
    public boolean existeUsuario(String id) {
        return usuarios.containsKey(id);
    }
    
    public void eliminarUsuario(String id) throws UsuarioNoEncontradoException {
        if (!existeUsuario(id)) {
            throw new UsuarioNoEncontradoException("No se puede eliminar. Usuario no encontrado: " + id);
        }
        
        usuarios.remove(id);
    }
    
    public int getCantidadUsuarios() {
        return usuarios.size();
    }
    
    // Método para obtener usuarios con más prestamos (para reportes)
    public List<Usuario> getUsuariosMasActivos(int limite) {
        List<Usuario> todos = new ArrayList<>(usuarios.values());
        
        // Ordena por cantidad de préstamos (descendente)
        todos.sort((u1, u2) -> Integer.compare(u2.getCantidadPrestamos(), u1.getCantidadPrestamos()));
        
        // Devuelve solo los primeros "limite" usuarios
        return todos.subList(0, Math.min(limite, todos.size()));
    }
} 