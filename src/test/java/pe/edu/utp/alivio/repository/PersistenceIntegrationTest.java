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
class PersistenceIntegrationTest {
    @Autowired EntityManager entityManager;

    @Test
    void conservaSolicitudYRelacionDespuesDeVaciarElContextoJpa() {
        Profesional profesional = new Profesional(null, "Ana Torres", TipoProfesional.TECNICA,
            "987654321", "Surco", TipoServicio.ADULTO_MAYOR, true);
        entityManager.persist(profesional);
        Solicitud solicitud = new Solicitud(null, "Rosa", "987654321", "María", 80,
            TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now(), TurnoAtencion.MANANA,
            "Apoyo diario", EstadoSolicitud.ASIGNADA, profesional);
        entityManager.persist(solicitud);
        Object id = entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(solicitud);
        entityManager.flush();
        entityManager.clear();

        Solicitud recargada = entityManager.find(Solicitud.class, id);
        assertThat(recargada.getCodigo()).startsWith("SOL-");
        assertThat(recargada.getEstado()).isEqualTo(EstadoSolicitud.ASIGNADA);
        assertThat(recargada.getProfesionalAsignado().getNombreCompleto()).isEqualTo("Ana Torres");
        assertThat(recargada.getProfesionalAsignado().getCodigo()).startsWith("PRO-");
    }
}
