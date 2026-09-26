package pe.edu.utp.alivio.controller;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.HtmlUtils;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.service.ProfesionalService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminProfesionalControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ProfesionalService service;

    @Test
    void muestraTablaDeProfesionalesYOpcionesAprobadas() throws Exception {
        Profesional profesional = service.registrar(new Profesional(null, "Ana Torres", TipoProfesional.TECNICA,
            "987654321", "Surco", TipoServicio.ADULTO_MAYOR, true));

        mvc.perform(get("/admin/profesionales"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/profesionales"))
            .andExpect(model().attributeExists("profesionales", "tiposProfesional", "especialidades", "nuevoProfesional"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString(profesional.getCodigo())))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Ana Torres")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("modalEditarProfesional")));
    }

    @Test
    void registraProfesionalConLosDatosDelFormulario() throws Exception {
        mvc.perform(post("/admin/profesionales")
            .param("nombreCompleto", "Elena Rojas")
            .param("tipoProfesional", "LICENCIADA")
            .param("telefono", "923456789")
            .param("zonaCobertura", "Miraflores")
            .param("especialidad", "CURACIONES_POSTOPERATORIO")
            .param("disponible", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/profesionales"))
            .andExpect(flash().attributeExists("mensaje"));

        mvc.perform(get("/admin/profesionales"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Elena Rojas")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Miraflores")));
    }

    @Test
    void editaYAlternaDisponibilidad() throws Exception {
        Profesional profesional = service.registrar(new Profesional(null, "Lucía Díaz", TipoProfesional.TECNICA,
            "912345678", "Surco", TipoServicio.ADULTO_MAYOR, true));

        mvc.perform(post("/admin/profesionales/{codigo}", profesional.getCodigo())
            .param("nombreCompleto", "Lucía Salas")
            .param("tipoProfesional", "LICENCIADA")
            .param("telefono", "912345678")
            .param("zonaCobertura", "San Borja")
            .param("especialidad", "INYECTABLES_TRATAMIENTOS"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/profesionales"));
        org.junit.jupiter.api.Assertions.assertEquals("Lucía Salas", service.buscarPorCodigo(profesional.getCodigo()).getNombreCompleto());
        org.junit.jupiter.api.Assertions.assertFalse(service.buscarPorCodigo(profesional.getCodigo()).isDisponible());

        mvc.perform(post("/admin/profesionales/{codigo}/disponibilidad", profesional.getCodigo()))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attributeExists("mensaje"));
        org.junit.jupiter.api.Assertions.assertTrue(service.buscarPorCodigo(profesional.getCodigo()).isDisponible());
    }

    @Test
    void explicaErroresDeRegistroYEdicion() throws Exception {
        mvc.perform(post("/admin/profesionales")
            .param("nombreCompleto", "Ana")
            .param("tipoProfesional", "TECNICA")
            .param("telefono", "123")
            .param("zonaCobertura", "Surco")
            .param("especialidad", "ADULTO_MAYOR"))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attribute("error", "El teléfono debe tener nueve dígitos"));

        mvc.perform(post("/admin/profesionales/PRO-999")
            .param("nombreCompleto", "Ana")
            .param("tipoProfesional", "TECNICA")
            .param("telefono", "987654321")
            .param("zonaCobertura", "Surco")
            .param("especialidad", "ADULTO_MAYOR"))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attributeExists("error"));
    }

    @Test
    void reabreRegistroConTipoRechazadoYValoresIngresados() throws Exception {
        MvcResult post = mvc.perform(post("/admin/profesionales")
            .param("nombreCompleto", "María Vega")
            .param("tipoProfesional", "MEDICA")
            .param("telefono", "987654321")
            .param("zonaCobertura", "Surquillo")
            .param("especialidad", "ADULTO_MAYOR")
            .param("disponible", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/profesionales"))
            .andReturn();

        String html = html(mvc.perform(get("/admin/profesionales").flashAttrs(post.getFlashMap()))
            .andExpect(status().isOk()).andReturn());
        assertThat(html).contains("data-error-modal=\"registro\"");
        assertThat(html).contains("value=\"María Vega\"");
        assertThat(html).contains("value=\"Surquillo\"");
        assertThat(html).contains("value=\"MEDICA\" selected");
        assertThat(html).contains("id=\"nuevoTipoError\"").contains("Selecciona un tipo de profesional válido");
        assertThat(html).contains("id=\"nuevaDisponible\"").contains("checked=\"checked\"");
    }

    @Test
    void reabreEdicionConCodigoYCambiosAunqueTelefonoEsteVacio() throws Exception {
        Profesional profesional = service.registrar(new Profesional(null, "Sofía León", TipoProfesional.TECNICA,
            "945678123", "Surco", TipoServicio.ADULTO_MAYOR, true));
        MvcResult post = mvc.perform(post("/admin/profesionales/{codigo}", profesional.getCodigo())
            .param("nombreCompleto", "Sofía Actualizada")
            .param("tipoProfesional", "LICENCIADA")
            .param("telefono", "")
            .param("zonaCobertura", "Lince")
            .param("especialidad", "CURACIONES_POSTOPERATORIO")
            .param("disponible", "false"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/profesionales"))
            .andReturn();

        String html = html(mvc.perform(get("/admin/profesionales").flashAttrs(post.getFlashMap()))
            .andExpect(status().isOk()).andReturn());
        assertThat(html).contains("data-error-modal=\"edicion\"");
        assertThat(html).contains("data-codigo-edicion=\"" + profesional.getCodigo() + "\"");
        assertThat(html).contains("value=\"Sofía Actualizada\"").contains("value=\"Lince\"");
        assertThat(html).contains("id=\"editarTelefonoError\"").contains("El teléfono debe tener nueve dígitos");
        assertThat(html).contains("value=\"LICENCIADA\" selected");
        assertThat(html).contains("value=\"CURACIONES_POSTOPERATORIO\" selected");
        assertThat(service.buscarPorCodigo(profesional.getCodigo()).getNombreCompleto()).isEqualTo("Sofía León");
    }

    @Test
    void conservaDisponibilidadMalformadaEnRegistroSinError500() throws Exception {
        MvcResult post = mvc.perform(post("/admin/profesionales")
            .param("nombreCompleto", "Elisa Paz")
            .param("tipoProfesional", "TECNICA")
            .param("telefono", "956789123")
            .param("zonaCobertura", "Surco")
            .param("especialidad", "ADULTO_MAYOR")
            .param("disponible", "quizas"))
            .andExpect(status().is3xxRedirection()).andReturn();

        String html = html(mvc.perform(get("/admin/profesionales").flashAttrs(post.getFlashMap()))
            .andExpect(status().isOk()).andReturn());
        assertThat(html).contains("data-error-modal=\"registro\"");
        assertThat(html).contains("value=\"quizas\"").contains("id=\"nuevaDisponibleError\"");
        assertThat(html).contains("Indica si la profesional está disponible");
    }

    @Test
    void conservaEspecialidadMalformadaEnEdicion() throws Exception {
        Profesional profesional = service.registrar(new Profesional(null, "Clara Ruiz", TipoProfesional.TECNICA,
            "934567812", "Surco", TipoServicio.ADULTO_MAYOR, true));
        MvcResult post = mvc.perform(post("/admin/profesionales/{codigo}", profesional.getCodigo())
            .param("nombreCompleto", "Clara Nueva")
            .param("tipoProfesional", "TECNICA")
            .param("telefono", "934567812")
            .param("zonaCobertura", "Barranco")
            .param("especialidad", "OTRA"))
            .andExpect(status().is3xxRedirection()).andReturn();

        String html = html(mvc.perform(get("/admin/profesionales").flashAttrs(post.getFlashMap()))
            .andExpect(status().isOk()).andReturn());
        assertThat(html).contains("data-error-modal=\"edicion\"");
        assertThat(html).contains("data-codigo-edicion=\"" + profesional.getCodigo() + "\"");
        assertThat(html).contains("value=\"OTRA\" selected").contains("id=\"editarEspecialidadError\"");
        assertThat(html).contains("Selecciona una especialidad válida").contains("value=\"Clara Nueva\"");
    }

    private String html(MvcResult result) throws Exception {
        return HtmlUtils.htmlUnescape(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
