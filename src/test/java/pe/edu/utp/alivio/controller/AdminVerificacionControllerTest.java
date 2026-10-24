package pe.edu.utp.alivio.controller;

import org.junit.jupiter.api.Test;
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
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class AdminVerificacionControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ProfesionalService profesionales;
    @Autowired VerificacionService verificaciones;

    @Test void muestraPendienteYEnlacePrivadoDesdeProfesionales() throws Exception {
        String codigo = profesional();
        mvc.perform(get("/admin/profesionales")).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("PENDIENTE")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("/admin/profesionales/" + codigo + "/verificaciones")));
        mvc.perform(get(ruta(codigo))).andExpect(status().isOk()).andExpect(view().name("admin/verificaciones"));
    }

    @Test void rechazaAprobacionIncompletaYConservaDatos() throws Exception {
        String codigo = profesional();
        mvc.perform(post(ruta(codigo)).principal(() -> "coordinador-prueba")
            .param("resultado", "APROBADA").param("identidadComprobada", "true")
            .param("referenciaCredencial", "Referencia ficticia").param("notaEvidencia", "Nota de prueba"))
            .andExpect(status().isOk()).andExpect(model().attributeHasErrors("verificacionForm"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Referencia ficticia")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Nota de prueba")));
        assertThat(verificaciones.historial(codigo)).isEmpty();
    }

    @Test void conservaValoresMalformadosConMensajesYNoAceptaAutorDelFormulario() throws Exception {
        String codigo = profesional();
        mvc.perform(post(ruta(codigo)).principal(() -> "coordinador-prueba")
            .param("resultado", "OTRO").param("aniosExperiencia", "muchos").param("notaEvidencia", "Prueba"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("verificacionForm", "resultado", "aniosExperiencia"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("muchos")));
        mvc.perform(post(ruta(codigo)).principal(() -> "coordinador-prueba")
            .param("resultado", "RECHAZADA").param("notaEvidencia", "Prueba ficticia")
            .param("revisor", "autor-falso").param("fecha", "2000-01-01"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(ruta(codigo)));
        assertThat(verificaciones.historial(codigo).getFirst().getRevisor()).isEqualTo("coordinador-prueba");
    }

    @Test void noRegistraSinPrincipal() throws Exception {
        String codigo = profesional();
        mvc.perform(post(ruta(codigo)).param("resultado", "RECHAZADA"))
            .andExpect(status().isOk()).andExpect(model().attributeHasErrors("verificacionForm"));
        assertThat(verificaciones.historial(codigo)).isEmpty();
    }
    private String ruta(String codigo) { return "/admin/profesionales/" + codigo + "/verificaciones"; }
    private String profesional() {
        return profesionales.registrar(new Profesional(null, "Profesional ficticia", TipoProfesional.TECNICA,
            "987654321", "Surco", TipoServicio.ADULTO_MAYOR, false)).getCodigo();
    }
}
