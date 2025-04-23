[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/tc38IXJF)
# 📚 Trabajo Práctico: Sistema de Gestión de Biblioteca Digital (Java 21+)

## 📌 Objetivo General

Desarrollar un sistema de gestión de biblioteca digital que implemente los cinco principios SOLID, programación orientada a objetos, y conceptos avanzados de Java. El sistema deberá manejar diferentes tipos de recursos digitales, préstamos, reservas, y notificaciones en tiempo real.

## 👨‍🎓 Información del Alumno
- **Nombre y Apellido**: Agustin Benavidez
- **Legajo**: 62344

## 📋 Requisitos Adicionales

### Documentación del Sistema
Como parte del trabajo práctico, deberás incluir en este README una guía de uso que explique:

1. **Cómo funciona el sistema**:
   - Descripción general de la arquitectura
   - Explicación de los componentes principales
   - Flujo de trabajo del sistema

2. **Cómo ponerlo en funcionamiento**:
   - Deberás incluir las instrucciones detalladas de puesta en marcha
   - Explicar los requisitos previos necesarios
   - Describir el proceso de compilación
   - Detallar cómo ejecutar la aplicación

3. **Cómo probar cada aspecto desarrollado**:
   - Deberás proporcionar ejemplos de uso para cada funcionalidad implementada
   - Incluir casos de prueba que demuestren el funcionamiento del sistema
   - Describir flujos de trabajo completos que muestren la interacción entre diferentes componentes

La guía debe ser clara, concisa y permitir a cualquier usuario entender y probar el sistema. Se valorará especialmente:
- La claridad de las instrucciones
- La completitud de la documentación
- La organización de la información
- La inclusión de ejemplos prácticos

### Prueba de Funcionalidades

#### 1. Gestión de Recursos
- **Agregar Libro**: 
  - Proceso para agregar un nuevo libro al sistema
  - Verificación de que el libro se agregó correctamente
  - Validación de los datos ingresados

- **Buscar Recurso**:
  - Proceso de búsqueda de recursos
  - Verificación de resultados de búsqueda
  - Manejo de casos donde no se encuentran resultados

- **Listar Recursos**:
  - Visualización de todos los recursos
  - Filtrado por diferentes criterios
  - Ordenamiento de resultados

#### 2. Gestión de Usuarios
- **Registrar Usuario**:
  - Proceso de registro de nuevos usuarios
  - Validación de datos del usuario
  - Verificación del registro exitoso

- **Buscar Usuario**:
  - Proceso de búsqueda de usuarios
  - Visualización de información del usuario
  - Manejo de usuarios no encontrados

#### 3. Préstamos
- **Realizar Préstamo**:
  - Proceso completo de préstamo
  - Verificación de disponibilidad
  - Actualización de estados

- **Devolver Recurso**:
  - Proceso de devolución
  - Actualización de estados
  - Liberación del recurso

#### 4. Reservas
- **Realizar Reserva**:
  - Proceso de reserva de recursos
  - Gestión de cola de reservas
  - Notificación de disponibilidad

#### 5. Reportes
- **Ver Reportes**:
  - Generación de diferentes tipos de reportes
  - Visualización de estadísticas
  - Exportación de datos

#### 6. Alertas
- **Verificar Alertas**:
  - Sistema de notificaciones
  - Diferentes tipos de alertas
  - Gestión de recordatorios

### Ejemplos de Prueba
1. **Flujo Completo de Préstamo**:
   - Registrar un usuario: `Usuario usuario = new Usuario("Juan Perez", "juan@mail.com")`
   - Agregar un libro: `Libro libro = new Libro("L001", "El Quijote", "Miguel de Cervantes", "Literatura")`
   - Realizar un préstamo: `gestorPrestamos.crearPrestamo(usuario.getId(), libro.getIdentificador())`
   - Verificar el estado del recurso: `libro.getEstado()` debería ser `PRESTADO`
   - Devolver el recurso: `gestorPrestamos.devolverPrestamo(prestamoId)`
   - Verificar la actualización del estado: `libro.getEstado()` debería ser `DISPONIBLE`

