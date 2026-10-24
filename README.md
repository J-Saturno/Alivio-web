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

Antes de iniciar, configurar `ALIVIO_ADMIN_USERNAME` y `ALIVIO_ADMIN_PASSWORD` como variables de entorno. Son obligatorias para proteger `/admin/**`; no usar credenciales reales en archivos versionados. Para un despliegue público, servir la aplicación mediante HTTPS.

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

## Acceso administrativo

Desde la portada seleccionar `Administración` e ingresar con las credenciales configuradas. Las páginas y descargas bajo `/admin/**` requieren sesión. Los CV no se publican como archivos estáticos.

## Verificar

```powershell
.\mvnw.cmd test
node --test src/test/js/admin-focus.test.cjs
.\mvnw.cmd package -DskipTests
```

Las pruebas automáticas usan H2 en modo PostgreSQL, con una base aislada y transacciones. No acceden a las credenciales del archivo local ni a la base personal. Incluyen CRUD, relaciones, restricciones, recuperación de formularios y concurrencia de transacciones JPA. La validación sobre PostgreSQL real y la revisión visual en celular, tableta y escritorio se comprueban por separado.

## Docker

El Dockerfile compila el ejecutable. Al ejecutar el contenedor, pasar las variables de conexión de PostgreSQL y las credenciales administrativas; en Docker Desktop, el servidor de la computadora puede usarse con `host.docker.internal`. La configuración local y las credenciales no se incluyen en la imagen.

### Última verificación

24 de octubre de 2026: 97 pruebas Java sobre H2 y dos pruebas JavaScript aprobadas; paquete ejecutable compilado. Las pruebas nuevas cubren seguridad, postulación con PDF, revisión administrativa, compatibilidad de asignación y dos coordinadores intentando reservar el mismo turno. Estas mejoras todavía no se probaron sobre PostgreSQL real ni se revisaron visualmente en varios tamaños de pantalla. La conexión al PostgreSQL personal requiere completar el archivo local.
