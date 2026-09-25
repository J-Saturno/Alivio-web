# Alivio Spring Web Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convertir Alivio en una aplicación Spring Boot independiente con una portada esencial, solicitudes de atención, gestión de profesionales y asignación administrativa en memoria.

**Architecture:** La aplicación seguirá una arquitectura MVC por capas: controladores Spring Web, servicios con reglas de negocio, repositorios en memoria y vistas Thymeleaf estilizadas con Bootstrap. JavaScript se limitará al modal público en dos pasos y a interacciones de presentación; Spring será la fuente de verdad de solicitudes y profesionales.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring Web MVC, Thymeleaf, Bootstrap 5.3.3, JavaScript, CSS, JUnit 5, Spring MVC Test y Maven Wrapper.

**Spec:** `docs/superpowers/specs/2026-09-24-alivio-spring-design.md`

## Global Constraints

- Mostrar únicamente cuatro servicios: adulto mayor, curaciones y postoperatorio, inyectables y tratamientos, y acompañamiento domiciliario.
- No agregar MySQL, JPA, Hibernate, Spring Security, pagos, geolocalización, expedientes clínicos ni productos médicos en esta etapa.
- Mantener `Acceso administrativo - Demo` como enlace directo y visible, sin fingir autenticación con JavaScript.
- No solicitar diagnóstico completo, historia clínica, DNI, recetas ni documentos en el formulario público.
- Bootstrap y Thymeleaf se complementan; Thymeleaf no sustituye Bootstrap.
- Usar listas Java como persistencia temporal y conservar límites claros entre controlador, servicio y repositorio.
- Todos los mensajes visibles deben estar en español y no deben exponer excepciones ni trazas técnicas.
- Verificar anchos de 360 px, 768 px y 1280 px.

---

## Mapa de archivos

### Base del proyecto

- `pom.xml`: dependencias y configuración Maven/Spring Boot.
- `.mvn/wrapper/maven-wrapper.properties`, `mvnw`, `mvnw.cmd`: ejecución reproducible sin Maven global.
- `src/main/java/pe/edu/utp/alivio/AlivioApplication.java`: punto de entrada.
- `src/main/resources/application.properties`: nombre de aplicación y puerto.

### Dominio y datos

- `model/TipoServicio.java`, `EstadoSolicitud.java`, `TipoProfesional.java`, `TurnoAtencion.java`: valores permitidos.
- `model/Solicitud.java`: solicitud pública y asignación.
- `model/Profesional.java`: enfermera o técnica disponible.
- `repository/SolicitudRepository.java`: colección temporal de solicitudes.
- `repository/ProfesionalRepository.java`: colección temporal de profesionales.
- `service/SolicitudService.java`: registro, filtros, asignación y cambios de estado.
- `service/ProfesionalService.java`: registro, edición y disponibilidad.

### Web

- `controller/InicioController.java`: `GET /`.
- `controller/SolicitudPublicaController.java`: `POST /solicitudes`.
- `controller/AdminSolicitudController.java`: `/admin/solicitudes`.
- `controller/AdminProfesionalController.java`: `/admin/profesionales`.
- `web/SolicitudForm.java`, `ProfesionalForm.java`: datos de entrada sin acoplar formularios al dominio.

### Presentación

- `templates/inicio.html`: portada pública y modal en dos pasos.
- `templates/admin/solicitudes.html`: indicadores, filtros, tabla y modales de revisión.
- `templates/admin/profesionales.html`: tabla y modales de alta/edición.
- `templates/fragments/layout.html`: navegación, pie y avisos reutilizables.
- `templates/error/404.html`, `templates/error/500.html`: errores amigables.
- `static/css/alivio.css`: identidad visual y ajustes responsivos.
- `static/js/solicitud-wizard.js`: navegación y validación del modal público.
- `static/js/admin.js`: preparación de modales administrativos.
- `static/img/logo.png`, `static/img/portada.png`: imágenes locales existentes renombradas.

### Pruebas

- `src/test/java/pe/edu/utp/alivio/AlivioApplicationTests.java`: contexto Spring.
- `repository/ProfesionalRepositoryTest.java`, `SolicitudRepositoryTest.java`: comportamiento en memoria.
- `service/ProfesionalServiceTest.java`, `SolicitudServiceTest.java`: reglas de negocio.
- `controller/InicioControllerTest.java`, `SolicitudPublicaControllerTest.java`, `AdminProfesionalControllerTest.java`, `AdminSolicitudControllerTest.java`: rutas y modelos MVC.

---

### Task 1: Crear la base Spring Boot y la ruta de inicio

**Files:**
- Create: `pom.xml`
- Copy: `.mvn/`, `mvnw`, `mvnw.cmd` desde `C:/Users/USER/Downloads/AppSmartPhones/`
- Create: `src/main/java/pe/edu/utp/alivio/AlivioApplication.java`
- Create: `src/main/java/pe/edu/utp/alivio/controller/InicioController.java`
- Create: `src/main/resources/application.properties`
- Create: `src/main/resources/templates/inicio.html`
- Create: `src/test/java/pe/edu/utp/alivio/AlivioApplicationTests.java`
- Create: `src/test/java/pe/edu/utp/alivio/controller/InicioControllerTest.java`

**Interfaces:**
- Consumes: ninguna; establece el proyecto base.
- Produces: aplicación `AlivioApplication` y ruta `GET /` que devuelve la vista `inicio`.

- [ ] **Step 1: Crear Maven Wrapper y el POM**