2. **Sistema de Reservas**:
   - Registrar dos usuarios: `Usuario usuario1 = new Usuario("Ana", "ana@mail.com")` y `Usuario usuario2 = new Usuario("Carlos", "carlos@mail.com")`
   - Agregar un libro: `Libro libro = new Libro("L002", "Harry Potter", "J.K. Rowling", "Literatura")`
   - Préstamo inicial: `gestorPrestamos.crearPrestamo(usuario1.getId(), libro.getIdentificador())`
   - Realizar reservas: `colaReservas.agregarReserva(usuario2.getId(), libro.getIdentificador())`
   - Verificar cola: `colaReservas.obtenerReservas(libro.getIdentificador())` debería mostrar a `usuario2`
   - Procesar devolución: `gestorPrestamos.devolverPrestamo(prestamoId)`
   - Verificar notificación: Se debe generar una notificación para `usuario2`

3. **Alertas y Notificaciones**:
   - Realizar un préstamo: `gestorPrestamos.crearPrestamo(usuarioId, recursoId)`
   - Verificar fecha cercana: Usar `LocalDateTime.now().plusDays(15)` para simular acercamiento al vencimiento
   - Alertas generadas: `servicioNotificaciones.obtenerNotificacionesPendientes(usuarioId)` debería contener alertas
   - Renovar préstamo: `gestorPrestamos.renovarPrestamo(prestamoId, "Necesito más tiempo", validador)`
   - Verificar nueva fecha: `prestamo.getFechaDevolucion()` debería mostrar la fecha extendida

## 🧩 Tecnologías y Herramientas

- Java 21+ (LTS)
- Git y GitHub
- GitHub Projects
- GitHub Issues
- GitHub Pull Requests

## 📘 Etapas del Trabajo

### Etapa 1: Diseño Base y Principios SOLID
- **SRP**: 
  - Crear clase `Usuario` con atributos básicos (nombre, ID, email)
  - Crear clase `RecursoDigital` como clase base abstracta
  - Implementar clase `GestorUsuarios` separada de `GestorRecursos`
  - Cada clase debe tener una única responsabilidad clara
  - Implementar clase `Consola` para manejar la interacción con el usuario

- **OCP**: 
  - Diseñar interfaz `RecursoDigital` con métodos comunes
  - Implementar clases concretas `Libro`, `Revista`, `Audiolibro`
  - Usar herencia para extender funcionalidad sin modificar código existente
  - Ejemplo: agregar nuevo tipo de recurso sin cambiar clases existentes
  - Implementar menú de consola extensible para nuevos tipos de recursos

- **LSP**: 
  - Asegurar que todas las subclases de `RecursoDigital` puedan usarse donde se espera `RecursoDigital`
  - Implementar métodos comunes en la clase base
  - Validar que el comportamiento sea consistente en todas las subclases
  - Crear métodos de visualización en consola para todos los tipos de recursos

- **ISP**: 
  - Crear interfaz `Prestable` para recursos que se pueden prestar
  - Crear interfaz `Renovable` para recursos que permiten renovación
  - Implementar solo las interfaces necesarias en cada clase
  - Diseñar menús de consola específicos para cada tipo de operación

- **DIP**: 
  - Crear interfaz `ServicioNotificaciones`
  - Implementar `ServicioNotificacionesEmail` y `ServicioNotificacionesSMS`
  - Usar inyección de dependencias en las clases que necesitan notificaciones
  - Implementar visualización de notificaciones en consola

### Etapa 2: Gestión de Recursos y Colecciones
- Implementar colecciones:
  - Usar `ArrayList<RecursoDigital>` para almacenar recursos
  - Usar `Map<String, Usuario>` para gestionar usuarios
  - Implementar métodos de búsqueda básicos
  - Crear menú de consola para gestión de recursos

- Crear servicios de búsqueda:
  - Implementar búsqueda por título usando Streams
  - Implementar filtrado por categoría
  - Crear comparadores personalizados para ordenamiento
  - Diseñar interfaz de consola para búsquedas con filtros

- Sistema de categorización:
  - Crear enum `CategoriaRecurso`
  - Implementar método de asignación de categorías
  - Crear búsqueda por categoría
  - Mostrar categorías disponibles en consola

