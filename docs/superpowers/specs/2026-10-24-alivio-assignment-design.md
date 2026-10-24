# Asignación compatible de profesionales

## Objetivo

Mostrar al coordinador profesionales compatibles para cada solicitud en evaluación y evitar asignaciones incorrectas desde el servidor.

## Criterios

Una candidata es compatible cuando está disponible, su especialidad coincide con el servicio, el distrito aparece como valor completo en su zona de cobertura y no tiene otra solicitud ASIGNADA o EN_ATENCION para la misma fecha y un turno que se superponga. La zona admite uno o varios distritos separados por comas. Se ignoran mayúsculas, espacios y tildes al comparar.

Los turnos iguales se superponen. VEINTICUATRO_HORAS y DOCE_HORAS se tratan de forma conservadora como superpuestos a cualquier otro turno del mismo día, porque hoy el sistema no registra horas de inicio y fin.

## Interfaz y reglas

El modal de solicitud mostrará solo candidatas compatibles y un mensaje cuando ninguna cumpla los criterios. La asignación seguirá siendo decisión manual del coordinador. `SolicitudService.asignar` repetirá las comprobaciones bajo bloqueo transaccional de la solicitud y la profesional; una petición HTTP manipulada no podrá saltarlas.

La disponibilidad actual es un indicador manual. El sistema aún no tiene un calendario laboral personal ni una disponibilidad futura por fecha; se añadirá en otra etapa si se necesita.

## Verificación

Pruebas para especialidad, zona exacta, distrito múltiple, tildes, turnos ocupados, cancelaciones, acceso a la lista compatible y POST directo incompatible.
