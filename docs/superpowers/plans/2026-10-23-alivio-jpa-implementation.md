# Alivio JPA y PostgreSQL Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans. Steps use checkbox syntax.

**Goal:** Aplicar semanas 9 a 12 con entidades persistentes, CRUD y validación del servidor según AppSmartPhones.

**Architecture:** MVC existente, servicios transaccionales y repositorios JpaRepository. Relación ManyToOne de Solicitud a Profesional; configuración PostgreSQL local externa y pruebas H2 separadas.

**Tech Stack:** Java 21, Spring Boot 4.1.1, JPA, PostgreSQL, Jakarta Validation, Thymeleaf, H2 de prueba.

**Spec:** docs/superpowers/specs/2026-10-23-alivio-jpa-design.md

## Global Constraints

- Mantener los cuatro servicios y el acceso administrativo Demo.
- No publicar credenciales ni modificar AppSmartPhones.
- No agregar Spring Security en esta etapa.
- Evitar pérdida de estados por concurrencia y borrado de profesionales referenciadas.

### Task 1: Entidades, repositorios y servicios

**Files:** pom.xml; model/Solicitud.java y Profesional.java; repository/*.java; service/*.java; application.properties; src/test/resources/application.properties; pruebas de repositorio y servicio.

**Interfaces:** preservar crear(Solicitud), registrar(Profesional), buscarPorCodigo(String), listar(String, EstadoSolicitud), asignar(String,String), actualizarEstado(String,EstadoSolicitud). Agregar actualizar(String,Solicitud), eliminar(String) en servicios; repositorios JpaRepository<Entity,Long> con consulta bloqueada por id.

- [x] Probar primero persistencia y relación con saveAndFlush/clear/findById; rechazo de borrado de profesional asignada y transiciones sobre solicitudes canceladas.
- [x] Agregar dependencias JPA, PostgreSQL, Validation y H2 solo test. Anotar entidades y reemplazar repositorios temporales.
- [x] Migrar servicios a transacciones; bloqueo PESSIMISTIC_WRITE para cambios y devolución de copias en lecturas.
- [x] Migrar pruebas que construían repositorios de memoria a repositorios reales aislados de prueba.
- [x] Verificar servicios y repositorios; corregir fallos antes de continuar.

### Task 2: Validación de formularios

**Files:** web/SolicitudForm.java y ProfesionalForm.java; controller/SolicitudPublicaController.java y AdminProfesionalController.java; pruebas MVC.

**Interfaces:** conservar nombres de atributos flash, pasos de formulario y valores no convertibles. @Valid antes de @ModelAttribute, BindingResult inmediatamente después.

- [x] Probar nombre en blanco, longitud excedida, fechas pasadas, edad fuera de rango y tipo inválido con conservación de valores.
- [x] Anotar campos y activar validación. Distinguir errores de conversión y de restricción para mostrar el mensaje correcto.
- [x] Ejecutar pruebas MVC con base aislada.

### Task 3: CRUD administrativo

**Files:** controller/AdminSolicitudController.java; controller/AdminProfesionalController.java; templates/admin/solicitudes.html, profesionales.html y solicitud-formulario.html; prueba AdminCrudControllerTest.

**Interfaces:** GET /admin/solicitudes/{codigo}/editar, POST /admin/solicitudes/{codigo}/editar, GET /admin/solicitudes/{codigo}/eliminar y POST /admin/solicitudes/{codigo}/eliminar; GET/POST /admin/profesionales/{codigo}/eliminar. GET confirma, POST ejecuta. Mantener rutas existentes.

- [x] Probar edición válida, formulario inválido, restricciones de estado y eliminación sin efectos secundarios en GET.
- [x] Añadir formularios y confirmaciones; completar CRUD sin alterar el flujo de asignación.
- [x] Verificar páginas de edición y confirmación mediante MVC.

### Task 4: Configuración, documentación y entrega

**Files:** config/DatosDemoConfig.java; application.properties; README.md; application-local.properties.example; docs de diseño y plan.

- [x] Desactivar datos demo por defecto y documentar activación explícita.
- [x] Documentar creación de alivio_web y configuración por variables o archivo ignorado.
- [x] Ejecutar pruebas Java y JS, package y diff --check.
- [x] Revisar cambios con un agente independiente y corregir hallazgos.
- [x] Probar PostgreSQL real sin sustituir esta comprobación por H2. Verificado en instancia PostgreSQL 18 temporal aislada; la conexión al servidor personal requiere completar el archivo local.
- [x] Guardar y subir la rama con los cambios verificados y señalar cualquier comprobación pendiente.

## Evidencia de verificación — 23 de octubre de 2026

- Maven package: BUILD SUCCESS, 81 pruebas con H2 sin fallos. Se sustituyeron 50 repeticiones de pruebas sobre listas en memoria por seis ejecuciones de dos escenarios de concurrencia con transacciones JPA.
- Prueba JavaScript de foco: una prueba aprobada.
- PostgreSQL 18 temporal, separado del servidor personal: siete pruebas de relación y concurrencia aprobadas.
- Ejecutable sobre PostgreSQL: registro por HTTP, edición, evaluación y asignación; tras reiniciar el proceso se conservan paciente, estado ASIGNADA y clave foránea de profesional. Instancia temporal detenida tras verificar.
- Revisión independiente: sin hallazgos importantes.
- Pendiente de configuración personal: crear alivio_web y completar application-local.properties; no se conoce la contraseña de la instalación habitual.
- No se realizó verificación visual de anchos de pantalla.

- Entrega: rama codex/semana9-12-postgresql publicada; pull request en borrador https://github.com/J-Saturno/Alivio-web/pull/1.