Copiar los archivos del wrapper desde el proyecto de referencia y crear `pom.xml` con este contenido:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.1.1</version>
        <relativePath/>
    </parent>
    <groupId>pe.edu.utp</groupId>
    <artifactId>alivio-web</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>Alivio Web</name>
    <description>Gestión de cuidados domiciliarios</description>
    <properties><java.version>21</java.version></properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Escribir las pruebas de arranque y ruta principal**

```java
@SpringBootTest
class AlivioApplicationTests {
    @Test void contextLoads() {}
}
```

```java
@WebMvcTest(InicioController.class)
class InicioControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void muestraLaPortada() throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("inicio"));
    }
}
```

- [ ] **Step 3: Ejecutar las pruebas para comprobar el fallo inicial**

Run: `.\mvnw.cmd -Dtest=AlivioApplicationTests,InicioControllerTest test`

Expected: FAIL porque `AlivioApplication` e `InicioController` todavía no existen.

- [ ] **Step 4: Implementar aplicación, controlador y plantilla mínima**

```java
@SpringBootApplication
public class AlivioApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlivioApplication.class, args);
    }
}
```

```java
@Controller
public class InicioController {
    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }
}
```

Crear una plantilla mínima válida con `<html lang="es" xmlns:th="http://www.thymeleaf.org">`, título `Alivio | Cuidado domiciliario` y encabezado `Tu familia cuidada con atención y confianza`.

Configurar:

```properties
spring.application.name=alivio-web
server.port=8080
spring.thymeleaf.cache=false
```

- [ ] **Step 5: Ejecutar las pruebas y confirmar que pasan**

Run: `.\mvnw.cmd -Dtest=AlivioApplicationTests,InicioControllerTest test`

Expected: BUILD SUCCESS; 2 tests sin fallos.

- [ ] **Step 6: Commit**

```bash
git add pom.xml .mvn mvnw mvnw.cmd src/main src/test
git commit -m "build: initialize Alivio Spring Boot app"
```

---

### Task 2: Implementar el registro de profesionales en memoria

**Files:**
- Create: `src/main/java/pe/edu/utp/alivio/model/TipoProfesional.java`
- Create: `src/main/java/pe/edu/utp/alivio/model/TipoServicio.java`
- Create: `src/main/java/pe/edu/utp/alivio/model/Profesional.java`
- Create: `src/main/java/pe/edu/utp/alivio/repository/ProfesionalRepository.java`
- Create: `src/main/java/pe/edu/utp/alivio/service/ProfesionalService.java`
- Test: `src/test/java/pe/edu/utp/alivio/repository/ProfesionalRepositoryTest.java`
- Test: `src/test/java/pe/edu/utp/alivio/service/ProfesionalServiceTest.java`

**Interfaces:**
- Consumes: contexto Spring creado en Task 1.
- Produces: `ProfesionalService.registrar`, `actualizar`, `listarTodos`, `listarDisponibles`, `buscarPorCodigo` y `cambiarDisponibilidad`.

- [ ] **Step 1: Crear enumeraciones compartidas**

```java
public enum TipoProfesional { LICENCIADA, TECNICA }
```

```java
public enum TipoServicio {
    ADULTO_MAYOR,
    CURACIONES_POSTOPERATORIO,
    INYECTABLES_TRATAMIENTOS,
    ACOMPANAMIENTO_DOMICILIARIO
}
```

- [ ] **Step 2: Escribir pruebas del repositorio**

```java
@Test
void asignaCodigoYPermiteBuscar() {
    ProfesionalRepository repository = new ProfesionalRepository();
    Profesional guardada = repository.save(new Profesional(null, "Ana Torres",
        TipoProfesional.TECNICA, "987654321", "Surco",
        TipoServicio.ADULTO_MAYOR, true));

    assertEquals("PRO-001", guardada.getCodigo());
    assertEquals("Ana Torres", repository.findByCodigo("PRO-001").orElseThrow().getNombreCompleto());
}
```

- [ ] **Step 3: Ejecutar la prueba y confirmar el fallo**

Run: `.\mvnw.cmd -Dtest=ProfesionalRepositoryTest test`

Expected: FAIL porque `Profesional` y `ProfesionalRepository` no existen.

- [ ] **Step 4: Implementar modelo y repositorio**

`Profesional` tendrá constructor completo, constructor vacío, getters y setters para: `codigo`, `nombreCompleto`, `tipoProfesional`, `telefono`, `zonaCobertura`, `especialidad` y `disponible`.

```java
@Repository
public class ProfesionalRepository {
    private final List<Profesional> profesionales = new ArrayList<>();
    private final AtomicInteger secuencia = new AtomicInteger(1);

    public synchronized Profesional save(Profesional profesional) {
        if (profesional.getCodigo() == null || profesional.getCodigo().isBlank()) {
            profesional.setCodigo("PRO-%03d".formatted(secuencia.getAndIncrement()));
            profesionales.add(profesional);
            return profesional;
        }
        Profesional existente = findByCodigo(profesional.getCodigo()).orElseThrow();
        profesionales.set(profesionales.indexOf(existente), profesional);
        return profesional;
    }

    public List<Profesional> findAll() { return new ArrayList<>(profesionales); }

    public Optional<Profesional> findByCodigo(String codigo) {
        return profesionales.stream().filter(p -> p.getCodigo().equals(codigo)).findFirst();
    }
}
```

- [ ] **Step 5: Ejecutar la prueba del repositorio**

Run: `.\mvnw.cmd -Dtest=ProfesionalRepositoryTest test`

Expected: PASS.

- [ ] **Step 6: Escribir pruebas del servicio**

