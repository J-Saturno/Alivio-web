package pe.edu.utp.alivio.repository;

import org.junit.jupiter.api.Test;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfesionalRepositoryTest {
    @Test
    void asignaCodigoYPermiteBuscar() {
        ProfesionalRepository repository = new ProfesionalRepository();
        Profesional guardada = repository.save(new Profesional(null, "Ana Torres",
            TipoProfesional.TECNICA, "987654321", "Surco",
            TipoServicio.ADULTO_MAYOR, true));

        assertEquals("PRO-001", guardada.getCodigo());
        assertEquals("Ana Torres", repository.findByCodigo("PRO-001").orElseThrow().getNombreCompleto());
    }
}
