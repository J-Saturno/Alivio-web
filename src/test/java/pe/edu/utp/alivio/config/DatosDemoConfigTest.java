package pe.edu.utp.alivio.config;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.service.ProfesionalService;
import pe.edu.utp.alivio.service.SolicitudService;

import static org.assertj.core.api.Assertions.assertThat;

@org.springframework.boot.test.context.SpringBootTest
@org.springframework.transaction.annotation.Transactional
class DatosDemoConfigTest {
    @org.springframework.beans.factory.annotation.Autowired ProfesionalService profesionales;
    @org.springframework.beans.factory.annotation.Autowired SolicitudService solicitudes;
    @Test
    void cargaTresProfesionalesYDosSolicitudesSoloUnaVezEnRepositoriosVacios() throws Exception {
        var runner = new DatosDemoConfig().datosDemo(profesionales, solicitudes);

        runner.run();
        assertThat(profesionales.listarTodos()).hasSize(3);
        assertThat(profesionales.listarDisponibles()).hasSize(2);
        assertThat(solicitudes.listar("", null)).hasSize(2)
            .allSatisfy(s -> assertThat(s.getFechaRequerida()).isAfterOrEqualTo(LocalDate.now()));

        runner.run();
        assertThat(profesionales.listarTodos()).hasSize(3);
        assertThat(solicitudes.listar("", null)).hasSize(2);
    }

    @Test
    void noCargaDatosSiYaExisteUnProfesional() throws Exception {
        profesionales.registrar(new Profesional(null, "Lucía Existente", TipoProfesional.TECNICA,
            "912345678", "Surco", TipoServicio.ADULTO_MAYOR, true));

        new DatosDemoConfig().datosDemo(profesionales, solicitudes).run();

        assertThat(profesionales.listarTodos()).hasSize(1);
        assertThat(solicitudes.listar("", null)).isEmpty();
    }
}