```java
@Test
void listaSoloProfesionalesDisponibles() {
    ProfesionalRepository repository = new ProfesionalRepository();
    ProfesionalService service = new ProfesionalService(repository);
    service.registrar(profesional("Ana", true));
    service.registrar(profesional("Rosa", false));

    assertEquals(List.of("Ana"), service.listarDisponibles().stream()
        .map(Profesional::getNombreCompleto).toList());
}

@Test
void rechazaTelefonoInvalido() {
    ProfesionalService service = new ProfesionalService(new ProfesionalRepository());
    Profesional profesional = profesional("Ana", true);
    profesional.setTelefono("123");
    assertThrows(IllegalArgumentException.class, () -> service.registrar(profesional));
}
```

- [ ] **Step 7: Implementar reglas del servicio**

```java
@Service
public class ProfesionalService {
    private final ProfesionalRepository repository;

    public ProfesionalService(ProfesionalRepository repository) { this.repository = repository; }

    public Profesional registrar(Profesional profesional) {
        validar(profesional);
        profesional.setCodigo(null);
        return repository.save(profesional);
    }

    public Profesional actualizar(String codigo, Profesional cambios) {
        validar(cambios);
        cambios.setCodigo(codigo);
        return repository.save(cambios);
    }

    public List<Profesional> listarTodos() { return repository.findAll(); }

    public List<Profesional> listarDisponibles() {
        return repository.findAll().stream().filter(Profesional::isDisponible).toList();
    }

    public Profesional buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo)
            .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrada"));
    }

    public Profesional cambiarDisponibilidad(String codigo) {
        Profesional profesional = buscarPorCodigo(codigo);
        profesional.setDisponible(!profesional.isDisponible());
        return repository.save(profesional);
    }

    private void validar(Profesional p) {
        if (p.getNombreCompleto() == null || p.getNombreCompleto().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");
        if (p.getTelefono() == null || !p.getTelefono().matches("9\\d{8}"))
            throw new IllegalArgumentException("El teléfono debe tener nueve dígitos");
        if (p.getTipoProfesional() == null || p.getEspecialidad() == null)
            throw new IllegalArgumentException("Selecciona tipo y especialidad");
        if (p.getZonaCobertura() == null || p.getZonaCobertura().isBlank())
            throw new IllegalArgumentException("La zona de cobertura es obligatoria");
    }
}
```

- [ ] **Step 8: Ejecutar las pruebas de profesionales**

Run: `.\mvnw.cmd -Dtest=ProfesionalRepositoryTest,ProfesionalServiceTest test`

Expected: BUILD SUCCESS.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/pe/edu/utp/alivio/model src/main/java/pe/edu/utp/alivio/repository/ProfesionalRepository.java src/main/java/pe/edu/utp/alivio/service/ProfesionalService.java src/test
git commit -m "feat: add in-memory professional registry"
```

---

### Task 3: Implementar solicitudes, filtros, asignación y estados

**Files:**
- Create: `src/main/java/pe/edu/utp/alivio/model/EstadoSolicitud.java`
- Create: `src/main/java/pe/edu/utp/alivio/model/TurnoAtencion.java`
- Create: `src/main/java/pe/edu/utp/alivio/model/Solicitud.java`
- Create: `src/main/java/pe/edu/utp/alivio/repository/SolicitudRepository.java`
- Create: `src/main/java/pe/edu/utp/alivio/service/SolicitudService.java`
- Test: `src/test/java/pe/edu/utp/alivio/repository/SolicitudRepositoryTest.java`
- Test: `src/test/java/pe/edu/utp/alivio/service/SolicitudServiceTest.java`

**Interfaces:**
- Consumes: `ProfesionalService.buscarPorCodigo(String)` de Task 2.
- Produces: `SolicitudService.crear`, `listar`, `buscarPorCodigo`, `asignar` y `actualizarEstado`.

- [ ] **Step 1: Crear estados y turnos**

```java
public enum EstadoSolicitud {
    PENDIENTE, EN_EVALUACION, ASIGNADA, EN_ATENCION, FINALIZADA, CANCELADA
}
```

```java
public enum TurnoAtencion { MANANA, TARDE, NOCHE, DOCE_HORAS, VEINTICUATRO_HORAS }
```

- [ ] **Step 2: Escribir la prueba de registro**

```java
@Test
void creaSolicitudPendienteConCodigo() {
    SolicitudRepository repository = new SolicitudRepository();
    Solicitud guardada = repository.save(solicitudValida());
    assertEquals("SOL-001", guardada.getCodigo());
    assertEquals(EstadoSolicitud.PENDIENTE, guardada.getEstado());
}
```

- [ ] **Step 3: Ejecutar la prueba y confirmar el fallo**

Run: `.\mvnw.cmd -Dtest=SolicitudRepositoryTest test`

Expected: FAIL porque las clases de solicitud todavía no existen.

- [ ] **Step 4: Implementar modelo y repositorio**

`Solicitud` tendrá constructor vacío, constructor completo, getters y setters para: `codigo`, `nombreContacto`, `telefonoContacto`, `nombrePaciente`, `edadPaciente` como `Integer`, `tipoServicio`, `distrito`, `fechaRequerida` como `LocalDate`, `turno`, `descripcion`, `estado` y `profesionalAsignado`.

`SolicitudRepository.save` generará `SOL-%03d`, establecerá `PENDIENTE` cuando el estado sea nulo y actualizará por código con la misma estrategia de `ProfesionalRepository`. También expondrá `findAll()` y `findByCodigo(String)`.

- [ ] **Step 5: Ejecutar la prueba del repositorio**

Run: `.\mvnw.cmd -Dtest=SolicitudRepositoryTest test`

Expected: PASS.

- [ ] **Step 6: Escribir pruebas de reglas y filtros**

```java
@Test
void asignaSoloProfesionalDisponible() {
    Profesional profesional = profesionalDisponible();
    profesional = profesionalService.registrar(profesional);
    Solicitud solicitud = service.crear(solicitudValida());
    service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_EVALUACION);

    Solicitud asignada = service.asignar(solicitud.getCodigo(), profesional.getCodigo());

    assertEquals(EstadoSolicitud.ASIGNADA, asignada.getEstado());
    assertEquals(profesional.getCodigo(), asignada.getProfesionalAsignado().getCodigo());
}

