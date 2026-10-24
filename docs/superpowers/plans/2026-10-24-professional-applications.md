# Postulaciones profesionales Implementation Plan

**Goal:** Recibir postulaciones con CV PDF, revisarlas de forma privada y proteger la administración.

**Architecture:** Una entidad `PostulacionProfesional` separa candidatas de profesionales asignables. Spring Security protege `/admin/**`; la aprobación crea una profesional no disponible. El PDF se almacena en PostgreSQL y se sirve como adjunto autenticado.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring Security, Spring Data JPA, Thymeleaf, PostgreSQL y MockMvc.

**Spec:** `docs/superpowers/specs/2026-10-24-professional-applications-design.md`

## Global Constraints

- No crear cuentas profesionales ni enviar mensajes automáticos.
- CV PDF de hasta 5 MiB, solo accesible a administración.
- Una postulación aceptada no debe ser asignable hasta que el equipo marque disponibilidad.
- Preservar los datos y pruebas existentes.

## Task 1: Protección administrativa

- [ ] Escribir prueba MockMvc: visitante recibe redirección al login en `/admin/solicitudes`, formulario público permanece accesible.
- [ ] Ejecutar prueba y confirmar fallo.
- [ ] Agregar Spring Security, pantalla de login y credenciales por variables de entorno sin valor por defecto.
- [ ] Confirmar prueba verde. Mantener las pruebas heredadas con seguridad desactivada solo en perfil de prueba.

## Task 2: Postulación pública y CV

- [ ] Escribir prueba MockMvc: envío PDF válido crea postulación PENDIENTE sin crear Profesional; PDF incorrecto y tamaño excesivo se rechazan.
- [ ] Ejecutar y confirmar fallo.
- [ ] Implementar entidad, repositorio, servicio, formulario, plantilla y enlace de navegación.
- [ ] Confirmar pruebas verdes.

## Task 3: Revisión privada

- [ ] Escribir prueba MockMvc: administrador autenticado lista postulaciones y descarga CV; visitante no accede.
- [ ] Escribir prueba de aceptación: crea profesional no disponible una sola vez; rechazo no crea profesional.
- [ ] Ejecutar y confirmar fallos.
- [ ] Implementar controladores, plantilla de revisión y transiciones con bloqueo transaccional.
- [ ] Confirmar pruebas verdes.

## Task 4: Verificación y documentación

- [ ] Documentar variables de acceso, límites de carga y flujo humano de llamada/mensaje/WhatsApp en README.
- [ ] Ejecutar suite Java, prueba JavaScript y empaquetado.
- [ ] Revisar `git diff` y comprobar que no se añadieron CV ni credenciales reales.
