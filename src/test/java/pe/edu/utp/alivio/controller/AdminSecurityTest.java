package pe.edu.utp.alivio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.csrf.CsrfToken;

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
