package pe.edu.utp.alivio.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.alivio.model.*;
import pe.edu.utp.alivio.repository.AccesoSeguimientoRepository;
import pe.edu.utp.alivio.repository.EventoSolicitudRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SeguimientoServiceTest {
    @Autowired SeguimientoService seguimiento;
    @Autowired SolicitudService solicitudes;
    @Autowired AccesoSeguimientoRepository accesos;
    @Autowired EventoSolicitudRepository eventos;
    @Autowired EntityManager entityManager;

    @Test
    void emiteTokenNoPredecibleYGuardaSoloHashConVencimiento() {
        Solicitud solicitud = solicitudes.crear(solicitud());
        String token = seguimiento.emitir(solicitud.getCodigo());
        var acceso = accesos.findBySolicitudId(solicitud.getId()).orElseThrow();
        assertThat(token).matches("[A-Za-z0-9_-]{43}");
        assertThat(acceso.getTokenHash()).matches("[0-9a-f]{64}").doesNotContain(token);
        assertThat(acceso.getExpiraEn()).isAfter(LocalDateTime.now().plusDays(29))
            .isBefore(LocalDateTime.now().plusDays(31));
        entityManager.flush();
        entityManager.clear();
        assertThat(seguimiento.consultar(token).orElseThrow().codigo()).isEqualTo(solicitud.getCodigo());
        assertThat(seguimiento.consultar(solicitud.getCodigo())).isEmpty();
    }

    @Test
    void renovarRevocarYVencerInvalidanElAccesoAnterior() {
        Solicitud solicitud = solicitudes.crear(solicitud());
        String primero = seguimiento.emitir(solicitud.getCodigo());
        String renovado = seguimiento.emitir(solicitud.getCodigo());
        assertThat(renovado).isNotEqualTo(primero);
        assertThat(seguimiento.consultar(primero)).isEmpty();
        assertThat(seguimiento.consultar(renovado)).isPresent();
        seguimiento.revocar(solicitud.getCodigo());
        assertThat(seguimiento.consultar(renovado)).isEmpty();
        String vencido = seguimiento.emitir(solicitud.getCodigo());
        accesos.findBySolicitudId(solicitud.getId()).orElseThrow().setExpiraEn(LocalDateTime.now().minusSeconds(1));
        assertThat(seguimiento.consultar(vencido)).isEmpty();
    }

    @Test
    void registraCambiosValidosSinInventarEventosAnteRechazo() {
        Solicitud solicitud = solicitudes.crear(solicitud());
        String token = seguimiento.emitir(solicitud.getCodigo());
        solicitudes.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_EVALUACION);
        assertThat(seguimiento.consultar(token).orElseThrow().historial())
            .extracting(SeguimientoService.Paso::estado)
            .containsExactly(EstadoSolicitud.PENDIENTE, EstadoSolicitud.EN_EVALUACION);
        assertThatThrownBy(() -> solicitudes.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.FINALIZADA))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(eventos.findBySolicitudIdOrderByIdAsc(solicitud.getId())).hasSize(2);
    }

    @Test
    void borrarSolicitudPendienteEliminaSusAccesosYEventos() {
        Solicitud solicitud = solicitudes.crear(solicitud());
        String token = seguimiento.emitir(solicitud.getCodigo());
        solicitudes.eliminar(solicitud.getCodigo());
        assertThat(seguimiento.consultar(token)).isEmpty();
        assertThat(eventos.findBySolicitudIdOrderByIdAsc(solicitud.getId())).isEmpty();
        assertThat(accesos.findBySolicitudId(solicitud.getId())).isEmpty();
    }

    private Solicitud solicitud() {
        return new Solicitud(null, "Contacto reservado", "987654321", "Paciente reservado", 75,
            TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now().plusDays(1), TurnoAtencion.MANANA,
            "Descripción reservada", null, null);
    }
}