- Manejo de excepciones:
  - Crear `RecursoNoDisponibleException`
  - Crear `UsuarioNoEncontradoException`
  - Implementar manejo adecuado de excepciones en los servicios
  - Mostrar mensajes de error amigables en consola

### Etapa 3: Sistema de Préstamos y Reservas
- Implementar sistema de préstamos:
  - Crear clase `Prestamo` con atributos básicos
  - Implementar lógica de préstamo y devolución
  - Manejar estados de los recursos (disponible, prestado, reservado)
  - Diseñar menú de consola para préstamos

- Sistema de reservas:
  - Crear clase `Reserva` con atributos necesarios
  - Implementar cola de reservas usando `BlockingQueue`
  - Manejar prioridad de reservas
  - Mostrar estado de reservas en consola

- Notificaciones:
  - Implementar sistema básico de notificaciones
  - Crear diferentes tipos de notificaciones
  - Usar `ExecutorService` para enviar notificaciones
  - Mostrar notificaciones en consola

- Concurrencia:
  - Implementar sincronización en operaciones de préstamo
  - Usar `synchronized` donde sea necesario
  - Manejar condiciones de carrera
  - Mostrar estado de operaciones concurrentes en consola

### Etapa 4: Reportes y Análisis
- Generar reportes básicos:
  - Implementar reporte de recursos más prestados
  - Crear reporte de usuarios más activos
  - Generar estadísticas de uso por categoría
  - Diseñar visualización de reportes en consola

- Sistema de alertas:
  - Implementar alertas por vencimiento de préstamos:
    - Crear clase `AlertaVencimiento` que monitorea fechas de devolución
    - Implementar lógica de recordatorios (1 día antes, día del vencimiento)
    - Mostrar alertas en consola con formato destacado
    - Permitir renovación desde la alerta
  
  - Crear notificaciones de disponibilidad:
    - Implementar `AlertaDisponibilidad` para recursos reservados
    - Notificar cuando un recurso reservado está disponible
    - Mostrar lista de recursos disponibles en consola
    - Permitir préstamo inmediato desde la notificación
  
  - Manejar recordatorios automáticos:
    - Implementar sistema de recordatorios periódicos
    - Crear diferentes niveles de urgencia (info, warning, error)
    - Mostrar historial de alertas en consola
    - Permitir configuración de preferencias de notificación

- Concurrencia en reportes:
  - Implementar generación de reportes en segundo plano
  - Usar `ExecutorService` para tareas asíncronas
  - Manejar concurrencia en acceso a datos
  - Mostrar progreso de generación de reportes en consola

## 📋 Detalle de Implementación

### 1. Estructura Base
```java
// Interfaces principales
public interface RecursoDigital {
    String getIdentificador();
    EstadoRecurso getEstado();
    void actualizarEstado(EstadoRecurso estado);
}

public interface Prestable {
    boolean estaDisponible();
    LocalDateTime getFechaDevolucion();
    void prestar(Usuario usuario);
}

public interface Notificable {
    void enviarNotificacion(String mensaje);
    List<Notificacion> getNotificacionesPendientes();
}

// Clase base abstracta
public abstract class RecursoBase implements RecursoDigital, Prestable {
    // Implementación común
}
```

### 2. Gestión de Biblioteca
```java
public class GestorBiblioteca {
    private final Map<String, RecursoDigital> recursos;
    private final List<Prestamo> prestamos;
    private final ExecutorService notificador;
    // Implementación de gestión
}
```

### 3. Sistema de Préstamos
```java
public class SistemaPrestamos {
    private final BlockingQueue<SolicitudPrestamo> colaSolicitudes;
    private final ExecutorService procesadorPrestamos;
    // Implementación de préstamos
}
```

## ✅ Entrega y Flujo de Trabajo con GitHub

1. **Configuración del Repositorio**
   - Proteger la rama `main`
   - Crear template de Issues y Pull Requests

2. **Project Kanban**
   - `To Do`
   - `In Progress`
   - `Code Review`
   - `Done`

