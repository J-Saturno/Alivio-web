package pe.edu.utp.alivio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.alivio.repository.ProfesionalRepository;
import pe.edu.utp.alivio.repository.PostulacionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PostulacionControllerTest {
    @Autowired MockMvc mvc;
    @Autowired PostulacionRepository postulaciones;
    @Autowired ProfesionalRepository profesionales;

    @Test
    void postulacionPdfQuedaPendienteSinSerAsignable() throws Exception {
        long antes = profesionales.count();
        mvc.perform(multipart("/postulaciones")
            .file(pdf("cv.pdf", "%PDF-1.4\nCurriculum"))
            .param("nombreCompleto", "María Valdez")
            .param("tipoProfesional", "TECNICA")
            .param("telefono", "987654321")
            .param("zonaCobertura", "Surco")
            .param("especialidad", "ADULTO_MAYOR"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/postular"));
        assertThat(postulaciones.findAll()).anySatisfy(p -> {
            assertThat(p.getNombreCompleto()).isEqualTo("María Valdez");
            assertThat(p.getEstado().name()).isEqualTo("PENDIENTE");
        });
        assertThat(profesionales.count()).isEqualTo(antes);
    }

    @Test
    void rechazaArchivoQueNoEsPdf() throws Exception {
        long antes = postulaciones.count();
        mvc.perform(multipart("/postulaciones")
            .file(pdf("cv.pdf", "un archivo falso"))
            .param("nombreCompleto", "Ana Rojas")
            .param("tipoProfesional", "TECNICA")
            .param("telefono", "987654321")
            .param("zonaCobertura", "Surco")
            .param("especialidad", "ADULTO_MAYOR"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("PDF")));
        assertThat(postulaciones.count()).isEqualTo(antes);
    }

    @Test
    void administradorAceptaUnaSolaVezYDescargaCv() throws Exception {
        mvc.perform(multipart("/postulaciones")
            .file(pdf("cv.pdf", "%PDF-1.4\nCurriculum"))
            .param("nombreCompleto", "Lucía Carranza")
            .param("tipoProfesional", "LICENCIADA")
            .param("telefono", "912345678")
            .param("zonaCobertura", "Miraflores")
            .param("especialidad", "CURACIONES_POSTOPERATORIO"));
        var postulacion = postulaciones.findAll().stream()
            .filter(p -> p.getNombreCompleto().equals("Lucía Carranza")).findFirst().orElseThrow();
        mvc.perform(get("/admin/postulaciones"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Lucía Carranza")));
        mvc.perform(get("/admin/postulaciones/{id}/cv", postulacion.getId()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));
        long antes = profesionales.count();
        mvc.perform(post("/admin/postulaciones/{id}/aceptar", postulacion.getId())
            .param("notaRevision", "Verificada por llamada"))
            .andExpect(status().is3xxRedirection());
        mvc.perform(post("/admin/postulaciones/{id}/aceptar", postulacion.getId()))
            .andExpect(status().is3xxRedirection());
        assertThat(profesionales.count()).isEqualTo(antes + 1);
        assertThat(profesionales.findAll().stream()
            .filter(p -> p.getNombreCompleto().equals("Lucía Carranza"))
            .findFirst().orElseThrow().isDisponible()).isFalse();
        mvc.perform(get("/admin/postulaciones"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("ACEPTADA")));
    }

    @Test
    void rechazoNoCreaProfesional() throws Exception {
        mvc.perform(multipart("/postulaciones")
            .file(pdf("cv.pdf", "%PDF-1.4\nCurriculum"))
            .param("nombreCompleto", "Rosa Medina")
            .param("tipoProfesional", "TECNICA")
            .param("telefono", "965432189")
            .param("zonaCobertura", "Callao")
            .param("especialidad", "ADULTO_MAYOR"));
        var postulacion = postulaciones.findAll().stream()
            .filter(p -> p.getNombreCompleto().equals("Rosa Medina")).findFirst().orElseThrow();
        long antes = profesionales.count();
        mvc.perform(post("/admin/postulaciones/{id}/rechazar", postulacion.getId())
            .param("notaRevision", "No cumplió requisitos"))
            .andExpect(status().is3xxRedirection());
        assertThat(postulaciones.findById(postulacion.getId()).orElseThrow().getEstado().name()).isEqualTo("RECHAZADA");
        assertThat(profesionales.count()).isEqualTo(antes);
    }

    private MockMultipartFile pdf(String name, String content) {
        return new MockMultipartFile("cv", name, "application/pdf", content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
