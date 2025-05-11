# Correcciones y Recomendaciones - Sistema de Gestión de Biblioteca Digital

## 📋 Resumen General

El trabajo implementa un sistema de gestión de biblioteca digital que cumple con los requisitos básicos establecidos. El estudiante demuestra un buen entendimiento de los conceptos fundamentales de programación orientada a objetos, especialmente en:

- Uso de clases e interfaces para modelar recursos y servicios
- Implementación de herencia y polimorfismo en la jerarquía de recursos
- Separación básica de responsabilidades en gestores
- Manejo básico de excepciones y validaciones

El código está organizado en paquetes lógicos (modelos, gestores, servicios) lo que facilita su comprensión y mantenimiento.

## 🎯 Aspectos Positivos

1. **Implementación de Interfaces**
   ```java
   // Buena definición de interfaces específicas
   public interface Prestable {
       boolean estaDisponible();
       void marcarComoPrestado();
       void marcarComoDisponible();
   }

   public interface RecursoDigital {
       String getTitulo();
       String getAutor();
       EstadoRecurso getEstado();
   }
   ```
   - Las interfaces son pequeñas y tienen un propósito claro
   - Facilita la extensión del sistema para nuevos tipos de recursos

2. **Manejo de Herencia**
   ```java
   public abstract class RecursoBase implements Prestable, RecursoDigital {
       protected String titulo;
       protected String autor;
       protected EstadoRecurso estado;
       protected Usuario usuarioActual;

       public RecursoBase(String titulo, String autor) {
           this.titulo = titulo;
           this.autor = autor;
           this.estado = EstadoRecurso.DISPONIBLE;
       }

       @Override
       public boolean estaDisponible() {
           return estado == EstadoRecurso.DISPONIBLE;
       }
   }
   ```
   - Buena abstracción de atributos y comportamientos comunes
   - Implementación clara de interfaces

3. **Gestión de Estado**
   ```java
   public class GestorPrestamos {
       private final List<Prestamo> prestamosActivos;
       private final ServicioNotificaciones servicioNotificaciones;

       public Prestamo crearPrestamo(RecursoBase recurso, Usuario usuario) {
           if (!recurso.estaDisponible()) {
               throw new RecursoNoDisponibleException(
                   "El recurso no está disponible para préstamo"
               );
           }
           // Lógica de préstamo
       }
   }
   ```
   - Uso apropiado de colecciones para manejar datos
   - Validaciones básicas implementadas

## 🔧 Áreas de Mejora

### 1. Separación de Responsabilidades (SRP)

#### Problema Actual
```java
// En GestorPrestamos.java
public Prestamo crearPrestamo(RecursoBase recurso, Usuario usuario) {
    // Se mezclan múltiples responsabilidades
    recurso.setEstado(EstadoRecurso.PRESTADO);
    usuario.incrementarContadorPrestamos();
    servicioNotificaciones.enviarNotificacion(
        usuario, 
        "Se ha creado un nuevo préstamo"
    );
    return new Prestamo(recurso, usuario, 
        LocalDateTime.now(), 
        LocalDateTime.now().plusDays(14)
    );
}
```

#### Mejora Sugerida
```java
public class GestorPrestamos {
    private final GestorEstadoRecurso gestorEstado;
    private final GestorContadorUsuario gestorContador;
    private final ServicioNotificaciones notificaciones;

    public Prestamo crearPrestamo(RecursoBase recurso, Usuario usuario) {
        validarDisponibilidad(recurso);
        actualizarEstadoRecurso(recurso);
        actualizarContadorUsuario(usuario);
        notificarPrestamo(usuario);
        return crearRegistroPrestamo(recurso, usuario);
    }

    private void actualizarEstadoRecurso(RecursoBase recurso) {
        gestorEstado.marcarComoPrestado(recurso);
    }

    private void actualizarContadorUsuario(Usuario usuario) {
        gestorContador.incrementarPrestamos(usuario);
    }

    private void notificarPrestamo(Usuario usuario) {
        notificaciones.enviarNotificacion(
            usuario, 
            "Se ha creado un nuevo préstamo"
        );
    }
}
```

### 2. Validaciones y Manejo de Excepciones

#### Problema Actual
```java
// En GestorRecursos.java
public void agregarRecurso(RecursoBase recurso) {
    if (recurso == null) {
        return; // Validación silenciosa
    }
    recursos.add(recurso);
}

// En GestorUsuarios.java
public Usuario buscarUsuario(String id) {
    for (Usuario u : usuarios) {
        if (u.getId().equals(id)) {
            return u;
        }
    }
    return null; // Retorno silencioso
}
```

