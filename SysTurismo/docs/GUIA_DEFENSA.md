# Guía para la presentación

Este documento relaciona lo que hay en el código con cada criterio de la rúbrica y reúne preguntas que el docente puede hacer, con una respuesta apoyada en el proyecto. Conviene abrir cada archivo mencionado antes de la presentación y leerlo una vez: es más fácil explicar código que se ha visto reciente.

## Qué se pide y dónde está

### Arquitectura en capas

La aplicación tiene tres capas y cada una solo habla con la siguiente.

- Presentación: `controller/` y `resources/view/`. Lee los campos, llama al servicio y muestra el resultado. No tiene SQL ni reglas del negocio.
- Lógica de negocio: `service/` y `service/impl/`. Valida, aplica las reglas (cupos, 30% de confirmación, duplicados) y decide qué se guarda.
- Acceso a datos: `repository/`. Es el único lugar con SQL. Hace de DAO.

Para hablar de SOLID con ejemplos concretos:

- Responsabilidad única: `ReservaRepository` solo sabe de SQL, `ReservaServiceImp` solo de reglas, `ReservaController` solo de pantalla.
- Abierto/cerrado: para agregar una tabla nueva no se modifica `AbstractJpaRepository` ni `CrudBaseController`; se crean clases que los extienden.
- Sustitución de Liskov: cualquier repositorio se puede usar donde se espera `ICrudGenericoRepository`.
- Segregación de interfaces: `IReservaService` no extiende el CRUD genérico porque una reserva no se edita ni se elimina. Si lo hiciera, tendría métodos que no debe ofrecer.
- Inversión de dependencias: los controladores dependen de interfaces (`IPaqueteService`), no de las clases concretas. `AppContext` decide qué implementación se entrega.

### Interacción con la base de datos

- Conexión con pool (HikariCP) en `DatabaseConfig`.
- Consultas con `PreparedStatement` en `SqlHelper`.
- Dos transacciones en `ReservaRepository`: `registrarConPago` guarda la reserva y su adelanto juntos, y `registrarPago` guarda el pago y cambia el estado. Si algo falla se hace `rollback` y no queda nada a medias.
- Control de cupos con `SELECT ... FOR UPDATE` sobre el paquete dentro de la transacción. Bloquea esa fila hasta el `commit`, así dos agentes que reserven el último cupo al mismo tiempo no pueden vender ambos.
- Manejo de excepciones: `SqlHelper.traducir` convierte los errores de H2 (valor duplicado, registro relacionado) en `ReglaNegocioException` con un mensaje claro.
- Paginación en la base de datos con `LIMIT` y `OFFSET` en `AbstractJpaRepository.findPage`. Solo viajan los registros de la página visible.

### Interfaz gráfica

Menú lateral por perfil (`NavegacionServiceImp`), panel con indicadores y gráficos, formularios con campos que no aceptan caracteres inválidos, tablas con etiquetas de estado, búsqueda, paginación y mensajes emergentes. Los estilos están en `css/turismo.css`.

### CRUD

Destinos, guías, paquetes, turistas y usuarios usan `CrudBaseController` (patrón Template Method). La clase base hace lo común: listar, buscar, paginar, guardar o actualizar, eliminar con confirmación y mostrar errores. Cada pantalla solo define sus campos y cómo convertirlos en entidad. Las reservas tienen su propia pantalla porque su flujo es distinto.

### Validación de campos con mensaje (4 puntos)

Las reglas están en las anotaciones de los modelos (`@NotBlank`, `@Size`, `@Pattern`, `@Email`, `@Min`, `@DecimalMin`). `ValidadorEntidades` las revisa en la capa de servicio. Las que dependen de varios campos o de otros registros están en `reglasDeNegocio` de cada servicio. Si algo falla, el servicio lanza `ValidacionException` con un mensaje por campo, y la pantalla marca ese campo en rojo con un tooltip, muestra el primer mensaje y abre un aviso. Al terminar una acción aparece un mensaje de éxito o de error.

### Investigación adicional

1. Contraseñas con PBKDF2 (`utils/PasswordHasher`). Se usa una sal aleatoria por usuario, 210 000 iteraciones y comparación en tiempo constante. Solo usa clases del JDK.
2. Paginación y búsqueda con consulta diferida de 300 ms (`PaginationBar`, `AbstractJpaRepository`).
3. Bloqueo de fila para evitar sobreventa de cupos.

## Preguntas que pueden hacer

