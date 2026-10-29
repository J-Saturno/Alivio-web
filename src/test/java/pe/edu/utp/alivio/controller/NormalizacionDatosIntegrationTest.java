package pe.edu.utp.alivio.controller;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.alivio.model.*;
import pe.edu.utp.alivio.service.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NormalizacionDatosIntegrationTest {
    @Autowired ProfesionalService profesionales;
    @Autowired SolicitudService solicitudes;
    @Autowired MockMvc mvc;
    @Autowired PostulacionService postulaciones;
    @Autowired VerificacionService verificaciones;

    @Test void serviciosNormalizanYValidanAunqueNoSeUseUnFormulario() {
        var creada = profesionales.registrar(profesional(" \u00a0Ana   Torres  ", " 912 345 678 ", " Ate - Molina - Santa Anita "));
        assertThat(creada.getNombreCompleto()).isEqualTo("Ana Torres");
        assertThat(creada.getTelefono()).isEqualTo("912345678");
        assertThat(creada.getZonaCobertura()).isEqualTo("Ate, Molina, Santa Anita");
        assertThatThrownBy(() -> profesionales.registrar(profesional("123", "912345678", "Molina")))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> profesionales.registrar(profesional("Ana", "912345678", ",,,")))
            .isInstanceOf(IllegalArgumentException.class);
        var solicitud = solicitudes.crear(new Solicitud(null, "  José   Pérez ", " 912345678 ",
            "  Ana   Torres ", null, TipoServicio.ADULTO_MAYOR, " Molina ",
            LocalDate.now().plusDays(1), TurnoAtencion.MANANA, "Apoyo.\n\nControl mañana.",
            EstadoSolicitud.PENDIENTE, null));
        assertThat(solicitud.getNombreContacto()).isEqualTo("José Pérez");
        solicitudes.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_EVALUACION);
        assertThat(solicitudes.candidatasCompatibles(solicitud.getCodigo()))
            .extracting(Profesional::getCodigo).contains(creada.getCodigo());
    }

    @Test void advierteCoincidenciasSinImpedirPersonasConElMismoNombreOTelefono() throws Exception {
        var primera = profesionales.registrar(profesional("Ana Torres", "912345678", "Molina"));
        var segunda = profesionales.registrar(profesional("  ANA   TORRES ", "923456789", "Molina"));
        var tercera = profesionales.registrar(profesional("Elena Rojas", "912345678", "Molina"));
        assertThat(profesionales.listarTodos()).hasSize(3);
        mvc.perform(get("/admin/profesionales"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Posibles registros duplicados")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString(primera.getCodigo() + " y " + segunda.getCodigo())))
            .andExpect(content().string(org.hamcrest.Matchers.containsString(primera.getCodigo() + " y " + tercera.getCodigo())));
    }

    @Test void formularioDevuelveErroresEspecificosSinGuardar() throws Exception {
        mvc.perform(post("/admin/profesionales").param("nombreCompleto", "12345")
            .param("tipoProfesional", "TECNICA").param("telefono", "912345678")
            .param("zonaCobertura", ",,,").param("especialidad", "ADULTO_MAYOR"))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attribute("modalError", "registro"))
            .andExpect(result -> {
                var errores = (java.util.Map<?, ?>) result.getFlashMap().get("erroresCampo");
                assertThat(errores.containsKey("nombreCompleto")).isTrue();
                assertThat(errores.containsKey("zonaCobertura")).isTrue();
            });
        assertThat(profesionales.listarTodos()).isEmpty();
    }

    @Test void postulacionNormalizaDatosYNoPermiteCamposInvalidos() {
        var postulacion = new PostulacionProfesional();
        postulacion.setNombreCompleto("  María   López ");
        postulacion.setTelefono(" 912 345 678 ");
        postulacion.setZonaCobertura(" Ate - Molina ");
        postulacion.setTipoProfesional(TipoProfesional.TECNICA);
        postulacion.setEspecialidad(TipoServicio.ADULTO_MAYOR);
        var pdf = new org.springframework.mock.web.MockMultipartFile("cv", "cv.pdf", "application/pdf",
            "%PDF-1.4\nCurriculum".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        var guardada = postulaciones.registrar(postulacion, pdf);
        assertThat(guardada.getNombreCompleto()).isEqualTo("María López");
        assertThat(guardada.getTelefono()).isEqualTo("912345678");
        assertThat(guardada.getZonaCobertura()).isEqualTo("Ate, Molina");
        var invalida = new PostulacionProfesional();
        invalida.setNombreCompleto("123");
        assertThatThrownBy(() -> postulaciones.registrar(invalida, pdf)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\u00a0\u200b", "\u034f", "\u0301", "\ufe0f", "\u3164"})
    void evidenciaInvisibleNoPermiteAprobarUnaVerificacion(String invisible) {
        var profesional = profesionales.registrar(profesional("Ana Torres", "912345678", "Molina"));
        var revision = new pe.edu.utp.alivio.web.VerificacionForm();
        revision.setIdentidadComprobada(true);
        revision.setCredencialComprobada(true);
        revision.setReferenciasComprobadas(true);
        revision.setResultado(ResultadoVerificacion.APROBADA);
        revision.setNotaEvidencia(invisible);
        assertThatThrownBy(() -> verificaciones.registrar(profesional.getCodigo(), revision, "coordinador"))
            .isInstanceOf(IllegalArgumentException.class);
        revision.setNotaEvidencia("  Identidad   revisada.\n\n Referencias confirmadas. ");
        var guardada = verificaciones.registrar(profesional.getCodigo(), revision, "coordinador");
        assertThat(guardada.getNotaEvidencia()).isEqualTo("Identidad revisada.\n\nReferencias confirmadas.");
    }

    private Profesional profesional(String nombre, String telefono, String zona) {
        return new Profesional(null, nombre, TipoProfesional.TECNICA, telefono, zona, TipoServicio.ADULTO_MAYOR, true);
    }
}