#### Mejora Sugerida
```java
public class GestorRecursos {
    public void agregarRecurso(RecursoBase recurso) {
        if (recurso == null) {
            throw new RecursoInvalidoException(
                "El recurso no puede ser null"
            );
        }
        if (recurso.getTitulo() == null || 
            recurso.getTitulo().trim().isEmpty()) {
            throw new RecursoInvalidoException(
                "El título del recurso no puede estar vacío"
            );
        }
        recursos.add(recurso);
    }
}

public class GestorUsuarios {
    public Usuario buscarUsuario(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new UsuarioInvalidoException(
                "El ID de usuario no puede estar vacío"
            );
        }
        return usuarios.stream()
            .filter(u -> u.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new UsuarioNoEncontradoException(
                "No se encontró usuario con ID: " + id
            ));
    }
}
```

### 3. Lógica de Negocio Mezclada

#### Problema Actual
```java
// En Reserva.java
public boolean haExpirado() {
    if (this.estado != EstadoReserva.PENDIENTE) {
        return false;
    }
    return LocalDateTime.now().isAfter(fechaExpiracion);
}
```

#### Mejora Sugerida
```java
public class Reserva {
    public boolean haExpirado() {
        return estaPendiente() && fechaExpirada();
    }

    private boolean estaPendiente() {
        return this.estado == EstadoReserva.PENDIENTE;
    }

    private boolean fechaExpirada() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }
}
```

### 4. Inyección de Dependencias

#### Problema Actual
```java
// En DemoSistemaBiblioteca.java
private void inicializarComponentes() {
    this.servicioNotificaciones = new ServicioNotificaciones();
    this.gestorUsuarios = new GestorUsuarios();
    this.gestorRecursos = new GestorRecursos();
}
```

#### Mejora Sugerida
```java
public class DemoSistemaBiblioteca {
    private final ServicioNotificaciones servicioNotificaciones;
    private final GestorUsuarios gestorUsuarios;
    private final GestorRecursos gestorRecursos;

    public DemoSistemaBiblioteca(
        ServicioNotificaciones servicioNotificaciones,
        GestorUsuarios gestorUsuarios,
        GestorRecursos gestorRecursos
    ) {
        this.servicioNotificaciones = servicioNotificaciones;
        this.gestorUsuarios = gestorUsuarios;
        this.gestorRecursos = gestorRecursos;
    }
}
```

### 5. Gestión de Notificaciones

#### Problema Actual
```java
// En Usuario.java
public void enviarNotificacion(Notificacion notificacion) {
    this.notificaciones.add(notificacion);
    System.out.println(
        "Notificación enviada a " + 
        this.nombre + ": " + 
        notificacion.getMensaje()
    );
}
```

#### Mejora Sugerida
```java
public class Usuario {
    private final List<Notificacion> notificaciones;
    private final ServicioNotificaciones servicioNotificaciones;

    public void enviarNotificacion(Notificacion notificacion) {
        almacenarNotificacion(notificacion);
        servicioNotificaciones.enviar(
            this, 
            notificacion.getMensaje()
        );
    }

    private void almacenarNotificacion(Notificacion notificacion) {
        this.notificaciones.add(notificacion);
    }
}
```

## 📈 Sugerencias de Mejora

### 1. Implementar Patrón Observer para Notificaciones
```java
public interface ObservadorNotificacion {
    void actualizar(Notificacion notificacion);
}

public class Usuario implements ObservadorNotificacion {
    @Override
    public void actualizar(Notificacion notificacion) {
        almacenarNotificacion(notificacion);
    }
}

public class ServicioNotificaciones {
    private final List<ObservadorNotificacion> observadores;

    public void notificar(Notificacion notificacion) {
        for (ObservadorNotificacion obs : observadores) {
            obs.actualizar(notificacion);
        }
    }
}
```

### 2. Implementar Patrón Strategy para Búsquedas
```java
public interface EstrategiaBusqueda {
    List<RecursoBase> buscar(
        List<RecursoBase> recursos, 
        String criterio
    );
}

public class BusquedaPorTitulo implements EstrategiaBusqueda {
    @Override
    public List<RecursoBase> buscar(
        List<RecursoBase> recursos, 
        String titulo
    ) {
        return recursos.stream()
            .filter(r -> r.getTitulo()
                .toLowerCase()
                .contains(titulo.toLowerCase()))
            .collect(Collectors.toList());
    }
}

public class BusquedaPorAutor implements EstrategiaBusqueda {
    @Override
    public List<RecursoBase> buscar(
        List<RecursoBase> recursos, 
        String autor
    ) {
        return recursos.stream()
            .filter(r -> r.getAutor()
                .toLowerCase()
                .contains(autor.toLowerCase()))
            .collect(Collectors.toList());
    }
}

// Uso en GestorRecursos
public class GestorRecursos {
    private EstrategiaBusqueda estrategiaBusqueda;

    public void setEstrategiaBusqueda(EstrategiaBusqueda estrategia) {
        this.estrategiaBusqueda = estrategia;
    }

    public List<RecursoBase> buscarRecursos(String criterio) {
        return estrategiaBusqueda.buscar(recursos, criterio);
    }
}
```

