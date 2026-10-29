# Alivio Web

Aplicación académica de cuidados domiciliarios con Java 21, Spring Boot, Spring Web, Thymeleaf y Bootstrap. La etapa de semanas 9 a 12 incorpora Spring Data JPA, PostgreSQL y Jakarta Validation según el ejemplo AppSmartPhones del profesor.

## Configurar PostgreSQL

1. Crear una base exclusiva para este proyecto desde pgAdmin o una consola SQL:

```sql
CREATE DATABASE alivio_web;
```

2. Copiar `application-local.properties.example` a `application-local.properties` en la raíz del proyecto y completar el usuario y la contraseña locales. El archivo local está ignorado por Git.
3. También se pueden configurar `ALIVIO_DB_URL`, `ALIVIO_DB_USERNAME` y `ALIVIO_DB_PASSWORD` en el entorno. Si se usa ese método, retirar el archivo local para que sus valores no sustituyan los del entorno.

JPA crea o actualiza las tablas mediante `ddl-auto=update`, igual que el ejemplo de clase. Los datos permanecen al detener la aplicación. No usar la base del proyecto del profesor.

## Ejecutar

```powershell
.\mvnw.cmd spring-boot:run
```

Abrir http://localhost:8080.

Antes de iniciar, completar también `alivio.admin.username` y `alivio.admin.password` en `application-local.properties`. Son el usuario y la contraseña para entrar a la web administrativa; son distintos de los de PostgreSQL. Este archivo está ignorado por Git y no se sube al repositorio.

Como alternativa, configurar `ALIVIO_ADMIN_USERNAME` y `ALIVIO_ADMIN_PASSWORD` en el entorno. El acceso administrativo requiere ambas credenciales; no se debe desactivar la protección para resolver un error de arranque. Para un despliegue público, servir la aplicación mediante HTTPS.

La base inicia vacía. Para cargar los tres profesionales y dos solicitudes ficticias, agregar `alivio.demo.enabled=true` al archivo local o definir `ALIVIO_DEMO_ENABLED=true`. Los datos demo solo se agregan cuando ambos registros están vacíos.

## Funciones de esta etapa

- Solicitudes y profesionales son entidades JPA con identificadores generados por secuencias. Sus códigos visibles SOL-001 y PRO-001 no se reutilizan tras borrar registros.
- Cada solicitud puede tener una profesional asignada; una profesional puede estar asociada a varias solicitudes mediante `ManyToOne` y una clave foránea.
- Solicitudes: registrar, listar, consultar, editar antes de asignar, asignar, cambiar de estado y eliminar si están pendientes o canceladas.
- Profesionales: registrar, listar, consultar en su tabla/modal, editar, cambiar disponibilidad y eliminar si no tienen solicitudes vinculadas.
- Las eliminaciones muestran una confirmación y se ejecutan por POST. No hay borrado en cascada de solicitudes.
- Los formularios usan `@Valid`, `BindingResult` y restricciones como `@NotBlank`, `@Size`, `@NotNull`, `@Pattern`, `@Min`, `@Max` y `@FutureOrPresent`. Se conservan los datos rechazados y se muestran errores por campo.
- El servidor limita los nombres a 100 caracteres, distrito/zona a 80 y descripción a 500; valida celulares que empiezan con 9 y tienen nueve dígitos.
- Los cambios de estado y asignación son transaccionales y bloquean la fila para evitar sobrescribir una cancelación concurrente.
- Para asignar, el panel propone profesionales disponibles cuya especialidad y distrito coinciden con la solicitud y que no tienen otra atención superpuesta en esa fecha. Los distritos de cobertura se escriben separados por comas; se comparan como nombres completos sin distinguir tildes ni mayúsculas. La elección final sigue siendo del coordinador y el servidor rechaza un POST incompatible.
- Los turnos de 12 y 24 horas bloquean conservadoramente cualquier otro turno de esa fecha. `disponible` es un indicador manual; todavía no hay calendario laboral personal ni horarios de inicio y fin.
- Postulaciones: formulario público en `/postular` con datos profesionales y CV PDF de hasta 5 MB. Se guardan como pendientes en PostgreSQL y no aparecen entre profesionales asignables.
- Administración: inicio de sesión en `/admin/ingresar`, revisión privada de postulaciones y descarga del CV como adjunto. Una aceptación crea el registro profesional como no disponible. La decisión requiere nota interna y evita aprobaciones duplicadas.
- La llamada o el mensaje de aceptación y la incorporación al grupo de WhatsApp son tareas manuales del equipo de Alivio.

## Validación y limpieza de textos

