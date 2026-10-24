# Asignación compatible Implementation Plan

**Goal:** Recomendar profesionales compatibles y bloquear asignaciones incompatibles.

**Architecture:** Un servicio de compatibilidad compara datos de solicitud y profesional con las reservas activas de la fecha. El controlador usa la misma lógica para presentar candidatas y `SolicitudService` la revalida antes de guardar.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring Data JPA, Thymeleaf, JavaScript y JUnit/MockMvc.

**Spec:** `docs/superpowers/specs/2026-10-24-alivio-assignment-design.md`

## Global Constraints

- La selección final sigue siendo manual.
- No inferir disponibilidad laboral futura a partir de la casilla `disponible`.
- Los turnos de 12 y 24 horas se tratan como conflictivos con cualquier turno de la misma fecha.
- Conservar los datos de la primera mejora y las pruebas existentes.

## Task 1: Criterios de compatibilidad

- [ ] Escribir pruebas que fallen para especialidad, distrito completo, normalización y cruces de turnos.
- [ ] Implementar servicio de compatibilidad y consulta de reservas activas.
- [ ] Confirmar que las pruebas específicas pasan.

## Task 2: Asignación segura

- [ ] Escribir prueba que demuestre rechazo de POST incompatible y aceptación de candidata compatible.
- [ ] Revalidar en `SolicitudService.asignar` mientras la fila profesional permanece bloqueada.
- [ ] Confirmar pruebas de servicio y controlador.

## Task 3: Interfaz administrativa

- [ ] Escribir prueba de candidatos por solicitud y mensajes cuando no existan.
- [ ] Mostrar candidatas compatibles en el modal sin exponer una lista de opciones incorrectas.
- [ ] Verificar accesibilidad y prueba JavaScript del modal.

## Task 4: Documentación y regresión

- [ ] Explicar criterios y límite del calendario en README.
- [ ] Ejecutar suite Java, prueba JavaScript y paquete.
- [ ] Revisar el diff y mantener intacto el checkout original.