### 3. Implementar Patrón Factory para Creación de Recursos
```java
public class RecursoFactory {
    public static RecursoBase crearRecurso(
        TipoRecurso tipo, 
        String titulo, 
        String autor
    ) {
        return switch (tipo) {
            case LIBRO -> new Libro(titulo, autor);
            case AUDIOLIBRO -> new Audiolibro(titulo, autor);
            case REVISTA -> new Revista(titulo, autor);
            default -> throw new TipoRecursoInvalidoException(
                "Tipo de recurso no soportado: " + tipo
            );
        };
    }
}

// Uso en GestorRecursos
public class GestorRecursos {
    public void agregarRecurso(
        TipoRecurso tipo,
        String titulo,
        String autor
    ) {
        RecursoBase recurso = RecursoFactory.crearRecurso(
            tipo, titulo, autor
        );
        validarRecurso(recurso);
        recursos.add(recurso);
    }
}
```

### 4. Implementar Patrón Decorator para Notificaciones
```java
public abstract class DecoradorNotificacion implements ServicioNotificaciones {
    protected final ServicioNotificaciones servicio;

    public DecoradorNotificacion(ServicioNotificaciones servicio) {
        this.servicio = servicio;
    }

    @Override
    public void enviarNotificacion(Usuario usuario, String mensaje) {
        servicio.enviarNotificacion(usuario, mensaje);
    }
}

public class NotificacionConRegistro extends DecoradorNotificacion {
    private final List<String> historial;

    public NotificacionConRegistro(
        ServicioNotificaciones servicio,
        List<String> historial
    ) {
        super(servicio);
        this.historial = historial;
    }

    @Override
    public void enviarNotificacion(Usuario usuario, String mensaje) {
        String notificacion = String.format(
            "[%s] %s: %s",
            LocalDateTime.now(),
            usuario.getNombre(),
            mensaje
        );
        historial.add(notificacion);
        super.enviarNotificacion(usuario, mensaje);
    }
}
```

### 5. Implementar Patrón Command para Operaciones
```java
public interface Comando {
    void ejecutar();
    void deshacer();
}

public class ComandoPrestamo implements Comando {
    private final GestorPrestamos gestor;
    private final RecursoBase recurso;
    private final Usuario usuario;
    private Prestamo prestamo;

    public ComandoPrestamo(
        GestorPrestamos gestor,
        RecursoBase recurso,
        Usuario usuario
    ) {
        this.gestor = gestor;
        this.recurso = recurso;
        this.usuario = usuario;
    }

    @Override
    public void ejecutar() {
        prestamo = gestor.crearPrestamo(recurso, usuario);
    }

    @Override
    public void deshacer() {
        if (prestamo != null) {
            gestor.devolverPrestamo(prestamo);
        }
    }
}

// Uso en GestorPrestamos
public class GestorPrestamos {
    private final List<Comando> historialComandos = new ArrayList<>();

    public void ejecutarComando(Comando comando) {
        comando.ejecutar();
        historialComandos.add(comando);
    }

    public void deshacerUltimoComando() {
        if (!historialComandos.isEmpty()) {
            Comando ultimoComando = historialComandos.remove(
                historialComandos.size() - 1
            );
            ultimoComando.deshacer();
        }
    }
}
```

## 📊 Conclusión

El trabajo demuestra un buen entendimiento de los conceptos fundamentales de programación orientada a objetos. La implementación es funcional y sigue buenas prácticas en general, aunque hay áreas de mejora que podrían fortalecer la calidad del código y su mantenibilidad.

### Calificación Detallada

- **Diseño POO**: 8/10
  - ✅ Buena implementación de interfaces
  - ✅ Uso apropiado de herencia
  - ✅ Encapsulamiento adecuado
  - ⚠️ Algunas clases podrían ser más cohesivas

- **Principios SOLID**: 7/10
  - ✅ Buen uso de interfaces (ISP)
  - ✅ Herencia bien implementada (LSP)
  - ⚠️ Mejorable en SRP (métodos con múltiples responsabilidades)
  - ⚠️ Mejorable en DIP (algunas dependencias no inyectadas)

