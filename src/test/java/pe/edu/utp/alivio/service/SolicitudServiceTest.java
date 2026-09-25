package pe.edu.utp.alivio.service;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.model.TurnoAtencion;
import pe.edu.utp.alivio.repository.ProfesionalRepository;
import pe.edu.utp.alivio.repository.SolicitudRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitudServiceTest {
    private SolicitudService service;
    private ProfesionalService profesionalService;

    @BeforeEach
    void setUp() {
        profesionalService = new ProfesionalService(new ProfesionalRepository());
        service = new SolicitudService(new SolicitudRepository(), profesionalService);
    }

    @Test
    void creaSolicitudPendienteSinAsignacionAunqueLlegueConEstadoYCodigo() {
        Solicitud solicitud = solicitudValida();
        solicitud.setCodigo("SOL-999");
        solicitud.setEstado(EstadoSolicitud.FINALIZADA);
        solicitud.setProfesionalAsignado(profesional(true));

        Solicitud creada = service.crear(solicitud);

        assertEquals("SOL-001", creada.getCodigo());
        assertEquals(EstadoSolicitud.PENDIENTE, creada.getEstado());
        assertNull(creada.getProfesionalAsignado());
    }

    @Test
    void rechazaFechaPasada() {
        Solicitud invalida = solicitudValida();
        invalida.setFechaRequerida(LocalDate.now().minusDays(1));

        assertThrows(IllegalArgumentException.class, () -> service.crear(invalida));
        assertTrue(service.listar(null, null).isEmpty());
    }

    @Test
    void validaCamposObligatoriosDeSolicitud() {
        Solicitud sinContacto = solicitudValida();
        sinContacto.setNombreContacto(" ");
        Solicitud sinPaciente = solicitudValida();
        sinPaciente.setNombrePaciente(null);
        Solicitud malTelefono = solicitudValida();
        malTelefono.setTelefonoContacto("123");
        Solicitud sinServicio = solicitudValida();
        sinServicio.setTipoServicio(null);
        Solicitud sinDistrito = solicitudValida();
        sinDistrito.setDistrito(" ");
        Solicitud sinFecha = solicitudValida();
        sinFecha.setFechaRequerida(null);
        Solicitud sinTurno = solicitudValida();
        sinTurno.setTurno(null);
        Solicitud sinDescripcion = solicitudValida();
        sinDescripcion.setDescripcion(" ");

        for (Solicitud invalida : new Solicitud[] {sinContacto, sinPaciente, malTelefono,
                sinServicio, sinDistrito, sinFecha, sinTurno, sinDescripcion}) {
            assertThrows(IllegalArgumentException.class, () -> service.crear(invalida));
        }
        assertTrue(service.listar(null, null).isEmpty());
    }

    @Test
    void filtraPorTextoYEstadoSinDistinguirMayusculas() {
        service.crear(solicitudValida());
        Solicitud otra = solicitudValida();
        otra.setNombreContacto("Elena");
        otra.setNombrePaciente("Luis");
        service.crear(otra);

        assertEquals(1, service.listar(" ROSA ", EstadoSolicitud.PENDIENTE).size());
        assertEquals(1, service.listar("maría", null).size());
        assertEquals(1, service.listar("SOL-002", null).size());
        assertTrue(service.listar("Rosa", EstadoSolicitud.FINALIZADA).isEmpty());
        assertEquals(2, service.listar(null, null).size());
    }

    @Test
    void asignaSoloProfesionalDisponibleTrasEvaluacion() {
        Profesional profesional = profesionalService.registrar(profesional(true));
        Solicitud solicitud = service.crear(solicitudValida());
        service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_EVALUACION);

        Solicitud asignada = service.asignar(solicitud.getCodigo(), profesional.getCodigo());

        assertEquals(EstadoSolicitud.ASIGNADA, asignada.getEstado());
        assertEquals(profesional.getCodigo(), asignada.getProfesionalAsignado().getCodigo());
    }

    @Test
    void rechazaAsignacionAntesDeEvaluacionOSiProfesionalNoDisponible() {
        Profesional disponible = profesionalService.registrar(profesional(true));
        Profesional noDisponible = profesionalService.registrar(profesional(false));
        Solicitud solicitud = service.crear(solicitudValida());

        assertThrows(IllegalArgumentException.class,
            () -> service.asignar(solicitud.getCodigo(), disponible.getCodigo()));
        service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_EVALUACION);
        assertThrows(IllegalArgumentException.class,
            () -> service.asignar(solicitud.getCodigo(), noDisponible.getCodigo()));
        assertEquals(EstadoSolicitud.EN_EVALUACION, service.buscarPorCodigo(solicitud.getCodigo()).getEstado());
        assertNull(service.buscarPorCodigo(solicitud.getCodigo()).getProfesionalAsignado());
    }

    @Test
    void permiteFlujoHastaFinalizadaYRechazaCambiosPosteriores() {
        Profesional profesional = profesionalService.registrar(profesional(true));
        Solicitud solicitud = service.crear(solicitudValida());

        assertEquals(EstadoSolicitud.EN_EVALUACION,
            service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_EVALUACION).getEstado());
        assertEquals(EstadoSolicitud.ASIGNADA,
            service.asignar(solicitud.getCodigo(), profesional.getCodigo()).getEstado());
        assertEquals(EstadoSolicitud.EN_ATENCION,
            service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_ATENCION).getEstado());
        assertEquals(EstadoSolicitud.FINALIZADA,
            service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.FINALIZADA).getEstado());
        assertThrows(IllegalArgumentException.class,
            () -> service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.CANCELADA));
    }

    @Test
    void rechazaSaltarEstadosYAsignadaSinProfesional() {
        Solicitud solicitud = service.crear(solicitudValida());

        assertThrows(IllegalArgumentException.class,
            () -> service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.FINALIZADA));
        assertThrows(IllegalArgumentException.class,
            () -> service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.ASIGNADA));
        service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_EVALUACION);
        assertThrows(IllegalArgumentException.class,
            () -> service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_ATENCION));
        assertThrows(IllegalArgumentException.class,
            () -> service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.ASIGNADA));
        assertEquals(EstadoSolicitud.EN_EVALUACION, solicitud.getEstado());
    }

    @Test
    void permiteCancelarDesdeEstadosActivosPeroNoReabrir() {
        Solicitud pendiente = service.crear(solicitudValida());
        service.actualizarEstado(pendiente.getCodigo(), EstadoSolicitud.CANCELADA);
        assertThrows(IllegalArgumentException.class,
            () -> service.actualizarEstado(pendiente.getCodigo(), EstadoSolicitud.EN_EVALUACION));

        Solicitud evaluacion = service.crear(solicitudValida());
        service.actualizarEstado(evaluacion.getCodigo(), EstadoSolicitud.EN_EVALUACION);
        assertEquals(EstadoSolicitud.CANCELADA,
            service.actualizarEstado(evaluacion.getCodigo(), EstadoSolicitud.CANCELADA).getEstado());

        Profesional profesional = profesionalService.registrar(profesional(true));
        Solicitud asignada = service.crear(solicitudValida());
        service.actualizarEstado(asignada.getCodigo(), EstadoSolicitud.EN_EVALUACION);
        service.asignar(asignada.getCodigo(), profesional.getCodigo());
        assertEquals(EstadoSolicitud.CANCELADA,
            service.actualizarEstado(asignada.getCodigo(), EstadoSolicitud.CANCELADA).getEstado());

        Solicitud atencion = service.crear(solicitudValida());
        service.actualizarEstado(atencion.getCodigo(), EstadoSolicitud.EN_EVALUACION);
        service.asignar(atencion.getCodigo(), profesional.getCodigo());
        service.actualizarEstado(atencion.getCodigo(), EstadoSolicitud.EN_ATENCION);
        assertEquals(EstadoSolicitud.CANCELADA,
            service.actualizarEstado(atencion.getCodigo(), EstadoSolicitud.CANCELADA).getEstado());
    }

    @Test
    void rechazaEstadoNuloYBusquedasInexistentes() {
        Solicitud solicitud = service.crear(solicitudValida());

        assertThrows(IllegalArgumentException.class,
            () -> service.actualizarEstado(solicitud.getCodigo(), null));
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
        assertThrows(IllegalArgumentException.class, () -> service.buscarPorCodigo("SOL-999"));
    }

    private Solicitud solicitudValida() {
        return new Solicitud(null, "Rosa", "987654321", "María", 80,
            TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now(), TurnoAtencion.MANANA,
            "Cuidados diarios", null, null);
    }

    private Profesional profesional(boolean disponible) {
        return new Profesional(null, "Ana Torres", TipoProfesional.TECNICA, "987654321",
            "Surco", TipoServicio.ADULTO_MAYOR, disponible);
    }
}
