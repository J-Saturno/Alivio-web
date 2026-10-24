package pe.edu.utp.alivio.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.alivio.model.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProfesionalRepositoryTest {
    @Autowired ProfesionalRepository repository;
    @Autowired EntityManager entityManager;

    @Test
    void persisteYActualizaSinDuplicarProfesional() {
        Profesional guardada = repository.saveAndFlush(new Profesional(null, "Ana Torres",
            TipoProfesional.TECNICA, "987654321", "Surco", TipoServicio.ADULTO_MAYOR, true));
        String codigo = guardada.getCodigo();
        entityManager.clear();
        Profesional recargada = repository.findByCodigo(codigo).orElseThrow();
        recargada.setDisponible(false);
        repository.saveAndFlush(recargada);
        entityManager.clear();
        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.findByCodigo(codigo).orElseThrow().isDisponible()).isFalse();
        assertThat(repository.findByCodigo("PRO-invalido")).isEmpty();
    }
}
