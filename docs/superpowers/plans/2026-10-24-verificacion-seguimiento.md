# Verificaciones y seguimiento Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans. Steps use checkbox syntax.

**Goal:** Registrar verificaciones profesionales trazables y ofrecer a la familia un seguimiento privado de estados.

**Architecture:** Nuevos historiales JPA privados; enlace aleatorio de seguimiento con hash persistido y vencimiento. Mantener MVC, formularios, control de estados y autenticación existentes.

**Tech Stack:** Java 21, Spring Boot, JPA/PostgreSQL, Jakarta Validation, Thymeleaf, Spring Security.

**Spec:** docs/superpowers/specs/2026-10-24-verificacion-seguimiento-design.md

## Global Constraints

- No mostrar datos personales o clínicos, notas internas ni CV en seguimiento.
- No almacenar el token de seguimiento en texto plano ni registrar enlaces secretos.
- No activar automáticamente profesionales al registrar verificaciones.
- Registrar autor y fecha de revisión desde el servidor.
- No enviar mensajes automáticos ni modificar las solicitudes personales durante pruebas.

### Task 1: Verificación de la base existente

- [x] Ejecutar pruebas de persistencia/asignación/concurrencia sobre PostgreSQL temporal: 37 aprobadas.
- [x] Revisar navegador con autenticación y CSRF activos; comprobar selección compatible y CV privado.
- [x] Revisar tamaños de pantalla adicionales cuando el navegador permita cambiar el viewport; documentar cualquier límite.

### Task 2: Revisiones profesionales

**Files:** nuevas clases model/VerificacionProfesional.java, model/ResultadoVerificacion.java, repository/VerificacionRepository.java, web/VerificacionForm.java, service/VerificacionService.java, controller/AdminVerificacionController.java y templates/admin/verificaciones.html; modificar AdminProfesionalController y templates/admin/profesionales.html.

**Interfaces:** VerificacionService.historial(String codigo), registrar(String codigo, VerificacionForm form, String revisor). La revisión aprobada requiere las tres comprobaciones y nota de evidencia; revisor obligatorio suministrado por Principal. Historial ordenado por fecha/id, sin edición destructiva.

- [x] Escribir pruebas de aprobación incompleta, evidencia y revisor obligatorios, historial y acceso privado.
- [x] Implementar entidad, formulario, validación de servicio y pantalla con errores conservados.
- [x] Mostrar estado pendiente/aprobado/rechazado sin alterar disponibilidad ni asignación.
- [x] Proteger el borrado de profesionales con historial de revisiones y mostrar un mensaje correcto.

### Task 3: Seguimiento familiar

**Files:** nuevas clases model/AccesoSeguimiento.java, model/EventoSolicitud.java, repository/AccesoSeguimientoRepository.java, repository/EventoSolicitudRepository.java, service/SeguimientoService.java, controller/SeguimientoController.java y templates/seguimiento.html; integrar SolicitudService, SolicitudPublicaController e inicio.html.

**Interfaces:** SeguimientoService.emitir(String codigo) devuelve token solo para mostrar una vez; revocar(String codigo); consultar(String token) devuelve DTO sin entidad ni datos personales; registrarEvento(Solicitud) registra estado actual. Dependencia de seguimiento solo hacia repositorios para evitar ciclos entre servicios.

- [x] Escribir pruebas de generación/hash/vencimiento/renovación/revocación y ausencia de datos privados.
- [x] Implementar token SecureRandom de 32 bytes, SHA-256 y duración de 30 días.
- [x] Registrar estado al crear, asignar y cambiar estado; no inventar eventos históricos de solicitudes antiguas.
- [x] Mostrar enlace al registrar solicitud; administración podrá emitir o revocar con POST y CSRF.
- [x] Añadir no-store, no-referrer y noindex a respuestas, incluyendo enlaces inválidos.
- [x] Permitir eliminar solicitudes según reglas actuales eliminando primero sus accesos/eventos propios.

### Task 4: Integración y entrega

- [x] Ejecutar pruebas H2 completas, JavaScript y paquete ejecutable.
- [x] Revisar cambios independientemente y resolver hallazgos importantes.
- [x] Repetir pruebas nuevas sobre PostgreSQL aislado y recorrido en navegador.
- [x] Actualizar README y evidencia de verificación.
- [x] Publicar rama y pull request para revisión; no integrar a main sin autorización expresa.

## Evidencia final

- 117 pruebas Java H2 aprobadas; paquete ejecutable compilado.
- Dos pruebas JavaScript aprobadas.
- Ocho pruebas nuevas aprobadas sobre PostgreSQL 18 temporal, incluida la regresión determinista de lectura concurrente.
- La prueba concurrente falló primero por mezclar CANCELADA con historial PENDIENTE; se corrigió con instantánea REPEATABLE_READ y estado derivado del último evento. Revisión independiente posterior aprobada.
- Navegador: login, asignación compatible, CV privado, seguimiento con dos estados, revisión incompleta rechazada y revisión ficticia completa registrada con autor y fecha. Anchos 360/768/1280 sin desbordamiento horizontal global en pantallas nuevas.
- No hay envío automático de mensajes ni cambios automáticos de disponibilidad. Los datos usados fueron ficticios.

- Entrega: rama codex/verificacion-seguimiento publicada; pull request en borrador https://github.com/J-Saturno/Alivio-web/pull/2. Instancia y base temporales detenidas/eliminadas después de verificar.
