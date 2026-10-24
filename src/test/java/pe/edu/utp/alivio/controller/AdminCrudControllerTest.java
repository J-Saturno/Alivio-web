package pe.edu.utp.alivio.controller;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.alivio.model.*;
import pe.edu.utp.alivio.service.ProfesionalService;
import pe.edu.utp.alivio.service.SolicitudService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminCrudControllerTest {
    @Autowired MockMvc mvc;
    @Autowired SolicitudService solicitudes;
    @Autowired ProfesionalService profesionales;

    @Test
    void editaSolicitudConservandoCodigoYEstado() throws Exception {
        Solicitud original = solicitud();
        mvc.perform(get("/admin/solicitudes/{codigo}/editar", original.getCodigo()))
            .andExpect(status().isOk()).andExpect(view().name("admin/solicitud-formulario"))
            .andExpect(model().attributeExists("solicitudForm", "tiposServicio", "turnos"))
            .andExpect(content().string(containsString("Paciente original")));
        mvc.perform(edicion(original.getCodigo()).param("nombrePaciente", "Paciente actualizado"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/solicitudes"))
            .andExpect(flash().attributeExists("mensaje"));
        Solicitud actualizada = solicitudes.buscarPorCodigo(original.getCodigo());
        assertThat(actualizada.getNombrePaciente()).isEqualTo("Paciente actualizado");
        assertThat(actualizada.getCodigo()).isEqualTo(original.getCodigo());
        assertThat(actualizada.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
    }

    @Test
    void conservaValoresInvalidosYMuestraErroresPorCampo() throws Exception {
        Solicitud original = solicitud();
        mvc.perform(edicion(original.getCodigo(), "no-es-edad", "INVENTADO").param("nombrePaciente", ""))
            .andExpect(status().isOk()).andExpect(view().name("admin/solicitud-formulario"))
            .andExpect(model().attributeHasFieldErrors("solicitudForm", "nombrePaciente", "edadPaciente", "tipoServicio"))
            .andExpect(content().string(containsString("no-es-edad")))
            .andExpect(content().string(containsString("INVENTADO")));
        assertThat(solicitudes.buscarPorCodigo(original.getCodigo()).getNombrePaciente()).isEqualTo("Paciente original");
    }

    @Test
    void noEditaSolicitudAsignadaNiMediantePostDirecto() throws Exception {
        Solicitud original = asignada();
        mvc.perform(get("/admin/solicitudes/{codigo}/editar", original.getCodigo()))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("error"));
        mvc.perform(edicion(original.getCodigo()).param("nombrePaciente", "Cambio prohibido"))
            .andExpect(status().isOk()).andExpect(model().attributeExists("error"));
        assertThat(solicitudes.buscarPorCodigo(original.getCodigo()).getNombrePaciente()).isEqualTo("Paciente original");
    }

    @Test
    void confirmaAntesDeEliminarSolicitud() throws Exception {
        Solicitud original = solicitud();
        mvc.perform(get("/admin/solicitudes/{codigo}/eliminar", original.getCodigo()))
            .andExpect(status().isOk()).andExpect(view().name("admin/confirmar-eliminacion"))
            .andExpect(content().string(containsString("method=\"post\"")));
        assertThat(solicitudes.buscarPorCodigo(original.getCodigo())).isNotNull();
        mvc.perform(post("/admin/solicitudes/{codigo}/eliminar", original.getCodigo()))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("mensaje"));
        assertThatThrownBy(() -> solicitudes.buscarPorCodigo(original.getCodigo())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void soloOfreceAccionesPermitidasSegunEstado() throws Exception {
        Solicitud original = solicitud();
        String base = "/admin/solicitudes/" + original.getCodigo();
        mvc.perform(get("/admin/solicitudes").param("q", original.getCodigo()))
            .andExpect(content().string(containsString(base + "/editar")))
            .andExpect(content().string(containsString(base + "/eliminar")));
        solicitudes.actualizarEstado(original.getCodigo(), EstadoSolicitud.EN_EVALUACION);
        mvc.perform(get("/admin/solicitudes").param("q", original.getCodigo()))
            .andExpect(content().string(containsString(base + "/editar")))
            .andExpect(content().string(org.hamcrest.Matchers.not(containsString(base + "/eliminar"))));
        solicitudes.actualizarEstado(original.getCodigo(), EstadoSolicitud.CANCELADA);
        mvc.perform(get("/admin/solicitudes").param("q", original.getCodigo()))
            .andExpect(content().string(org.hamcrest.Matchers.not(containsString(base + "/editar"))))
            .andExpect(content().string(containsString(base + "/eliminar")));
        mvc.perform(post(base + "/eliminar")).andExpect(flash().attributeExists("mensaje"));
    }

    @Test
    void confirmaConTextoEscapadoYCodigosInexistentesSonAmigables() throws Exception {
        Profesional profesional = profesionales.registrar(new Profesional(null, "<script>alert(1)</script>",
            TipoProfesional.LICENCIADA, "987654321", "Surco", TipoServicio.ADULTO_MAYOR, true));
        mvc.perform(get("/admin/profesionales/{codigo}/eliminar", profesional.getCodigo()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("&lt;script&gt;")))
            .andExpect(content().string(org.hamcrest.Matchers.not(containsString("<script>alert(1)</script>"))));
        mvc.perform(get("/admin/solicitudes/SOL-999999/editar"))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("error"));
        mvc.perform(get("/admin/profesionales/PRO-999999/eliminar"))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("error"));
    }

    @Test
    void confirmaYEliminaProfesionalSinSolicitudes() throws Exception {
        Profesional profesional = profesional();
        mvc.perform(get("/admin/profesionales/{codigo}/eliminar", profesional.getCodigo()))
            .andExpect(status().isOk()).andExpect(view().name("admin/confirmar-eliminacion"));
        assertThat(profesionales.buscarPorCodigo(profesional.getCodigo())).isNotNull();
        mvc.perform(post("/admin/profesionales/{codigo}/eliminar", profesional.getCodigo()))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("mensaje"));
        assertThatThrownBy(() -> profesionales.buscarPorCodigo(profesional.getCodigo())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void protegeProfesionalRelacionadaYSolicitudAsignada() throws Exception {
        Solicitud original = asignada();
        String codigoProfesional = original.getProfesionalAsignado().getCodigo();
        mvc.perform(get("/admin/profesionales/{codigo}/eliminar", codigoProfesional)).andExpect(status().isOk());
        mvc.perform(post("/admin/profesionales/{codigo}/eliminar", codigoProfesional))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("error"));
        mvc.perform(post("/admin/solicitudes/{codigo}/eliminar", original.getCodigo()))
            .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("error"));
        assertThat(profesionales.buscarPorCodigo(codigoProfesional)).isNotNull();
        assertThat(solicitudes.buscarPorCodigo(original.getCodigo()).getEstado()).isEqualTo(EstadoSolicitud.ASIGNADA);
    }

    private MockHttpServletRequestBuilder edicion(String codigo) {
        return edicion(codigo, "72", "ADULTO_MAYOR");
    }

    private MockHttpServletRequestBuilder edicion(String codigo, String edad, String tipo) {
        return post("/admin/solicitudes/{codigo}/editar", codigo)
            .param("nombreContacto", "Contacto actualizado").param("telefonoContacto", "912345678")
            .param("edadPaciente", edad).param("tipoServicio", tipo)
            .param("distrito", "Surco").param("fechaRequerida", LocalDate.now().plusDays(1).toString())
            .param("turno", "MANANA").param("descripcion", "Necesita acompañamiento");
    }

    private Solicitud solicitud() {
        return solicitudes.crear(new Solicitud(null, "Contacto original", "912345678", "Paciente original", 72,
            TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now().plusDays(1), TurnoAtencion.MANANA,
            "Necesita acompañamiento", null, null));
    }

    private Profesional profesional() {
        return profesionales.registrar(new Profesional(null, "Elena Disponible", TipoProfesional.LICENCIADA,
            "987654321", "Surco", TipoServicio.ADULTO_MAYOR, true));
    }

    private Solicitud asignada() {
        Solicitud original = solicitud();
        solicitudes.actualizarEstado(original.getCodigo(), EstadoSolicitud.EN_EVALUACION);
        solicitudes.asignar(original.getCodigo(), profesional().getCodigo());
        return solicitudes.buscarPorCodigo(original.getCodigo());
    }
}