Los formularios y los servicios comparten las reglas de `validacion/Textos.java`. Se normalizan espacios Unicode, se retiran caracteres de formato invisibles y se conservan los párrafos de las descripciones y notas. Los nombres requieren letras y admiten tildes, ñ, iniciales, apóstrofes y guiones. Se rechazan nombres numéricos, campos visualmente vacíos y coberturas con elementos vacíos o solo símbolos.

La cobertura se guarda con distritos separados por comas. También se normalizan punto y coma y guiones rodeados de espacios. Esta validación comprueba el formato; todavía no hay un catálogo oficial ni equivalencias entre nombres como `Molina` y `La Molina`. Solicitud y cobertura deben usar el mismo nombre de distrito. La comparación mantiene su tolerancia a tildes y mayúsculas.

Al pegar un celular completo con espacios, el navegador los limpia antes de aplicar el límite del campo. El servidor vuelve a normalizar y exige nueve dígitos que empiecen con 9. El listado administrativo advierte posibles duplicados por nombre normalizado o teléfono, sin bloquear homónimos ni teléfonos compartidos. Los registros existentes no se reescriben ni se fusionan automáticamente.

## Verificaciones profesionales

Desde el listado de profesionales se puede abrir su historial privado de verificaciones. Cada revisión registra comprobación de identidad, credencial y referencias, experiencia declarada, referencia de credencial, evidencia, resultado, fecha y usuario autenticado. Una aprobación requiere las tres comprobaciones y evidencia escrita. Recibir un CV o aceptar una postulación no marca a la profesional como verificada.

Las revisiones se agregan al historial; no se editan ni se borran desde la aplicación. Registrar una revisión no activa la disponibilidad ni cambia asignaciones. El registro de una profesional con verificaciones no se puede eliminar. Estas anotaciones documentan revisiones humanas, no una consulta automática a registros oficiales.

## Seguimiento para la familia

Al registrar una solicitud se muestra un enlace privado con vigencia de 30 días. La página presenta únicamente código, estado, fechas e historial de estados; no incluye nombres, teléfonos, descripción de atención, CV ni notas administrativas. Solo se guarda el hash del token aleatorio en PostgreSQL.

El coordinador puede generar o renovar el enlace y revocarlo desde la tabla de solicitudes. El enlace se muestra una sola vez; renovarlo invalida el anterior. Para solicitudes antiguas, el historial comienza con el estado conocido al empezar a registrar eventos, sin reconstruir cambios anteriores. No hay avisos automáticos: compartir el enlace y comunicar cambios sigue siendo tarea del equipo.

Las respuestas de seguimiento no se almacenan en caché ni se indexan y no envían referencias de navegación. La página no carga recursos externos. En un despliegue público se debe usar HTTPS, evitar registrar rutas secretas en logs del proxy y compartir los enlaces por un canal privado.

## Acceso administrativo

Desde la portada seleccionar `Administración` e ingresar con las credenciales configuradas. Las páginas y descargas bajo `/admin/**` requieren sesión. Los CV no se publican como archivos estáticos.

## Verificar

```powershell
.\mvnw.cmd test
node --test src/test/js/admin-focus.test.cjs src/test/js/telefono-pegado.test.cjs
.\mvnw.cmd package -DskipTests
```

Las pruebas automáticas usan H2 en modo PostgreSQL, con una base aislada y transacciones. No acceden a las credenciales del archivo local ni a la base personal. Incluyen CRUD, relaciones, restricciones, recuperación de formularios y concurrencia de transacciones JPA. La validación sobre PostgreSQL real y la revisión visual en celular, tableta y escritorio se comprueban por separado.

## Docker

El Dockerfile compila el ejecutable. Al ejecutar el contenedor, pasar las variables de conexión de PostgreSQL y las credenciales administrativas; en Docker Desktop, el servidor de la computadora puede usarse con `host.docker.internal`. La configuración local y las credenciales no se incluyen en la imagen.

### Última verificación

24 de octubre de 2026: Maven package completado con 117 pruebas Java sobre H2 y dos pruebas JavaScript aprobadas. Ocho pruebas nuevas de verificaciones, seguimiento, vencimiento y lectura concurrente también pasaron sobre una base temporal de PostgreSQL 18. Antes de esta ampliación se ejecutaron otras 37 pruebas de persistencia/asignación/concurrencia sobre PostgreSQL.

En navegador, con autenticación y CSRF activos, se comprobaron asignación compatible, postulación con PDF privado, generación del enlace familiar, cambio visible de estado, rechazo de revisión profesional incompleta y registro de una revisión ficticia con autor/fecha. Las dos pantallas nuevas se revisaron a 360, 768 y 1280 px sin desbordamiento horizontal global. Los datos de prueba se mantuvieron en una base temporal separada; no se enviaron mensajes ni se realizaron verificaciones reales de personas.