@Test
void rechazaFechaPasada() {
    Solicitud invalida = solicitudValida();
    invalida.setFechaRequerida(LocalDate.now().minusDays(1));
    assertThrows(IllegalArgumentException.class, () -> service.crear(invalida));
}

@Test
void filtraPorTextoYEstado() {
    service.crear(solicitudValida());
    assertEquals(1, service.listar("Rosa", EstadoSolicitud.PENDIENTE).size());
    assertTrue(service.listar("Rosa", EstadoSolicitud.FINALIZADA).isEmpty());
}

@Test
void rechazaSaltarDePendienteAFinalizada() {
    Solicitud solicitud = service.crear(solicitudValida());
    assertThrows(IllegalArgumentException.class,
        () -> service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.FINALIZADA));
}
```

- [ ] **Step 7: Implementar servicio de solicitudes**

```java
@Service
public class SolicitudService {
    private final SolicitudRepository repository;
    private final ProfesionalService profesionalService;

    public SolicitudService(SolicitudRepository repository, ProfesionalService profesionalService) {
        this.repository = repository;
        this.profesionalService = profesionalService;
    }

    public Solicitud crear(Solicitud solicitud) {
        validar(solicitud);
        solicitud.setCodigo(null);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setProfesionalAsignado(null);
        return repository.save(solicitud);
    }

    public List<Solicitud> listar(String texto, EstadoSolicitud estado) {
        String filtro = texto == null ? "" : texto.toLowerCase().trim();
        return repository.findAll().stream()
            .filter(s -> estado == null || s.getEstado() == estado)
            .filter(s -> filtro.isBlank()
                || s.getCodigo().toLowerCase().contains(filtro)
                || s.getNombreContacto().toLowerCase().contains(filtro)
                || s.getNombrePaciente().toLowerCase().contains(filtro))
            .toList();
    }

    public Solicitud buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
    }

    public Solicitud asignar(String solicitudCodigo, String profesionalCodigo) {
        Solicitud solicitud = buscarPorCodigo(solicitudCodigo);
        Profesional profesional = profesionalService.buscarPorCodigo(profesionalCodigo);
        if (!profesional.isDisponible())
            throw new IllegalArgumentException("Selecciona una profesional disponible");
        if (solicitud.getEstado() != EstadoSolicitud.EN_EVALUACION)
            throw new IllegalArgumentException("La solicitud debe estar en evaluación antes de asignar");
        solicitud.setProfesionalAsignado(profesional);
        solicitud.setEstado(EstadoSolicitud.ASIGNADA);
        return repository.save(solicitud);
    }

    public Solicitud actualizarEstado(String codigo, EstadoSolicitud nuevoEstado) {
        Solicitud solicitud = buscarPorCodigo(codigo);
        if (nuevoEstado == EstadoSolicitud.ASIGNADA && solicitud.getProfesionalAsignado() == null)
            throw new IllegalArgumentException("Asigna una profesional antes de continuar");
        if (!transicionPermitida(solicitud.getEstado(), nuevoEstado))
            throw new IllegalArgumentException("El cambio de estado solicitado no está permitido");
        solicitud.setEstado(nuevoEstado);
        return repository.save(solicitud);
    }

    private boolean transicionPermitida(EstadoSolicitud actual, EstadoSolicitud siguiente) {
        return switch (actual) {
            case PENDIENTE -> siguiente == EstadoSolicitud.EN_EVALUACION
                || siguiente == EstadoSolicitud.CANCELADA;
            case EN_EVALUACION -> siguiente == EstadoSolicitud.CANCELADA;
            case ASIGNADA -> siguiente == EstadoSolicitud.EN_ATENCION
                || siguiente == EstadoSolicitud.CANCELADA;
            case EN_ATENCION -> siguiente == EstadoSolicitud.FINALIZADA
                || siguiente == EstadoSolicitud.CANCELADA;
            case FINALIZADA, CANCELADA -> false;
        };
    }
}
```

La función privada `validar` comprobará nombres no vacíos, teléfono con `9\\d{8}`, servicio, distrito, turno, descripción y `fechaRequerida` igual o posterior a `LocalDate.now()`.

- [ ] **Step 8: Ejecutar pruebas de solicitudes**

Run: `.\mvnw.cmd -Dtest=SolicitudRepositoryTest,SolicitudServiceTest test`

Expected: BUILD SUCCESS.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/pe/edu/utp/alivio/model src/main/java/pe/edu/utp/alivio/repository/SolicitudRepository.java src/main/java/pe/edu/utp/alivio/service/SolicitudService.java src/test
git commit -m "feat: add request workflow and assignment rules"
```

---

### Task 4: Construir la portada esencial y el formulario público

