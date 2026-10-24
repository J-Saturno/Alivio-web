# Propuesta: verificaciones profesionales y seguimiento familiar

Estado: aprobado por el usuario el 24 de octubre de 2026 mediante respuesta «Sí, implementar esa propuesta». Continúa la revisión de prioridades de esa fecha.

## 1. Verificación sobre PostgreSQL y navegador

Ejecutar las nuevas pruebas sobre una base temporal exclusiva del servidor PostgreSQL instalado, sin usar las tablas personales de alivio_web. Probar con seguridad activa y revisar portada, ingreso, postulaciones y administración en navegador. Diferenciar resultados funcionales de inspección visual.

## 2. Registro de verificaciones profesionales

Agregar una pantalla privada por profesional, accesible solo desde administración. Registrar comprobación de identidad, credencial, referencias, años de experiencia y una nota de evidencia. No recopilar DNI ni publicar documentos adicionales en esta etapa.

Cada revisión conservará resultado (aprobada/rechazada), fecha y usuario autenticado que la registró. No se podrá marcar aprobada sin confirmar identidad, credencial y referencias y describir la evidencia revisada. Las profesionales sin revisión se mostrarán como pendientes; un CV recibido o una postulación aceptada no equivalen a una verificación aprobada.

Mantener estas revisiones como historial privado. La disponibilidad manual y el flujo existente de postulaciones se conservan; registrar una verificación no activa automáticamente a la profesional ni cambia una asignación existente. Una futura regla que obligue a verificar antes de asignar se decidirá expresamente para no bloquear registros existentes de forma sorpresiva.

## 3. Seguimiento de solicitudes para la familia

Opción recomendada: enlace privado aleatorio generado al registrar una solicitud. Alternativa: cuenta de familiar o código de un solo uso por SMS/correo; exige gestión de cuentas o un proveedor de mensajería, fuera de esta primera entrega.

El enlace usa 32 bytes aleatorios, no el código SOL-001. Guardar solo su hash en la base y mostrar el enlace completo únicamente al generarlo. Caducidad inicial de 30 días; administración podrá renovarlo o revocarlo. Renovar invalida el anterior. Las solicitudes antiguas no recibirán enlaces accesibles a partir de su código público: el coordinador los generará y compartirá por su canal de contacto existente.

La página familiar mostrará código, estado actual, fecha de actualización e historial de estados desde la incorporación de esta función. No mostrará nombre del paciente, teléfono, dirección, descripción de necesidad, CV ni notas internas. Los estados previos de solicitudes antiguas no se inventarán: se indicará desde cuándo se registra el historial.

Las respuestas llevarán Cache-Control no-store, Referrer-Policy no-referrer y exclusión de indexación. Un enlace inválido, revocado o vencido mostrará un mensaje genérico. No registrar enlaces secretos en logs ni incorporarlos a páginas públicas de listados.

No enviar mensajes automáticos en esta entrega. El registro de cambios permite consultarlos desde el enlace; compartirlo por llamada, mensaje o correo seguirá siendo tarea del equipo.

## 4. Alcance y criterios de entrega

- Mantener autenticación, CSRF, CRUD, revisión de postulaciones y compatibilidad de asignación existentes.
- Pruebas de rechazo de verificaciones incompletas, registro de revisor/fecha e historial privado.
- Pruebas de enlace impredecible, hash persistido, expiración, revocación y renovación; ausencia de datos personales en la vista familiar.
- Pruebas de historial consistente con transacciones: una transición rechazada no genera eventos.
- Revisar PostgreSQL, navegador y suite completa antes de publicar los cambios.
- Usuarios administrativos múltiples, agenda con horas exactas y notificaciones automáticas quedan fuera de esta entrega.
