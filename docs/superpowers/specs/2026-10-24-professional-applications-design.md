# Postulaciones profesionales y acceso administrativo

## Objetivo

Permitir que profesionales de enfermería se postulen desde un formulario público con CV PDF. El equipo de Alivio revisa cada postulación y contacta manualmente por llamada o mensaje. Solo las aprobadas ingresan al listado asignable. La incorporación al grupo de WhatsApp es manual y ocurre después de la aceptación.

## Flujo

1. La persona envía nombre, celular, tipo profesional, especialidad, zona de cobertura y CV PDF.
2. Alivio guarda la postulación como PENDIENTE, sin crear una profesional asignable.
3. El administrador autenticado ve los datos y descarga el CV privado.
4. El administrador registra ACEPTADA o RECHAZADA y una nota interna de revisión. La comunicación se hace fuera de la web.
5. La aceptación crea una profesional no disponible. El administrador confirma contacto y disponibilidad antes de asignarla.

## Seguridad

- La administración requiere inicio de sesión con credenciales configuradas fuera del repositorio.
- Los CV solo se descargan mediante una ruta administrativa autenticada, como adjuntos con `nosniff`.
- El formulario acepta únicamente PDF de hasta 5 MiB y comprueba la firma del archivo.
- No se crea cuenta para profesionales ni se automatizan llamadas, mensajes o WhatsApp.

## Verificación

Pruebas de acceso, envío válido e inválido, revisión, CV privado y prevención de doble aprobación; compilación y suite completa.