**Files:**
- Create: `src/main/java/pe/edu/utp/alivio/web/SolicitudForm.java`
- Create: `src/main/java/pe/edu/utp/alivio/controller/SolicitudPublicaController.java`
- Modify: `src/main/java/pe/edu/utp/alivio/controller/InicioController.java`
- Replace: `src/main/resources/templates/inicio.html`
- Create: `src/main/resources/templates/fragments/layout.html`
- Create: `src/main/resources/static/css/alivio.css`
- Create: `src/main/resources/static/js/solicitud-wizard.js`
- Copy: `imagen/logoo.png` to `src/main/resources/static/img/logo.png`
- Copy: `imagen/portadaPrin.png` to `src/main/resources/static/img/portada.png`
- Test: `src/test/java/pe/edu/utp/alivio/controller/SolicitudPublicaControllerTest.java`

**Interfaces:**
- Consumes: `SolicitudService.crear(Solicitud)` y enumeraciones de Tasks 2-3.
- Produces: `POST /solicitudes`, atributo flash `solicitudCreada` y portada completa.

- [ ] **Step 1: Escribir pruebas MVC del formulario público**

```java
@WebMvcTest({InicioController.class, SolicitudPublicaController.class})
class SolicitudPublicaControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean SolicitudService solicitudService;

    @Test
    void registraSolicitudYRedirigeConConfirmacion() throws Exception {
        Solicitud creada = new Solicitud();
        creada.setCodigo("SOL-001");
        when(solicitudService.crear(any())).thenReturn(creada);

        mvc.perform(post("/solicitudes")
            .param("nombreContacto", "María Pérez")
            .param("telefonoContacto", "987654321")
            .param("nombrePaciente", "Rosa Pérez")
            .param("tipoServicio", "ADULTO_MAYOR")
            .param("distrito", "Surco")
            .param("fechaRequerida", LocalDate.now().plusDays(1).toString())
            .param("turno", "MANANA")
            .param("descripcion", "Apoyo con movilidad y medicación"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("solicitudCreada", "SOL-001"));
    }
}
```

- [ ] **Step 2: Ejecutar la prueba y confirmar el fallo**

Run: `.\mvnw.cmd -Dtest=SolicitudPublicaControllerTest test`

Expected: FAIL porque el formulario y el controlador aún no existen.

- [ ] **Step 3: Implementar DTO y controlador**

`SolicitudForm` tendrá los mismos campos públicos del modal y un método `toSolicitud()` que copie sus valores a un objeto `Solicitud` nuevo.

```java
@Controller
public class SolicitudPublicaController {
    private final SolicitudService service;

    public SolicitudPublicaController(SolicitudService service) { this.service = service; }

    @PostMapping("/solicitudes")
    public String registrar(@ModelAttribute SolicitudForm form,
                            RedirectAttributes redirect) {
        try {
            Solicitud creada = service.crear(form.toSolicitud());
            redirect.addFlashAttribute("solicitudCreada", creada.getCodigo());
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("errorSolicitud", error.getMessage());
            redirect.addFlashAttribute("formAnterior", form);
        }
        return "redirect:/";
    }
}
```

`InicioController.inicio(Model)` agregará `tiposServicio`, `turnos` y un `SolicitudForm` solo cuando no exista `formAnterior`.

- [ ] **Step 4: Crear fragmentos y portada Thymeleaf**

La plantilla debe contener exactamente estas secciones, en este orden:

```html
<header th:replace="~{fragments/layout :: navbar}"></header>
<main>
  <section class="hero" aria-labelledby="hero-title">...</section>
  <section class="trust-strip" aria-label="Compromisos de Alivio">...</section>
  <section id="servicios" class="section-padding">...</section>
  <section id="como-funciona" class="section-padding section-soft">...</section>
  <section class="section-padding human-care">...</section>
  <section class="final-cta">...</section>
</main>
<footer th:replace="~{fragments/layout :: footer}"></footer>
<div class="modal fade" id="modalSolicitud">...</div>
<div class="modal fade" id="modalConfirmacion" th:if="${solicitudCreada}">...</div>
```

La navegación tendrá `Inicio`, `Servicios`, `Cómo funciona`, el botón `Solicitar atención` y el enlace `/admin/solicitudes` con texto `Acceso administrativo - Demo`.

- [ ] **Step 5: Implementar el modal de dos pasos**

El primer panel tendrá contacto, paciente, edad opcional y servicio. El segundo tendrá distrito, fecha, turno y descripción. Los botones usarán `type="button"` para avanzar/retroceder y `type="submit"` solo para registrar.

```javascript
const form = document.querySelector('#formSolicitud');
const panels = [...document.querySelectorAll('[data-wizard-panel]')];
let current = 0;

function showPanel(index) {
  current = index;
  panels.forEach((panel, position) => panel.hidden = position !== index);
  document.querySelector('#wizardStep').textContent = `Paso ${index + 1} de 2`;
}

function firstStepIsValid() {
  return ['nombreContacto', 'telefonoContacto', 'nombrePaciente', 'tipoServicio']
    .map(id => document.getElementById(id))
    .every(field => field.reportValidity());
}

document.querySelector('#wizardNext')?.addEventListener('click', () => {
  if (firstStepIsValid()) showPanel(1);
});
document.querySelector('#wizardBack')?.addEventListener('click', () => showPanel(0));
const confirmation = document.querySelector('#modalConfirmacion');
if (confirmation) bootstrap.Modal.getOrCreateInstance(confirmation).show();
showPanel(0);
```

Configurar `min` de la fecha con la fecha local actual y patrón de teléfono `[9][0-9]{8}`.