**¿Por qué no usa JPA?** La rúbrica acepta DAO o JPA. Se usó el patrón DAO con JDBC puro, como en la Sesión 11, para controlar el SQL y las transacciones de forma explícita. Con JPA habría menos código, pero las transacciones y los bloqueos quedarían menos visibles.

**¿Qué es la inyección de dependencias y dónde está?** Es recibir los objetos que una clase necesita en vez de crearlos dentro. Los servicios reciben su repositorio por constructor y los controladores reciben sus servicios. `AppContext` los crea y los conecta. `FXMLLoader` pide los controladores a `AppContext` con `setControllerFactory`.

**¿Por qué los controladores se crean cada vez y los servicios no?** Un servicio no guarda estado de pantalla, así que una sola instancia sirve. Un controlador guarda lo que hay en el formulario; si se reutilizara, al volver a abrir la pantalla conservaría datos de la vez anterior.

**¿Qué hace `PreparedStatement` y por qué se usa?** Separa el SQL de los valores. Los valores nunca se interpretan como SQL, lo que evita la inyección SQL.

**¿Qué es una transacción? Muestre una.** Un grupo de operaciones que se guardan todas o ninguna. En `registrarConPago` se desactiva el autocommit, se inserta la reserva y el pago, y se hace `commit`. Si ocurre un error, `rollback`.

**¿Qué pasa si dos agentes reservan el último cupo a la vez?** La primera transacción bloquea la fila del paquete con `FOR UPDATE`. La segunda espera, y cuando recibe el turno ya ve los cupos descontados y falla con el mensaje de falta de cupos.

**¿Por qué las contraseñas se cifran con hash y no con cifrado reversible?** Con hash no existe forma de recuperar la clave original, solo de comprobar si una clave dada produce el mismo resultado. Si alguien roba la base, no obtiene las claves. La sal aleatoria evita que dos usuarios con la misma clave tengan el mismo hash y vuelve inútiles las tablas precalculadas. Las iteraciones hacen lento cada intento, lo que dificulta probar claves por fuerza bruta.

**¿Por qué `BigDecimal` y no `double` para el dinero?** `double` representa mal algunos decimales (0.1 + 0.2 no da exactamente 0.3) y los errores se acumulan. `BigDecimal` es exacto.

**¿Dónde se valida y por qué ahí?** En el servicio. Si la validación estuviera solo en la pantalla, otra pantalla, una prueba o una carga de datos podrían saltársela. Aquí también la usa `DataInitializer` al cargar los datos de demostración.

**¿Por qué una reserva no se elimina?** Tiene pagos asociados y es un registro de lo que ocurrió. Se cancela y queda su historial; los cupos se liberan porque las canceladas no cuentan.

**¿Qué patrones de diseño usó?** Template Method (`AbstractJpaRepository`, `CrudBaseController`), DAO (repositorios), Singleton (`AppContext`, `SessionManager`), Factory (las fábricas de controladores en `AppContext`), Builder (Lombok en los modelos).

**¿Qué diferencia hay entre modelo y DTO?** El modelo representa una tabla del negocio (`Turista`). El DTO transporta datos armados para una pantalla (`ResumenDashboard`) que no corresponden a una sola tabla.

**¿Cómo funciona la paginación?** La barra pide una página y un tamaño. El servicio llama a `findPage`, que cuenta los registros que cumplen el filtro y trae solo los de esa página con `LIMIT` y `OFFSET`. La barra muestra el total y habilita o deshabilita los botones.

**¿Cómo se agregaría un módulo nuevo, por ejemplo Hoteles?** Tabla en el script SQL, modelo, repositorio que extienda `AbstractJpaRepository`, interfaz y servicio, un controlador que extienda `CrudBaseController`, un FXML, el registro en `AppContext` y una línea en `NavegacionServiceImp`.

## Antes de presentar

1. Ejecutar la aplicación y recorrer cada pantalla: crear, editar, eliminar, buscar, paginar, guardar con campos vacíos y con datos repetidos.
2. Probar una reserva sin adelanto, otra con el 30% o más, un pago posterior, una cancelación y un intento de superar el cupo.
3. Entrar con el usuario `agente1` y comprobar que el menú es más corto.
4. Subir todo a GitHub. Si el repositorio es privado, agregar al usuario `dmamanipar`.
5. Crear el modelo en SQL Power Architect y guardar el archivo en `docs/`.
6. Cambiar los textos de demostración si el caso de su agencia es otro (nombre, ciudad, destinos).
