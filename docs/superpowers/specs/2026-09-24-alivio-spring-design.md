# Diseño de Alivio Web con Spring Boot

Fecha: 24 de septiembre de 2026

## 1. Propósito

Alivio será una plataforma web para solicitar servicios de cuidado domiciliario y para que un administrador con criterio clínico revise cada solicitud, seleccione al personal adecuado y haga seguimiento del servicio.

El proyecto debe demostrar los temas del curso de forma progresiva:

- Frontend: HTML, CSS, JavaScript, Bootstrap y diseño responsivo.
- Backend inicial: Java, Spring Boot, Spring Web y Thymeleaf.
- Evolución posterior: Spring Data JPA, MySQL, validación y Spring Security.

## 2. Alcance de esta versión

La primera versión funcional incluirá:

- Una portada pública esencial y enfocada.
- Un formulario de solicitud en un modal de Bootstrap dividido en dos pasos.
- Un panel administrativo de solicitudes basado en una tabla responsiva.
- Un registro básico de enfermeras y técnicas.
- La asignación manual de una profesional disponible a cada solicitud.
- El seguimiento de solicitudes mediante estados controlados.
- Datos almacenados temporalmente en listas de Java mientras se estudia la persistencia con MySQL.
- Un enlace temporal `Acceso administrativo - Demo` sin autenticación real.

## 3. Fuera de alcance

Esta etapa no incluirá:

- MySQL, Hibernate ni Spring Data JPA.
- Autenticación real, roles o Spring Security.
- Pagos, membresías o facturación.
- Geolocalización o seguimiento en tiempo real.
- Calendarios avanzados o gestión detallada de turnos.
- Expedientes, historias clínicas o documentos médicos.
- Venta o alquiler de productos médicos.
- Fisioterapia, podología, nutrición u otras especialidades adicionales.

## 4. Usuarios

### Familiar o cliente

Consulta los servicios, registra una solicitud breve y recibe un código de confirmación. No necesita una cuenta en esta etapa.

### Administrador

Revisa solicitudes, evalúa la necesidad descrita, administra el personal disponible, asigna una profesional y actualiza el estado del servicio. El acceso será directo mediante un enlace de demostración hasta incorporar Spring Security.

### Profesional

Es una enfermera licenciada o técnica registrada por el administrador. No tendrá acceso propio al sistema en esta etapa.

## 5. Servicios principales

La portada mostrará únicamente cuatro categorías:

1. Cuidado del adulto mayor.
2. Curaciones y cuidado postoperatorio.
3. Inyectables y tratamientos indicados.
4. Acompañamiento domiciliario.

Cada categoría tendrá una descripción breve y una llamada a solicitar atención. La plataforma no presentará servicios que todavía no pueda gestionar.

## 6. Estructura de la portada

La dirección visual será humana, directa y con poco ruido. La página tendrá:

1. Navegación breve con marca, servicios, cómo funciona y acceso administrativo de demostración.
2. Sección principal con una promesa clara y el botón `Solicitar atención`.
3. Tres señales de confianza: personal registrado, atención a domicilio y seguimiento del servicio.
4. Cuatro tarjetas de servicios.
5. Explicación del proceso en tres pasos: solicitar, evaluar y asignar.
6. Sección breve sobre por qué elegir Alivio.
7. Llamada final a solicitar atención.
8. Pie de página con contacto y enlaces esenciales.

No se incluirán productos, carruseles extensos ni bloques repetidos de contenido.

## 7. Formulario público

El formulario se abrirá dentro de un modal de Bootstrap y se dividirá en dos pasos para reducir la carga visual.

### Paso 1: contacto y necesidad

- Nombre del contacto.
- Teléfono peruano de nueve dígitos.
- Nombre del paciente.
- Edad del paciente, opcional.
- Uno de los cuatro servicios aprobados.

### Paso 2: coordinación

- Distrito.
- Fecha requerida.
- Turno o rango horario básico.
- Descripción breve de la necesidad.

El usuario podrá avanzar y retroceder sin perder los datos. Al completar el formulario, Spring registrará la solicitud y mostrará un modal de confirmación con su código.

No se solicitarán diagnóstico completo, historia clínica, DNI, recetas ni documentos. La información clínica adicional se recopilará posteriormente durante el contacto del administrador.

## 8. Gestión de solicitudes

La pantalla administrativa utilizará una tabla enfocada y responsiva con:

- Indicadores de solicitudes pendientes, asignadas y finalizadas.
- Búsqueda por código, contacto o paciente.
- Filtro por estado.
- Columnas esenciales: código, paciente, servicio, fecha, estado y acción.
- Acción `Revisar`, que abrirá un modal con el detalle completo.
- Selector de profesionales disponibles para realizar la asignación.

El flujo de estados será:

`Pendiente -> En evaluación -> Asignada -> En atención -> Finalizada`

`Cancelada` será un estado alternativo. Una solicitud no podrá pasar a `Asignada` hasta seleccionar una profesional disponible.

## 9. Gestión de profesionales

El administrador tendrá una pantalla con tabla y modales para registrar y editar personal. Cada registro incluirá:

- Código generado automáticamente.
- Nombre completo.
- Tipo: licenciada en enfermería o técnica en enfermería.
- Teléfono.
- Distrito o zona de cobertura.
- Especialidad principal.
- Disponibilidad: disponible o no disponible.

