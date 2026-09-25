package pe.edu.utp.alivio.controller;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SolicitudPublicaControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void registraSolicitudYRedirigeConConfirmacion() throws Exception {
        mvc.perform(post("/solicitudes")
            .param("nombreContacto", "María Pérez")
            .param("telefonoContacto", "987654321")
            .param("nombrePaciente", "Rosa Pérez")
            .param("tipoServicio", "ADULTO_MAYOR")
            .param("distrito", "Surco")
            .param("fechaRequerida", LocalDate.now().plusDays(1).toString())
            .param("turno", "MANANA")
            .param("descripcion", "Apoyo con movilidad y medicación"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attributeExists("solicitudCreada"));
    }

    @Test
    void conservaDatosYExplicaErrorDeSolicitudInvalida() throws Exception {
        mvc.perform(post("/solicitudes")
            .param("nombreContacto", "María Pérez")
            .param("telefonoContacto", "123")
            .param("nombrePaciente", "Rosa Pérez")
            .param("tipoServicio", "ADULTO_MAYOR")
            .param("distrito", "Surco")
            .param("fechaRequerida", LocalDate.now().plusDays(1).toString())
            .param("turno", "MANANA")
            .param("descripcion", "Apoyo con movilidad"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attributeExists("errorSolicitud", "formAnterior"));
    }

    @Test
    void muestraServiciosAprobadosYAccesoDemo() throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Cuidado del adulto mayor")))
            .andExpect(content().string(containsString("Curaciones y cuidado postoperatorio")))
            .andExpect(content().string(containsString("Inyectables y tratamientos indicados")))
            .andExpect(content().string(containsString("Acompañamiento domiciliario")))
            .andExpect(content().string(containsString("Acceso administrativo - Demo")))
            .andExpect(content().string(containsString("id=\"modalSolicitud\"")));
    }
}
