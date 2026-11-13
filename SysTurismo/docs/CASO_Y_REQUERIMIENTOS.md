# Caso y requerimientos

## Descripción del caso

Una agencia de turismo de Arequipa vende paquetes de viaje a destinos del Perú. Hoy lleva sus reservas en cuadernos y hojas de cálculo, y eso le ha causado problemas: vende más cupos de los que tiene para una misma salida, no sabe cuánto debe cada cliente y no puede responder rápido cuántos viajes tiene en el mes.

Se necesita un sistema de escritorio que use el personal de la agencia. El administrador mantiene el catálogo (destinos, guías, paquetes y usuarios). El agente atiende a los turistas, registra sus reservas y cobra los pagos.

Un paquete pertenece a un destino y tiene un guía asignado, una duración, un precio por persona y un cupo máximo de personas por salida. Un turista reserva un paquete para una fecha y una cantidad de personas. El total es el precio por la cantidad de personas. La reserva puede quedar pendiente o confirmada, según el dinero adelantado. Los pagos pueden hacerse en partes, y una reserva puede cancelarse, lo que libera los cupos.

## Reglas del negocio

1. Una reserva queda confirmada cuando lo pagado alcanza el 30% del total. Si el adelanto es menor, queda pendiente.
2. Para un mismo paquete y una misma fecha no se pueden reservar más personas que el cupo máximo. Las reservas canceladas no cuentan.
3. La fecha de viaje debe ser posterior a hoy.
4. El total lo calcula el sistema; la pantalla solo lo muestra.
5. Un pago no puede superar el saldo pendiente, y una reserva cancelada no admite pagos.
6. Las reservas no se editan ni se eliminan: se cancelan.
7. No se repite el documento de un turista, el DNI de un guía ni el nombre de un usuario.
8. Un DNI tiene 8 dígitos. El celular de un guía tiene 9 dígitos y empieza con 9.
9. Un destino, guía, paquete o turista con registros relacionados no se puede eliminar.
10. La contraseña tiene al menos 8 caracteres y combina letras y números.

## Requerimientos funcionales

| Código | Requerimiento |
|---|---|
| RF01 | Iniciar y cerrar sesión con usuario y contraseña. |
| RF02 | Mostrar un menú distinto según el perfil (Root, Administrador, Agente). |
| RF03 | Crear, listar, editar y eliminar destinos. |
| RF04 | Crear, listar, editar y eliminar guías. |
| RF05 | Crear, listar, editar y eliminar paquetes, asignando destino y guía. |
| RF06 | Crear, listar, editar y eliminar turistas. |
| RF07 | Crear, listar, editar y eliminar usuarios del sistema con su perfil. |
| RF08 | Registrar una reserva con su adelanto, mostrando antes el total y los cupos libres. |
| RF09 | Registrar pagos posteriores de una reserva. |
| RF10 | Cancelar una reserva. |
| RF11 | Buscar y paginar los listados de todas las pantallas. |
| RF12 | Mostrar un panel con turistas, paquetes activos, reservas del mes, ingresos, reservas por estado, ingresos por paquete y próximas salidas. |

## Requerimientos no funcionales

| Código | Requerimiento |
|---|---|
| RNF01 | Aplicación de escritorio en Java 21 con interfaz JavaFX. |
| RNF02 | Datos en una base relacional H2 embebida, accedida con JDBC. |
| RNF03 | Arquitectura en capas: presentación, lógica de negocio y acceso a datos. |
| RNF04 | Las contraseñas se guardan con PBKDF2 y sal aleatoria, nunca en texto plano. |
| RNF05 | Todas las consultas usan parámetros, no se arma SQL con texto del usuario. |
| RNF06 | Cada campo se valida y el usuario ve qué campo falló y por qué. |
| RNF07 | Cada acción informa su resultado con un mensaje en pantalla. |
