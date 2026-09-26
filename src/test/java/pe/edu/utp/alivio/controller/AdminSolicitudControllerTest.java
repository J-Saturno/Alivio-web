package pe.edu.utp.alivio.controller;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.model.TurnoAtencion;
import pe.edu.utp.alivio.service.ProfesionalService;
import pe.edu.utp.alivio.service.SolicitudService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSolicitudControllerTest {
    @Autowired MockMvc mvc;
    @Autowired SolicitudService solicitudes;
    @Autowired ProfesionalService profesionales;

    @Test
    void filtraTablaPeroCuentaTodasLasSolicitudes() throws Exception {
        Solicitud una = solicitud("Ana Primera");
        Solicitud otra = solicitud("Beatriz Segunda");
        long pendientes = solicitudes.listar("", null).stream()
            .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE).count();

        mvc.perform(get("/admin/solicitudes").param("q", una.getCodigo()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/solicitudes"))
            .andExpect(model().attribute("pendientes", pendientes))
            .andExpect(model().attributeExists("profesionalesDisponibles", "estados"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString(una.getNombrePaciente())))
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(otra.getNombrePaciente()))));

        mvc.perform(get("/admin/solicitudes").param("estado", "FINALIZADA"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("pendientes", pendientes))
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(una.getNombrePaciente()))));
    }

    @Test
    void evaluaYAsignaSoloUnaProfesionalDisponible() throws Exception {
        Solicitud solicitud = solicitud("Paciente Asignable");
        Profesional profesional = profesionales.registrar(new Profesional(null, "Elena Disponible",
            TipoProfesional.LICENCIADA, "987654321", "Surco", TipoServicio.ADULTO_MAYOR, true));

        mvc.perform(get("/admin/solicitudes").param("q", solicitud.getCodigo()))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString(profesional.getCodigo())))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Elena Disponible")));

        mvc.perform(post("/admin/solicitudes/{codigo}/asignar", solicitud.getCodigo())
            .param("profesionalCodigo", profesional.getCodigo()))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attributeExists("error"));
        assertThat(solicitudes.buscarPorCodigo(solicitud.getCodigo()).getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);

        mvc.perform(post("/admin/solicitudes/{codigo}/estado", solicitud.getCodigo())
            .param("estado", "EN_EVALUACION"))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attributeExists("mensaje"));
        mvc.perform(post("/admin/solicitudes/{codigo}/asignar", solicitud.getCodigo())
            .param("profesionalCodigo", profesional.getCodigo()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/solicitudes"))
            .andExpect(flash().attributeExists("mensaje"));
        assertThat(solicitudes.buscarPorCodigo(solicitud.getCodigo()).getEstado()).isEqualTo(EstadoSolicitud.ASIGNADA);
        assertThat(solicitudes.buscarPorCodigo(solicitud.getCodigo()).getProfesionalAsignado().getCodigo())
            .isEqualTo(profesional.getCodigo());
    }

    @Test
    void rechazaSaltosYValoresDeEstadoNoValidosSinErrorTecnico() throws Exception {
        Solicitud solicitud = solicitud("Paciente Protegido");
        mvc.perform(post("/admin/solicitudes/{codigo}/estado", solicitud.getCodigo())
            .param("estado", "FINALIZADA"))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attributeExists("error"));
        MvcResult respuesta = mvc.perform(post("/admin/solicitudes/{codigo}/estado", solicitud.getCodigo())
            .param("estado", "INVENTADO"))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attributeExists("error"))
            .andReturn();
        assertThat(respuesta.getFlashMap().get("error").toString()).doesNotContain("Exception", "INVENTADO");
        assertThat(solicitudes.buscarPorCodigo(solicitud.getCodigo()).getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
    }

    @Test
    void detalleIncluyeDatosPermitidosYAccionesDelFlujo() throws Exception {
        Solicitud solicitud = solicitud("Paciente Modal");
        mvc.perform(get("/admin/solicitudes").param("q", solicitud.getCodigo()))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("modalSolicitudDetalle")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("data-paciente=\"Paciente Modal\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("data-descripcion=\"Necesita acompañamiento\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("data-codigo=\"" + solicitud.getCodigo() + "\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("data-form=\"estado\"")));
    }

    private Solicitud solicitud(String paciente) {
        return solicitudes.crear(new Solicitud(null, "Contacto " + paciente, "912345678", paciente, 72,
            TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now().plusDays(1), TurnoAtencion.MANANA,
            "Necesita acompañamiento", null, null));
    }
}
