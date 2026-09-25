package pe.edu.utp.alivio.repository;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.model.TurnoAtencion;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolicitudRepositoryTest {
    @Test
    void creaSolicitudPendienteConCodigo() {
        SolicitudRepository repository = new SolicitudRepository();
        Solicitud guardada = repository.save(solicitudValida());

        assertEquals("SOL-001", guardada.getCodigo());
        assertEquals(EstadoSolicitud.PENDIENTE, guardada.getEstado());
        assertEquals("Rosa", repository.findByCodigo("SOL-001").orElseThrow().getNombreContacto());
    }

    @Test
    void actualizaSolicitudPorCodigoSinDuplicarla() {
        SolicitudRepository repository = new SolicitudRepository();
        Solicitud guardada = repository.save(solicitudValida());
        Solicitud cambios = solicitudValida();
        cambios.setCodigo(guardada.getCodigo());
        cambios.setNombreContacto("Elena");
        cambios.setEstado(EstadoSolicitud.EN_EVALUACION);

        repository.save(cambios);

        assertEquals(1, repository.findAll().size());
        assertEquals("Elena", repository.findByCodigo("SOL-001").orElseThrow().getNombreContacto());
        assertEquals(EstadoSolicitud.EN_EVALUACION,
            repository.findByCodigo("SOL-001").orElseThrow().getEstado());
    }

    private Solicitud solicitudValida() {
        return new Solicitud(null, "Rosa", "987654321", "María", 80,
            TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now(), TurnoAtencion.MANANA,
            "Cuidados diarios", null, null);
    }
}
