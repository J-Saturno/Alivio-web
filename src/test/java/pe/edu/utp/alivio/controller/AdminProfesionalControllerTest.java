package pe.edu.utp.alivio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.service.ProfesionalService;

import static org.hamcrest.Matchers.hasItem;
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
            .andExpect(model().attribute("profesionales", hasItem(profesional)))
            .andExpect(model().attributeExists("tiposProfesional", "especialidades", "nuevoProfesional"))
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
}