3. **Milestones**
   - Etapa 1: Diseño Base
   - Etapa 2: Gestión de Recursos
   - Etapa 3: Sistema de Préstamos
   - Etapa 4: Reportes

4. **Issues y Pull Requests**
   - Crear Issues detallados para cada funcionalidad
   - Asociar cada Issue a un Milestone
   - Implementar en ramas feature
   - Revisar código antes de merge

## 📝 Ejemplo de Issue

### Título
Implementar sistema de préstamos concurrente

### Descripción
Crear el sistema de préstamos que utilice hilos y el patrón productor-consumidor para procesar solicitudes de préstamo en tiempo real.

#### Requisitos
- Implementar `BlockingQueue` para solicitudes de préstamo
- Crear procesador de solicitudes usando `ExecutorService`
- Implementar sistema de notificaciones
- Asegurar thread-safety en operaciones de préstamo

#### Criterios de Aceptación
- [ ] Sistema procesa préstamos concurrentemente
- [ ] Manejo adecuado de excepciones
- [ ] Documentación de diseño

### Labels
- `enhancement`
- `concurrency`

## ✅ Requisitos para la Entrega

- ✅ Implementación completa de todas las etapas
- ✅ Código bien documentado
- ✅ Todos los Issues cerrados
- ✅ Todos los Milestones completados
- ✅ Pull Requests revisados y aprobados
- ✅ Project actualizado

> ⏰ **Fecha de vencimiento**: 23/04/2025 a las 13:00 hs

## 📚 Recursos Adicionales

- Documentación oficial de Java 21
- Guías de estilo de código
- Ejemplos de implementación concurrente
- Patrones de diseño aplicados

## 📝 Consideraciones Éticas

### Uso de Inteligencia Artificial
El uso de herramientas de IA en este trabajo práctico debe seguir las siguientes pautas:

1. **Transparencia**
   - Documentar claramente qué partes del código fueron generadas con IA
   - Explicar las modificaciones realizadas al código generado
   - Mantener un registro de las herramientas utilizadas

2. **Aprendizaje**
   - La IA debe usarse como herramienta de aprendizaje, no como reemplazo
   - Comprender y ser capaz de explicar el código generado
   - Utilizar la IA para mejorar la comprensión de conceptos

3. **Integridad Académica**
   - El trabajo final debe reflejar tu aprendizaje y comprensión personal
   - No se permite la presentación de código generado sin comprensión
   - Debes poder explicar y defender cualquier parte del código

4. **Responsabilidad**
   - Verificar la corrección y seguridad del código generado
   - Asegurar que el código cumple con los requisitos del proyecto
   - Mantener la calidad y estándares de código establecidos

5. **Desarrollo Individual**
   - La IA puede usarse para facilitar tu proceso de aprendizaje
   - Documentar tu proceso de desarrollo y decisiones tomadas
   - Mantener un registro de tu progreso y aprendizaje

### Consecuencias del Uso Inadecuado
El uso inadecuado de IA puede resultar en:
- Calificación reducida o nula
- Sanciones académicas
- Pérdida de oportunidades de aprendizaje
- Impacto negativo en tu desarrollo profesional

## 📝 Licencia

Este trabajo es parte del curso de Programación Avanzada de Ingeniería en Informática. Uso educativo únicamente.

# Sistema de Gestión de Biblioteca Digital

Este proyecto implementa un sistema completo de gestión de biblioteca que permite administrar préstamos, reservas, renovaciones y notificaciones para diferentes tipos de recursos.

## Características principales

- Gestión de usuarios
- Gestión de recursos (libros, multimedia)
- Sistema de préstamos y devoluciones
- Cola de reservas para recursos no disponibles
- Renovación de préstamos con reglas según categoría
- Historial de renovaciones
- Reportes y estadísticas
- Notificaciones a usuarios

## Requisitos

- Java 21 o superior
- Maven para gestión de dependencias

## Instrucciones de uso

### Ejecución del sistema

Para ejecutar el sistema, compile el proyecto y ejecute la clase `Main`:

```bash
# Compilar el proyecto
javac -d bin src/main/java/sistema/biblioteca/Main.java

# Ejecutar el sistema
java -cp bin sistema.biblioteca.Main
```