- [ ] **Step 6: Crear estilos y comportamiento responsivo**

Definir variables `--alivio-primary`, `--alivio-primary-dark`, `--alivio-soft`, `--alivio-ink` y `--alivio-muted`. Limitar el texto del hero a 620 px, usar tarjetas de igual altura y reducir títulos/botones en `@media (max-width: 576px)`.

- [ ] **Step 7: Ejecutar pruebas MVC y suite completa**

Run: `.\mvnw.cmd -Dtest=InicioControllerTest,SolicitudPublicaControllerTest test`

Expected: PASS.

Run: `.\mvnw.cmd test`

Expected: BUILD SUCCESS.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/pe/edu/utp/alivio/controller src/main/java/pe/edu/utp/alivio/web src/main/resources src/test
git commit -m "feat: build focused public request experience"
```

---

### Task 5: Construir el panel de profesionales

**Files:**
- Create: `src/main/java/pe/edu/utp/alivio/web/ProfesionalForm.java`
- Create: `src/main/java/pe/edu/utp/alivio/controller/AdminProfesionalController.java`
- Create: `src/main/resources/templates/admin/profesionales.html`
- Create: `src/main/resources/static/js/admin.js`
- Test: `src/test/java/pe/edu/utp/alivio/controller/AdminProfesionalControllerTest.java`

**Interfaces:**
- Consumes: métodos de `ProfesionalService` de Task 2.
- Produces: rutas `GET /admin/profesionales`, `POST /admin/profesionales`, `POST /admin/profesionales/{codigo}` y `POST /admin/profesionales/{codigo}/disponibilidad`.

- [ ] **Step 1: Escribir pruebas MVC del registro profesional**

```java
@Test
void muestraTablaDeProfesionales() throws Exception {
    when(service.listarTodos()).thenReturn(List.of(profesional));
    mvc.perform(get("/admin/profesionales"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/profesionales"))
        .andExpect(model().attribute("profesionales", hasSize(1)));
}

@Test
void registraProfesionalYRedirige() throws Exception {
    mvc.perform(post("/admin/profesionales")
        .param("nombreCompleto", "Ana Torres")
        .param("tipoProfesional", "TECNICA")
        .param("telefono", "987654321")
        .param("zonaCobertura", "Surco")
        .param("especialidad", "ADULTO_MAYOR")
        .param("disponible", "true"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/profesionales"));
    verify(service).registrar(any(Profesional.class));
}
```

- [ ] **Step 2: Ejecutar pruebas y confirmar el fallo**

Run: `.\mvnw.cmd -Dtest=AdminProfesionalControllerTest test`

Expected: FAIL porque no existen controlador, formulario ni plantilla.

- [ ] **Step 3: Implementar DTO y controlador**

`ProfesionalForm` copiará sus siete campos mediante `toProfesional()`. El controlador cargará `profesionales`, `tiposProfesional`, `especialidades` y `nuevoProfesional`.

```java
@PostMapping("/{codigo}/disponibilidad")
public String cambiarDisponibilidad(@PathVariable String codigo, RedirectAttributes redirect) {
    service.cambiarDisponibilidad(codigo);
    redirect.addFlashAttribute("mensaje", "Disponibilidad actualizada");
    return "redirect:/admin/profesionales";
}
```

Las acciones de registro y edición capturarán `IllegalArgumentException` y devolverán mensajes flash legibles.

- [ ] **Step 4: Crear tabla y modales Bootstrap**

La tabla mostrará: código, nombre, tipo, zona, especialidad, disponibilidad y acciones. El modal `modalProfesional` registrará; `modalEditarProfesional` recibirá valores mediante atributos `data-*` y `admin.js`.

```javascript
document.querySelector('#modalEditarProfesional')?.addEventListener('show.bs.modal', event => {
  const button = event.relatedTarget;
  const form = event.currentTarget.querySelector('form');
  form.action = `/admin/profesionales/${button.dataset.codigo}`;
  form.elements.nombreCompleto.value = button.dataset.nombreCompleto;
  form.elements.telefono.value = button.dataset.telefono;
  form.elements.zonaCobertura.value = button.dataset.zonaCobertura;
  form.elements.tipoProfesional.value = button.dataset.tipo;
  form.elements.especialidad.value = button.dataset.especialidad;
  form.elements.disponible.checked = button.dataset.disponible === 'true';
});
```

- [ ] **Step 5: Ejecutar pruebas del controlador y suite**

Run: `.\mvnw.cmd -Dtest=AdminProfesionalControllerTest test`

Expected: PASS.

Run: `.\mvnw.cmd test`

Expected: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/pe/edu/utp/alivio/controller/AdminProfesionalController.java src/main/java/pe/edu/utp/alivio/web/ProfesionalForm.java src/main/resources/templates/admin/profesionales.html src/main/resources/static/js/admin.js src/test
git commit -m "feat: add professional administration"
```

---

### Task 6: Construir la tabla enfocada de solicitudes

**Files:**
- Create: `src/main/java/pe/edu/utp/alivio/controller/AdminSolicitudController.java`
- Create: `src/main/resources/templates/admin/solicitudes.html`
- Modify: `src/main/resources/static/js/admin.js`
- Test: `src/test/java/pe/edu/utp/alivio/controller/AdminSolicitudControllerTest.java`

**Interfaces:**
- Consumes: `SolicitudService` y `ProfesionalService.listarDisponibles()`.
- Produces: listado, filtros, asignación y actualización de estado bajo `/admin/solicitudes`.

- [ ] **Step 1: Escribir pruebas MVC administrativas**

```java
@Test
void muestraSolicitudesConFiltrosYPersonalDisponible() throws Exception {
    when(solicitudService.listar("", null)).thenReturn(List.of(solicitud));
    when(profesionalService.listarDisponibles()).thenReturn(List.of(profesional));
    mvc.perform(get("/admin/solicitudes"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/solicitudes"))
        .andExpect(model().attribute("solicitudes", hasSize(1)))
        .andExpect(model().attribute("profesionalesDisponibles", hasSize(1)));
}

@Test
void asignaProfesionalYRedirige() throws Exception {
    mvc.perform(post("/admin/solicitudes/SOL-001/asignar")
        .param("profesionalCodigo", "PRO-001"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/solicitudes"));
    verify(solicitudService).asignar("SOL-001", "PRO-001");
}
```

- [ ] **Step 2: Ejecutar pruebas y confirmar el fallo**

Run: `.\mvnw.cmd -Dtest=AdminSolicitudControllerTest test`

Expected: FAIL porque no existe el controlador administrativo.

- [ ] **Step 3: Implementar controlador de solicitudes**

```java
@GetMapping
public String listar(@RequestParam(defaultValue = "") String q,
                     @RequestParam(required = false) EstadoSolicitud estado,
                     Model model) {
    List<Solicitud> solicitudes = solicitudService.listar(q, estado);
    model.addAttribute("solicitudes", solicitudes);
    model.addAttribute("profesionalesDisponibles", profesionalService.listarDisponibles());
    model.addAttribute("estados", EstadoSolicitud.values());
    model.addAttribute("q", q);
    model.addAttribute("estadoSeleccionado", estado);
    model.addAttribute("pendientes", solicitudes.stream().filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE).count());
    model.addAttribute("asignadas", solicitudes.stream().filter(s -> s.getEstado() == EstadoSolicitud.ASIGNADA).count());
    model.addAttribute("finalizadas", solicitudes.stream().filter(s -> s.getEstado() == EstadoSolicitud.FINALIZADA).count());
    return "admin/solicitudes";
}
```

Agregar `POST /{codigo}/asignar` y `POST /{codigo}/estado`; ambos capturan errores y devuelven mensajes flash.

- [ ] **Step 4: Crear indicadores, filtros y tabla responsiva**

La vista tendrá tres tarjetas de indicadores, formulario GET de filtros, `.table-responsive` y columnas: código, paciente, servicio, fecha, estado y acción. Cada botón `Revisar` abrirá un modal con todos los datos permitidos, el selector de profesionales y el formulario de estado.

Usar `th:text`, `th:each`, `th:value`, `th:action` y `th:selected`; no construir filas mediante JavaScript.

- [ ] **Step 5: Completar la preparación del modal en `admin.js`**

El botón de cada fila expondrá únicamente datos mediante `data-*`. Al abrir el modal, JavaScript establecerá textos y acciones de los formularios:

```javascript
document.querySelector('#modalSolicitudDetalle')?.addEventListener('show.bs.modal', event => {
  const source = event.relatedTarget;
  const modal = event.currentTarget;
  modal.querySelector('[data-detail="codigo"]').textContent = source.dataset.codigo;
  modal.querySelector('[data-detail="paciente"]').textContent = source.dataset.paciente;
  modal.querySelector('[data-detail="descripcion"]').textContent = source.dataset.descripcion;
  modal.querySelector('[data-form="asignar"]').action = `/admin/solicitudes/${source.dataset.codigo}/asignar`;
  modal.querySelector('[data-form="estado"]').action = `/admin/solicitudes/${source.dataset.codigo}/estado`;
});
```

- [ ] **Step 6: Ejecutar pruebas y suite completa**

Run: `.\mvnw.cmd -Dtest=AdminSolicitudControllerTest test`

Expected: PASS.

Run: `.\mvnw.cmd test`

Expected: BUILD SUCCESS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/pe/edu/utp/alivio/controller/AdminSolicitudController.java src/main/resources/templates/admin/solicitudes.html src/main/resources/static/js/admin.js src/test
git commit -m "feat: add focused request administration"
```

---

### Task 7: Añadir datos de demostración, errores y accesibilidad

**Files:**
- Create: `src/main/java/pe/edu/utp/alivio/config/DatosDemoConfig.java`
- Create: `src/main/java/pe/edu/utp/alivio/controller/GlobalExceptionHandler.java`
- Create: `src/main/resources/templates/error/404.html`
- Create: `src/main/resources/templates/error/500.html`
- Modify: `src/main/resources/templates/inicio.html`
- Modify: `src/main/resources/templates/admin/solicitudes.html`
- Modify: `src/main/resources/templates/admin/profesionales.html`
- Modify: `src/main/resources/static/css/alivio.css`
- Test: `src/test/java/pe/edu/utp/alivio/controller/ErrorPageTest.java`

**Interfaces:**
- Consumes: servicios completos de Tasks 2-6.
- Produces: demostración útil al arrancar y respuestas de error sin detalles técnicos.

- [ ] **Step 1: Escribir prueba del manejo de errores**

```java
@Test
void muestraErrorAmigableAnteRecursoInexistente() throws Exception {
    mvc.perform(post("/admin/solicitudes/SOL-999/estado")
        .param("estado", "FINALIZADA"))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("error", "Solicitud no encontrada"));
}
```

- [ ] **Step 2: Ejecutar prueba y confirmar el fallo**

Run: `.\mvnw.cmd -Dtest=ErrorPageTest test`

Expected: FAIL hasta conectar el manejo de `IllegalArgumentException`.

- [ ] **Step 3: Crear datos de demostración**

`DatosDemoConfig` expondrá un `CommandLineRunner` que registre tres profesionales disponibles/no disponibles y dos solicitudes solo cuando ambos repositorios estén vacíos. Usar nombres ficticios y fechas `LocalDate.now().plusDays(1)` y `plusDays(2)`.

- [ ] **Step 4: Crear páginas y manejo de errores**

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String errorGeneral(Exception error, Model model) {
        model.addAttribute("mensaje", "No pudimos completar la operación. Inténtalo nuevamente.");
        return "error/500";
    }
}
```

Configurar `server.error.whitelabel.enabled=false` y crear páginas 404/500 con botón de regreso, sin imprimir `${exception}`, `${trace}` ni `${message}` técnico.

- [ ] **Step 5: Completar accesibilidad y responsividad**

Verificar y corregir:

- Un único `h1` por vista y jerarquía de encabezados sin saltos.
- `label for` para cada control y `aria-describedby` para ayudas/errores.
- `aria-live="polite"` en mensajes flash.
- `alt` descriptivo en logo y portada.
- estados `:focus-visible` con borde de al menos 2 px.
- modal con `modal-dialog-scrollable`.
- tablas dentro de `.table-responsive`.
- botones táctiles con altura mínima de 44 px en móvil.

- [ ] **Step 6: Ejecutar pruebas y análisis manual de HTML**

Run: `.\mvnw.cmd test`

Expected: BUILD SUCCESS.

Run: `rg -n "onclick=|javascript:|\$\{exception\}|\$\{trace\}" src/main/resources/templates`

Expected: sin coincidencias.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/pe/edu/utp/alivio/config src/main/java/pe/edu/utp/alivio/controller/GlobalExceptionHandler.java src/main/resources src/test
git commit -m "feat: polish demo data errors and accessibility"
```

---

### Task 8: Retirar la web antigua y realizar la verificación integral

**Files:**
- Delete after migration verification: `index.html`, `Gesolicitud.html`, `ayuda/`, `contacto/`, `nosotros/`, `productos/`, `servicios/`, `CSS/`, `JS/`, `imagen/`
- Modify: `.gitignore`
- Modify: `README.md` or create it if absent
- Test: entire `src/test/java/pe/edu/utp/alivio/`

**Interfaces:**
- Consumes: aplicación funcional de Tasks 1-7.
- Produces: repositorio limpio, documentado y listo para demostrar.

- [ ] **Step 1: Verificar que todos los recursos útiles fueron migrados**

Run: `rg -n "CSS/|JS/|imagen/|productos|Gesolicitud" src pom.xml`

Expected: sin referencias a rutas antiguas.

- [ ] **Step 2: Ejecutar la suite antes de retirar archivos antiguos**

Run: `.\mvnw.cmd clean test`

Expected: BUILD SUCCESS.

- [ ] **Step 3: Retirar únicamente los archivos estáticos sustituidos**

Eliminar las rutas listadas en `Files` solo después de los dos pasos anteriores. Mantener `docs/`, `.git/`, `.gitattributes`, Maven Wrapper, `pom.xml` y `src/`.

- [ ] **Step 4: Documentar ejecución y alcance**

Crear `README.md` con:

````markdown
# Alivio Web

Aplicación académica de gestión de cuidados domiciliarios construida con Spring Boot, Spring Web, Thymeleaf y Bootstrap.

## Ejecutar

```powershell
.\mvnw.cmd spring-boot:run
```

Abrir `http://localhost:8080`.

## Acceso administrativo de demostración

Desde la portada, seleccionar `Acceso administrativo - Demo`. Esta etapa no implementa autenticación real.

## Datos

Las solicitudes y profesionales se guardan temporalmente en memoria y se reinician al detener la aplicación. MySQL y Spring Security pertenecen a unidades posteriores del curso.
````

- [ ] **Step 5: Arrancar la aplicación para pruebas visuales**

Run: `.\mvnw.cmd spring-boot:run`

Expected: aplicación disponible en `http://localhost:8080` sin errores de plantilla.

- [ ] **Step 6: Verificar flujo público**

En 360 px, 768 px y 1280 px:

1. Abrir la portada y comprobar que no haya desplazamiento horizontal.
2. Abrir el modal, intentar avanzar con campos vacíos y confirmar errores visibles.
3. Completar ambos pasos, retroceder y confirmar que los datos permanecen.
4. Registrar una solicitud válida y anotar su código de confirmación.

Expected: navegación fluida, modal desplazable y confirmación con código `SOL-xxx`.

- [ ] **Step 7: Verificar flujo administrativo**

1. Entrar mediante `Acceso administrativo - Demo`.
2. Encontrar la solicitud nueva mediante búsqueda.
3. Filtrar por `PENDIENTE`.
4. Abrir su detalle.
5. Cambiar la solicitud a `EN_EVALUACION`.
6. Asignar una profesional disponible.
7. Confirmar cambio a `ASIGNADA`.
8. Registrar una nueva técnica, editarla y cambiar su disponibilidad.

Expected: las tablas se actualizan después de cada redirección y ninguna acción muestra errores técnicos.

- [ ] **Step 8: Ejecutar la verificación final automatizada**

Run: `.\mvnw.cmd clean test`

Expected: BUILD SUCCESS, cero fallos y cero errores.

Run: `git status --short`

Expected: solo cambios de esta tarea antes del commit.

- [ ] **Step 9: Commit**

```bash
git add -A
git commit -m "chore: complete Alivio Spring migration"
```
