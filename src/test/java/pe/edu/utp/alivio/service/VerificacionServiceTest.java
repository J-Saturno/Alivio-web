package pe.edu.utp.alivio.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.alivio.model.*;
import pe.edu.utp.alivio.web.VerificacionForm;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class VerificacionServiceTest {
    @Autowired VerificacionService service;
    @Autowired ProfesionalService profesionales;

    @Test void aprobacionExigeTodasLasComprobacionesYEvidencia() {
        String codigo = profesional();
        for (int faltante = 0; faltante < 4; faltante++) {
            VerificacionForm form = aprobada();
            if (faltante == 0) form.setIdentidadComprobada(false);
            if (faltante == 1) form.setCredencialComprobada(false);
            if (faltante == 2) form.setReferenciasComprobadas(false);
            if (faltante == 3) form.setNotaEvidencia("  ");
            assertThatThrownBy(() -> service.registrar(codigo, form, "coordinador-prueba"))
                .isInstanceOf(IllegalArgumentException.class);
        }
        assertThat(service.historial(codigo)).isEmpty();
    }

    @Test void registraHistorialInmutableConFechaYRevisorSinCambiarDisponibilidad() {
        String codigo = profesional();
        var primera = service.registrar(codigo, aprobada(), "coordinador-prueba");
        VerificacionForm segunda = new VerificacionForm();
        segunda.setResultado(ResultadoVerificacion.RECHAZADA);
        segunda.setNotaEvidencia("Referencia pendiente de confirmar (dato ficticio)");
        service.registrar(codigo, segunda, "coordinador-prueba");
        assertThat(primera.getFecha()).isNotNull();
        assertThat(primera.getRevisor()).isEqualTo("coordinador-prueba");
        assertThat(service.historial(codigo)).extracting(VerificacionProfesional::getResultado)
            .containsExactly(ResultadoVerificacion.RECHAZADA, ResultadoVerificacion.APROBADA);
        assertThat(profesionales.buscarPorCodigo(codigo).isDisponible()).isFalse();
        assertThat(service.estadosActuales()).containsEntry(codigo, "RECHAZADA");
        assertThatThrownBy(() -> profesionales.eliminar(codigo)).hasMessageContaining("historial de verificaciones");
    }

    @Test void rechazaRevisorAusenteYLimitesFueraDeRango() {
        String codigo = profesional();
        assertThatThrownBy(() -> service.registrar(codigo, aprobada(), " ")).isInstanceOf(IllegalArgumentException.class);
        VerificacionForm form = aprobada();
        form.setAniosExperiencia(81);
        assertThatThrownBy(() -> service.registrar(codigo, form, "coordinador-prueba")).isInstanceOf(IllegalArgumentException.class);
        form.setAniosExperiencia(-1);
        assertThatThrownBy(() -> service.registrar(codigo, form, "coordinador-prueba")).isInstanceOf(IllegalArgumentException.class);
        form.setAniosExperiencia(null);
        form.setNotaEvidencia("X".repeat(1001));
        assertThatThrownBy(() -> service.registrar(codigo, form, "coordinador-prueba")).isInstanceOf(IllegalArgumentException.class);
        form.setNotaEvidencia("Evidencia ficticia");
        form.setReferenciaCredencial("X".repeat(101));
        assertThatThrownBy(() -> service.registrar(codigo, form, "coordinador-prueba")).isInstanceOf(IllegalArgumentException.class);
        assertThat(service.historial(codigo)).isEmpty();
    }

    private String profesional() {
        return profesionales.registrar(new Profesional(null, "Profesional ficticia", TipoProfesional.TECNICA,
            "987654321", "Surco", TipoServicio.ADULTO_MAYOR, false)).getCodigo();
    }
    private VerificacionForm aprobada() {
        VerificacionForm form = new VerificacionForm();
        form.setResultado(ResultadoVerificacion.APROBADA);
        form.setIdentidadComprobada(true);
        form.setCredencialComprobada(true);
        form.setReferenciasComprobadas(true);
        form.setNotaEvidencia("Evidencia ficticia revisada para prueba");
        return form;
    }
}