### Menú principal

Al iniciar el sistema, se mostrará un menú con las siguientes opciones:

1. **Iniciar sistema interactivo**: Accede al sistema completo con todas las funcionalidades
2. **Ejecutar demostración**: Ejecuta un escenario automático para mostrar el funcionamiento
3. **Salir**: Cierra la aplicación

### Sistema interactivo

Si selecciona la opción 1, accederá al sistema interactivo que ofrece las siguientes funcionalidades:

1. **Mostrar Usuarios**: Muestra los usuarios registrados en el sistema
2. **Mostrar Recursos**: Lista todos los recursos disponibles
3. **Realizar Préstamo**: Permite registrar un nuevo préstamo
4. **Devolver Préstamo**: Registra la devolución de un recurso
5. **Renovar Préstamo**: Extiende la fecha de devolución de un préstamo
6. **Mostrar Préstamos**: Muestra diferentes filtros de préstamos
7. **Realizar Reserva**: Reserva un recurso que no está disponible
8. **Mostrar Estadísticas**: Muestra datos y reportes del sistema
9. **Ejecutar Escenario Completo**: Ejecuta una demostración automática
0. **Salir**: Regresa al menú principal

## Flujos de uso principales

### Préstamo de un recurso

1. Seleccione la opción 3 "Realizar Préstamo"
2. Seleccione el ID del usuario
3. Seleccione el ID del recurso disponible
4. El sistema registrará el préstamo y asignará una fecha de devolución

### Devolución de un recurso

1. Seleccione la opción 4 "Devolver Préstamo"
2. Elija el ID del préstamo que desea devolver
3. El sistema registrará la devolución y actualizará el estado del recurso

### Renovación de un préstamo

1. Seleccione la opción 5 "Renovar Préstamo"
2. Elija el ID del préstamo que desea renovar
3. Introduzca el motivo de la renovación
4. El sistema validará si es posible renovar según las reglas
5. Si es posible, se extenderá la fecha de devolución

### Reserva de un recurso

1. Seleccione la opción 7 "Realizar Reserva"
2. Elija el ID del usuario que realiza la reserva
3. Seleccione el ID del recurso que desea reservar
4. Si el recurso no está disponible, se registrará en la cola de reservas

## Reglas de renovación

El sistema aplica diferentes reglas según la categoría del recurso:

- **Académico**: Máximo 3 renovaciones de 10 días cada una
- **Literatura**: Máximo 2 renovaciones de 7 días cada una
- **Histórico**: Máximo 1 renovación de 5 días (requiere autorización)
- **Referencia**: Máximo 1 renovación de 3 días

Además, existen restricciones generales:
- No se permite renovar préstamos vencidos
- No se permite renovar si hay reservas pendientes para ese recurso
- No se puede renovar un préstamo ya devuelto

## Limitaciones conocidas

El sistema actual tiene algunas limitaciones que podrían mejorarse en versiones futuras:

1. **Persistencia de datos**: Actualmente el sistema almacena los datos en memoria, por lo que se pierden al cerrar la aplicación. Una mejora futura sería implementar almacenamiento en base de datos.

2. **Interfaz gráfica**: El sistema funciona por línea de comandos. Se podría mejorar la experiencia de usuario implementando una interfaz gráfica.

3. **Notificaciones reales**: Las notificaciones solo se muestran en consola. En una versión futura podrían enviarse por email o SMS.

## Componentes del sistema

- `GestorUsuarios`: Administra los usuarios registrados
- `GestorRecursos`: Maneja los diferentes tipos de recursos
- `GestorPrestamos`: Controla los préstamos y devoluciones
- `GestorReglaRenovacion`: Define las reglas de renovación
- `ValidadorRenovaciones`: Valida las condiciones para renovar
- `ColaReservas`: Gestiona las reservas de recursos
- `ReportesPrestamos`: Genera estadísticas y reportes

## Contacto

Para más información, contacte con el equipo de desarrollo o directamente al desarrollador:
- Email: agustin.benavidez@alumno.um.edu.ar