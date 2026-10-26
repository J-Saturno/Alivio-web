package pe.edu.utp.alivio.controller;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.service.ProfesionalService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "alivio.admin.security.enabled=true",
    "alivio.admin.username=coordinador",
    "alivio.admin.password=clave-de-prueba"
})
@AutoConfigureMockMvc
class AdminSecurityTest {
    @Autowired MockMvc mvc;
    @Autowired ProfesionalService profesionales;

    @Test
    @Transactional
    void edicionProfesionalIncluyeTokenYPermiteGuardarConSeguridadActiva() throws Exception {
        var profesional = profesionales.registrar(new Profesional(null,
            "Profesional de prueba", TipoProfesional.TECNICA,
            "987654321", "Ate, Molina, Santa Anita",
            TipoServicio.ADULTO_MAYOR, false));
        MvcResult ingreso = mvc.perform(get("/admin/ingresar")).andReturn();
        CsrfToken token = (CsrfToken) ingreso.getRequest().getAttribute("_csrf");
        MockHttpSession sesion = (MockHttpSession) ingreso.getRequest().getSession(false);
        mvc.perform(post("/admin/ingresar").session(sesion)
            .param(token.getParameterName(), token.getToken())
            .param("username", "coordinador").param("password", "clave-de-prueba"))
            .andExpect(status().is3xxRedirection());

        String html = mvc.perform(get("/admin/profesionales").session(sesion))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String modal = html.substring(html.indexOf("id=\"modalEditarProfesional\""));
        String formulario = modal.substring(modal.indexOf("<form"), modal.indexOf("</form>"));
        var tokenFormulario = Pattern.compile(
            "<input[^>]*name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(formulario);
        assertThat(tokenFormulario.find())
            .as("El formulario de edición debe incluir su propio token CSRF").isTrue();
        String ruta = "/admin/profesionales/" + profesional.getCodigo();
        mvc.perform(post(ruta).session(sesion)).andExpect(status().isForbidden());
        mvc.perform(post(ruta).session(sesion).param("_csrf", tokenFormulario.group(1))
            .param("nombreCompleto", "Profesional actualizada")
            .param("tipoProfesional", "TECNICA").param("telefono", "987654321")
            .param("zonaCobertura", "Ate, Molina, Santa Anita")
            .param("especialidad", "ADULTO_MAYOR").param("disponible", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/profesionales"))
            .andExpect(flash().attribute("mensaje", "Profesional actualizada"));
        assertThat(profesionales.buscarPorCodigo(profesional.getCodigo()))
            .satisfies(actual -> {
                assertThat(actual.isDisponible()).isTrue();
                assertThat(actual.getNombreCompleto()).isEqualTo("Profesional actualizada");
            });
    }

    @Test
    void solicitudPublicaGeneraEnlacePrivadoConCsrfActivo() throws Exception {
        MvcResult pagina = mvc.perform(get("/")).andExpect(status().isOk()).andReturn();
        CsrfToken token = (CsrfToken) pagina.getRequest().getAttribute("_csrf");
        MockHttpSession sesion = (MockHttpSession) pagina.getRequest().getSession(false);
        MvcResult registro = mvc.perform(post("/solicitudes").session(sesion)
            .param(token.getParameterName(), token.getToken())
            .param("nombreContacto", "Contacto confidencial")
            .param("telefonoContacto", "987654321")
            .param("nombrePaciente", "Paciente confidencial")
            .param("tipoServicio", "ADULTO_MAYOR")
            .param("distrito", "Surco")
            .param("fechaRequerida", java.time.LocalDate.now().plusDays(1).toString())
            .param("turno", "MANANA").param("descripcion", "Necesidad confidencial"))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attributeExists("solicitudCreada", "enlaceSeguimiento"))
            .andReturn();
        String enlace = (String) registro.getFlashMap().get("enlaceSeguimiento");
        mvc.perform(get(enlace))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(content().string(org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.containsString("confidencial"))));
    }

    @Test
    void visitanteDebeIniciarSesionParaVerAdministracion() throws Exception {
        mvc.perform(get("/admin/solicitudes"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/ingresar"));
        mvc.perform(get("/"))
            .andExpect(status().isOk());
        mvc.perform(get("/postular"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("_csrf")));
        mvc.perform(get("/admin/postulaciones/1/cv"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/ingresar"));
        mvc.perform(get("/admin/solicitudes/SOL-001/candidatas"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/ingresar"));
    }

    @Test
    void credencialesValidasAbrenElPanel() throws Exception {
        MvcResult pagina = mvc.perform(get("/admin/ingresar"))
            .andExpect(status().isOk()).andReturn();
        CsrfToken token = (CsrfToken) pagina.getRequest().getAttribute("_csrf");
        MockHttpSession sesion = (MockHttpSession) pagina.getRequest().getSession(false);
        mvc.perform(post("/admin/ingresar").session(sesion)
            .param(token.getParameterName(), token.getToken())
            .param("username", "coordinador")
            .param("password", "clave-de-prueba"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/solicitudes"));
        mvc.perform(get("/admin/postulaciones").session(sesion))
            .andExpect(status().isOk());
    }

    @Test
    void formularioPublicoAceptaPostulacionConProteccionCsrf() throws Exception {
        MvcResult pagina = mvc.perform(get("/postular")).andExpect(status().isOk()).andReturn();
        CsrfToken token = (CsrfToken) pagina.getRequest().getAttribute("_csrf");
        MockHttpSession sesion = (MockHttpSession) pagina.getRequest().getSession(false);
        var cv = new MockMultipartFile("cv", "cv.pdf", "application/pdf",
            "%PDF-1.4\nCurriculum".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        mvc.perform(multipart("/postulaciones").session(sesion).file(cv)
            .param(token.getParameterName(), token.getToken())
            .param("nombreCompleto", "Elena Salas")
            .param("tipoProfesional", "TECNICA")
            .param("telefono", "987654321")
            .param("zonaCobertura", "Surco")
            .param("especialidad", "ADULTO_MAYOR"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/postular"));
    }
}