Solo el personal marcado como disponible aparecerá en el selector de asignación. Esta versión no administrará calendarios ni turnos detallados.

## 10. Modelo de datos

### Solicitud

- `codigo`
- `nombreContacto`
- `telefonoContacto`
- `nombrePaciente`
- `edadPaciente`, opcional
- `tipoServicio`
- `distrito`
- `fechaRequerida`
- `turno`
- `descripcion`
- `estado`
- `profesionalAsignado`, opcional hasta realizar la asignación

### Profesional

- `codigo`
- `nombreCompleto`
- `tipoProfesional`
- `telefono`
- `zonaCobertura`
- `especialidad`
- `disponible`

Una solicitud tendrá cero o una profesional asignada. Una profesional podrá atender varias solicitudes a lo largo del tiempo.

## 11. Arquitectura

Alivio será un proyecto Spring Boot independiente. `AppSmartPhones` se conservará únicamente como referencia del trabajo de clase.

### Presentación

- `inicio.html`: portada y formulario público.
- `solicitudes.html`: gestión administrativa de solicitudes.
- `profesionales.html`: registro y disponibilidad del personal.
- Fragmentos Thymeleaf reutilizables para navegación, pie de página y mensajes.
- Recursos estáticos organizados en `static/css`, `static/js` y `static/img`.

### Controladores

- `InicioController`: renderiza la portada.
- `SolicitudController`: registra, lista, filtra, revisa, asigna y actualiza solicitudes.
- `ProfesionalController`: lista, registra, edita y cambia la disponibilidad del personal.

### Servicios

- `SolicitudService`: aplica las reglas del flujo de solicitudes y asignación.
- `ProfesionalService`: administra el registro y la disponibilidad del personal.

### Repositorios temporales

- `SolicitudRepository`: mantiene solicitudes en una lista de Java.
- `ProfesionalRepository`: mantiene profesionales en una lista de Java.

La separación por capas permitirá sustituir posteriormente los repositorios en memoria por Spring Data JPA sin rediseñar las vistas ni los controladores.

## 12. Responsabilidad de cada tecnología

- Bootstrap controlará la cuadrícula responsiva, navegación, tarjetas, tablas, formularios y modales.
- CSS propio definirá la identidad visual humana de Alivio sin duplicar las utilidades de Bootstrap.
- JavaScript manejará el formulario en dos pasos, la interacción inmediata, filtros visuales cuando correspondan y validaciones del navegador.
- Thymeleaf generará rutas, valores, listas, opciones, mensajes y estados provenientes de Spring.
- Spring Web recibirá las acciones, aplicará la navegación y enviará los datos a las plantillas.

Bootstrap no será reemplazado por Thymeleaf; ambos se usarán en conjunto.

## 13. Validaciones y errores

- Los errores se mostrarán junto al campo correspondiente.
- Se validarán campos obligatorios, teléfono de nueve dígitos y fecha no anterior al día actual.
- El formulario conservará sus valores si existe un error.
- Una asignación sin profesional disponible será rechazada con un mensaje comprensible.
- No se expondrán mensajes técnicos ni trazas de Java al usuario.
- Una operación correcta mostrará una confirmación visible y contextual.
- Las rutas inexistentes presentarán una página de error sencilla y coherente con la identidad de Alivio.

## 14. Diseño responsivo y accesibilidad

- La interfaz se verificará en anchos de celular, tableta y escritorio.
- La tabla tendrá una presentación desplazable o adaptada en pantallas pequeñas.
- Los modales no excederán la altura visible y permitirán desplazamiento interno.
- Todos los campos tendrán etiquetas explícitas.
- Los controles se podrán recorrer mediante teclado.
- El contraste, los estados de foco y los mensajes de error serán visibles.
- Las imágenes tendrán texto alternativo pertinente.

## 15. Verificación

La entrega se considerará preparada cuando se compruebe:

- Navegación correcta entre portada, solicitudes y profesionales.
- Renderizado de vistas mediante controladores Spring y Thymeleaf.
- Comportamiento responsivo en celular, tableta y escritorio.
- Formulario de dos pasos con datos válidos e inválidos.
- Registro de una solicitud y aparición inmediata en la tabla administrativa.
- Búsqueda y filtrado por estado.
- Registro, edición y cambio de disponibilidad de profesionales.
- Asignación únicamente de profesionales disponibles.
- Cambio correcto de estado después de la asignación.
- Mensajes comprensibles ante entradas inválidas.
- Pruebas automáticas básicas de controladores y servicios.

## 16. Evolución durante el curso

### Semanas 5 a 8

Spring Boot, Spring Web y Thymeleaf con repositorios en memoria.

### Semanas 9 a 12

Conversión de `Solicitud` y `Profesional` en entidades JPA, persistencia MySQL, relaciones, operaciones CRUD y validación del servidor.

### Semanas 13 a 18

Spring Security, autenticación real y autorización por roles. El enlace de demostración será reemplazado por un inicio de sesión protegido para el administrador.

## 17. Criterios de diseño

- Claridad antes que cantidad de contenido.
- Una acción principal por sección.
- Responsabilidades técnicas separadas.
- Datos clínicos mínimos en el formulario público.
- Alcance compatible con el ritmo y los contenidos del curso.
- Preparación para las unidades posteriores sin implementar funciones prematuramente.