- **Claridad y Robustez**: 6/10
  - ✅ Validaciones básicas implementadas
  - ⚠️ Manejo de excepciones mejorable
  - ⚠️ Lógica de negocio mezclada en algunos casos
  - ⚠️ Falta de documentación en algunos métodos

- **Funcionalidad**: 9/10
  - ✅ Cumple todos los requisitos básicos
  - ✅ Implementación creativa de cola de reservas
  - ✅ Sistema de notificaciones funcional
  - ✅ Buena organización de paquetes

**Nota Final**: 7.5/10

### Próximos Pasos Recomendados

1. **Refactorizar GestorPrestamos**
   ```java
   // Implementar la separación de responsabilidades
   // siguiendo el ejemplo de mejora proporcionado
   public class GestorPrestamos {
       private final GestorEstadoRecurso gestorEstado;
       private final GestorContadorUsuario gestorContador;
       // ... resto de la implementación
   }
   ```

2. **Mejorar Validaciones**
   ```java
   // Implementar excepciones específicas
   public class RecursoInvalidoException extends RuntimeException {
       public RecursoInvalidoException(String mensaje) {
           super(mensaje);
       }
   }
   // Y validaciones más robustas
   public void validarRecurso(RecursoBase recurso) {
       if (recurso == null) {
           throw new RecursoInvalidoException("El recurso no puede ser null");
       }
       // ... más validaciones
   }
   ```

3. **Refactorizar Reserva**
   ```java
   // Separar lógica de estado y fechas
   public class Reserva {
       public boolean haExpirado() {
           return estaPendiente() && fechaExpirada();
       }
       // ... métodos privados auxiliares
   }
   ```

4. **Implementar Inyección de Dependencias**
   ```java
   // Modificar DemoSistemaBiblioteca
   public class DemoSistemaBiblioteca {
       private final ServicioNotificaciones notificaciones;
       private final GestorUsuarios usuarios;
       // ... constructor con inyección
   }
   ```

5. **Mejorar Sistema de Notificaciones**
   ```java
   // Implementar patrón Observer
   public interface ObservadorNotificacion {
       void actualizar(Notificacion notificacion);
   }
   // ... implementación en Usuario
   ```

6. **Implementar Patrones de Diseño**
   ```java
   // Agregar Factory para recursos
   public class RecursoFactory {
       public static RecursoBase crearRecurso(TipoRecurso tipo, ...) {
           // ... implementación
       }
   }
   // Y Strategy para búsquedas
   public interface EstrategiaBusqueda {
       List<RecursoBase> buscar(List<RecursoBase> recursos, String criterio);
   }
   ```

7. **Agregar Documentación**
   ```java
   /**
    * Gestiona las operaciones de préstamo de recursos.
    * Implementa la lógica de negocio para crear, renovar y devolver préstamos.
    */
   public class GestorPrestamos {
       /**
        * Crea un nuevo préstamo para el recurso y usuario especificados.
        * @param recurso El recurso a prestar
        * @param usuario El usuario que solicita el préstamo
        * @return El préstamo creado
        * @throws RecursoNoDisponibleException si el recurso no está disponible
        */
       public Prestamo crearPrestamo(RecursoBase recurso, Usuario usuario) {
           // ... implementación
       }
   }
   ```

8. **Implementar Tests**
   ```java
   @Test
   public void testCrearPrestamo() {
       // Arrange
       RecursoBase recurso = new Libro("Título", "Autor");
       Usuario usuario = new Usuario("ID", "Nombre");
       GestorPrestamos gestor = new GestorPrestamos(...);

       // Act
       Prestamo prestamo = gestor.crearPrestamo(recurso, usuario);

       // Assert
       assertNotNull(prestamo);
       assertEquals(EstadoRecurso.PRESTADO, recurso.getEstado());
       assertEquals(1, usuario.getContadorPrestamos());
   }
   ```

### Recomendaciones Adicionales

1. **Considerar la implementación de logs**
   - Agregar logging para operaciones críticas
   - Facilitar el debugging y monitoreo

2. **Mejorar la gestión de errores**
   - Crear una jerarquía de excepciones
   - Implementar manejo de errores consistente

3. **Optimizar el rendimiento**
   - Revisar el uso de colecciones
   - Considerar el uso de índices para búsquedas frecuentes

4. **Mejorar la interfaz de usuario**
   - Agregar más opciones de menú
   - Mejorar los mensajes al usuario

5. **Implementar persistencia**
   - Considerar la serialización de datos
   - Preparar para futura integración con base de datos

El trabajo demuestra un buen nivel de comprensión de los conceptos de POO y SOLID, y con las mejoras sugeridas, podría convertirse en un excelente ejemplo de buenas prácticas de programación. 