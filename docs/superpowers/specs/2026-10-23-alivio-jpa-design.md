# Alivio: persistencia y CRUD de semanas 9 a 12

Referencia: AppSmartPhones del profesor. El usuario confirmó PostgreSQL instalado y autorizó continuar con entidades JPA, relaciones, CRUD y validación. PostgreSQL sustituye la referencia a MySQL del diseño original.

## Alcance aprobado y adaptación

- Mantener Spring MVC, Thymeleaf, Bootstrap y formularios propios; incorporar Spring Data JPA, PostgreSQL y Jakarta Validation.
- Solicitud y Profesional serán entidades con identificador Long generado por secuencia; mostrar códigos SOL-001 y PRO-001 derivados del identificador, sin reiniciar contadores al arrancar.
- Solicitud tendrá ManyToOne opcional con Profesional. No eliminar en cascada profesionales ni solicitudes. Enumeraciones guardadas como texto.
- Repositorios JpaRepository; lecturas devolverán copias para conservar el aislamiento actual. Las operaciones que cambian estado usan transacciones y bloqueo de fila para evitar sobrescribir cancelaciones.
- Crear, listar, consultar, editar y eliminar mediante formularios. Solo editar solicitudes PENDIENTE/EN_EVALUACION; eliminar solicitudes PENDIENTE/CANCELADA. No eliminar profesionales vinculadas a solicitudes. Las eliminaciones usan POST y confirmación previa.
- Validar formularios con @Valid y BindingResult, conservando valores rechazados y mensajes por campo. Entidades con NotBlank, Size, NotNull, Pattern y límites numéricos. FutureOrPresent solo al recibir formularios, para no impedir finalizar atenciones de fechas pasadas.
- Los datos demo serán opcionales y estarán desactivados por defecto con persistencia real.
- Configuración local ignorada por Git; ninguna contraseña al repositorio. No modificar la base del ejemplo del profesor.
- Pruebas automáticas con H2 en modo PostgreSQL; comprobación adicional con PostgreSQL local cuando las credenciales estén disponibles. Distinguir ambos resultados.
- No incorporar autenticación, pagos ni historias clínicas en esta etapa.

## Validación de entrega

Ejecutar suite Java, prueba JS, compilación de artefacto y revisión del diff. Probar CRUD, relación, integridad referencial, conservación de formularios inválidos y bloqueo concurrente. Con PostgreSQL real comprobar guardado, recarga y persistencia tras reiniciar. Documentar cualquier comprobación pendiente.
