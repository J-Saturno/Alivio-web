package pe.edu.utp.alivio.repository;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.alivio.model.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SolicitudRepositoryTest {
    @Autowired SolicitudRepository repository;
    @Autowired EntityManager entityManager;

    @Test
    void actualizaSolicitudPersistidaSinDuplicarla() {
        Solicitud guardada = repository.saveAndFlush(solicitud());
        String codigo = guardada.getCodigo();
        entityManager.clear();
        Solicitud cambios = repository.findByCodigo(codigo).orElseThrow();
        cambios.setNombreContacto("Elena");
        cambios.setEstado(EstadoSolicitud.EN_EVALUACION);
        repository.saveAndFlush(cambios);
        entityManager.clear();
        Solicitud recargada = repository.findByCodigo(codigo).orElseThrow();
        assertThat(repository.count()).isEqualTo(1);
        assertThat(recargada.getNombreContacto()).isEqualTo("Elena");
        assertThat(recargada.getEstado()).isEqualTo(EstadoSolicitud.EN_EVALUACION);
    }

    @Test
    void noReutilizaCodigosDespuesDeEliminar() {
        Solicitud primera = repository.saveAndFlush(solicitud());
        String codigo = primera.getCodigo();
        repository.delete(primera);
        repository.flush();
        Solicitud siguiente = repository.saveAndFlush(solicitud());
        assertThat(siguiente.getCodigo()).isNotEqualTo(codigo);
        assertThat(repository.findByCodigo(codigo)).isEmpty();
    }

    private Solicitud solicitud() {
        return new Solicitud(null, "Rosa", "987654321", "María", 80,
            TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now(), TurnoAtencion.MANANA,
            "Cuidados diarios", EstadoSolicitud.PENDIENTE, null);
    }
}
