# SysTurismo

Sistema de escritorio para una agencia de turismo. Registra destinos, guías, paquetes, turistas y reservas con sus pagos. Proyecto final del curso de Programación Orientada a Objetos (Evaluación de la Unidad 2).

Está hecho con Java 21, JavaFX, JDBC y H2 (base de datos en un archivo local), siguiendo la estructura de clases de la Sesión 11: repositorio, servicio y controlador.

## Cómo ejecutarlo

Requisitos: JDK 21. Maven viene incluido con el wrapper del proyecto.

```
./mvnw clean javafx:run
```

En Windows: `mvnw.cmd clean javafx:run`. También se puede abrir la carpeta en IntelliJ IDEA y ejecutar la clase `pe.edu.upeu.systurismo.SysTurismo`.

La primera vez se crea el archivo `data/systurismo.mv.db` con las tablas y datos de demostración (7 destinos, 4 guías, 8 paquetes, 8 turistas y 9 reservas con sus pagos). Para empezar de cero basta con borrar la carpeta `data`.

## Usuarios de demostración

| Usuario | Contraseña | Perfil |
|---|---|---|
| admin | Admin2026! | Root (ve todos los módulos) |
| agente1 | Agente2026! | Agente (inicio, reservas y turistas) |

Las contraseñas se guardan cifradas. Cambie estas dos antes de usar el sistema fuera de la clase.

## Estructura

```
src/main/java/pe/edu/upeu/systurismo
├── config        AppContext (inyección de dependencias), DatabaseConfig, DataInitializer
├── controller    Pantallas JavaFX (CrudBaseController y una clase por pantalla)
├── service       Interfaces y reglas de negocio (service/impl)
├── repository    Acceso a datos con JDBC (AbstractJpaRepository y uno por tabla)
├── model         Entidades con anotaciones de validación
├── enums         Estados, tipos y métodos de pago
├── dto           Datos para el panel, menú y sesión
├── components    Toast, tabla, paginación, tooltips de error
├── exception     ReglaNegocioException, ValidacionException, ModelNotFoundException
└── utils         PasswordHasher, ValidadorEntidades, FxUtil
```

Las pantallas están en `src/main/resources/view` (FXML) y el diseño en `css/turismo.css`.

## Documentación

- `docs/CASO_Y_REQUERIMIENTOS.md`: descripción del caso y lista de requerimientos.
- `docs/MODELO_DATOS.md` y `docs/modelo_er.png`: modelo de datos.
- `docs/schema_turismo.sql`: script de las tablas (el mismo que ejecuta la aplicación).
- `docs/GUIA_DEFENSA.md`: cómo se relaciona el proyecto con la rúbrica y qué puede preguntar el docente.
