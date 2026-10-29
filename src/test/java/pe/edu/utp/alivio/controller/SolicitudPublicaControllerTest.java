package pe.edu.utp.alivio.controller;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@org.springframework.transaction.annotation.Transactional
@AutoConfigureMockMvc
class SolicitudPublicaControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void descripcionDemasiadoLargaConservaTextoYMensajeDeValidacion() throws Exception {
        String descripcion = "A".repeat(501);
        MvcResult post = mvc.perform(post("/solicitudes")
            .param("nombreContacto", "María Pérez")
            .param("telefonoContacto", "987654321")
            .param("nombrePaciente", "Rosa Pérez")
            .param("tipoServicio", "ADULTO_MAYOR")
            .param("distrito", "Surco")
            .param("fechaRequerida", LocalDate.now().plusDays(1).toString())
            .param("turno", "MANANA")
            .param("descripcion", descripcion))
            .andExpect(status().is3xxRedirection())
            .andExpect(result -> assertThat(result.getFlashMap().containsKey("solicitudCreada")).isFalse())
            .andReturn();

        assertThat((java.util.Map<?, ?>) post.getFlashMap().get("valoresRechazados")).isEmpty();
        mvc.perform(get("/").flashAttrs(post.getFlashMap()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("data-error-step=\"1\"")))
            .andExpect(content().string(containsString(descripcion)))
            .andExpect(content().string(containsString("La descripción debe tener como máximo 500 caracteres.")));
    }

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
    void muestraServiciosAprobadosYEnlacesDeAcceso() throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Cuidado del adulto mayor")))
            .andExpect(content().string(containsString("Curaciones y cuidado postoperatorio")))
            .andExpect(content().string(containsString("Inyectables y tratamientos indicados")))
            .andExpect(content().string(containsString("Acompañamiento domiciliario")))
            .andExpect(content().string(containsString("Cuidado del adulto mayor</option>")))
            .andExpect(content().string(containsString("Trabaja con Alivio")))
            .andExpect(content().string(containsString("Administración")))
            .andExpect(content().string(containsString("id=\"modalSolicitud\"")));
    }

    @Test
    void conservaEdadNoConvertibleAlVolverAlPrimerPaso() throws Exception {
        MvcResult post = mvc.perform(post("/solicitudes")
            .param("nombreContacto", "María Pérez")
            .param("telefonoContacto", "987654321")
            .param("nombrePaciente", "Rosa Pérez")
            .param("edadPaciente", "abc")
            .param("tipoServicio", "ADULTO_MAYOR")
            .param("distrito", "Surco")
            .param("fechaRequerida", LocalDate.now().plusDays(1).toString())
            .param("turno", "MANANA")
            .param("descripcion", "Apoyo con movilidad"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andReturn();

        mvc.perform(get("/").flashAttrs(post.getFlashMap()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Revisa los datos ingresados e inténtalo de nuevo")))
            .andExpect(content().string(containsString("data-error-step=\"0\"")))
            .andExpect(content().string(matchesPattern(
                "(?s).*<input\\b(?=[^>]*\\bname=\"edadPaciente\")"
                    + "(?=[^>]*\\btype=\"text\")(?=[^>]*\\bvalue=\"abc\")[^>]*>.*")));
    }

    @Test
    void conservaFechaNoConvertibleAlVolverAlSegundoPaso() throws Exception {
        MvcResult post = mvc.perform(post("/solicitudes")
            .param("nombreContacto", "María Pérez")
            .param("telefonoContacto", "987654321")
            .param("nombrePaciente", "Rosa Pérez")
            .param("tipoServicio", "ADULTO_MAYOR")
            .param("distrito", "Surco")
            .param("fechaRequerida", "manana")
            .param("turno", "MANANA")
            .param("descripcion", "Apoyo con movilidad"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andReturn();

        mvc.perform(get("/").flashAttrs(post.getFlashMap()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Revisa los datos ingresados e inténtalo de nuevo")))
            .andExpect(content().string(containsString("data-error-step=\"1\"")))
            .andExpect(content().string(matchesPattern(
                "(?s).*<input\\b(?=[^>]*\\bname=\"fechaRequerida\")"
                    + "(?=[^>]*\\btype=\"text\")(?=[^>]*\\bvalue=\"manana\")[^>]*>.*")));
    }

    @Test
    void conservaServicioNoConvertibleComoOpcionVisible() throws Exception {
        MvcResult post = mvc.perform(post("/solicitudes")
            .param("nombreContacto", "María Pérez")
            .param("telefonoContacto", "987654321")
            .param("nombrePaciente", "Rosa Pérez")
            .param("tipoServicio", "OTRO_SERVICIO")
            .param("distrito", "Surco")
            .param("fechaRequerida", LocalDate.now().plusDays(1).toString())
            .param("turno", "MANANA")
            .param("descripcion", "Apoyo con movilidad"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andReturn();

        mvc.perform(get("/").flashAttrs(post.getFlashMap()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Revisa los datos ingresados e inténtalo de nuevo")))
            .andExpect(content().string(containsString("data-error-step=\"0\"")))
            .andExpect(content().string(containsString("value=\"OTRO_SERVICIO\" selected")));
    }

    @Test
    void conservaTurnoNoConvertibleEnElSegundoPasoConAyudaVisible() throws Exception {
        MvcResult post = mvc.perform(post("/solicitudes")
            .param("nombreContacto", "María Pérez")
            .param("telefonoContacto", "987654321")
            .param("nombrePaciente", "Rosa Pérez")
            .param("tipoServicio", "ADULTO_MAYOR")
            .param("distrito", "Surco")
            .param("fechaRequerida", LocalDate.now().plusDays(1).toString())
            .param("turno", "TURNO_INVENTADO")
            .param("descripcion", "Apoyo con movilidad"))
            .andExpect(status().is3xxRedirection())
            .andReturn();

        mvc.perform(get("/").flashAttrs(post.getFlashMap()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("data-error-step=\"1\"")))
            .andExpect(content().string(containsString("value=\"TURNO_INVENTADO\" selected")))
            .andExpect(content().string(containsString("Selecciona un turno válido.")));
    }

    @Test
    void fechaPasadaReabreSegundoPasoConValorYErrorDeCampo() throws Exception {
        String fecha = LocalDate.now().minusDays(1).toString();
        MvcResult post = mvc.perform(post("/solicitudes")
            .param("nombreContacto", "María Pérez")
            .param("telefonoContacto", "987654321")
            .param("nombrePaciente", "Rosa Pérez")
            .param("tipoServicio", "ADULTO_MAYOR")
            .param("distrito", "Surco")
            .param("fechaRequerida", fecha)
            .param("turno", "MANANA")
            .param("descripcion", "Apoyo con movilidad"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andReturn();

        mvc.perform(get("/").flashAttrs(post.getFlashMap()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("data-error-step=\"1\"")))
            .andExpect(content().string(containsString("id=\"errorSolicitud\"")))
            .andExpect(content().string(containsString("id=\"fechaRequerida\"")))
            .andExpect(content().string(containsString("value=\"" + fecha + "\"")))
            .andExpect(content().string(containsString("aria-invalid=\"true\"")))
            .andExpect(content().string(containsString("Selecciona hoy o una fecha posterior.")));
    }

    @Test
    void distritoEnBlancoReabreSegundoPasoConValorYErrorDeCampo() throws Exception {
        MvcResult post = mvc.perform(post("/solicitudes")
            .param("nombreContacto", "María Pérez")
            .param("telefonoContacto", "987654321")
            .param("nombrePaciente", "Rosa Pérez")
            .param("tipoServicio", "ADULTO_MAYOR")
            .param("distrito", " ")
            .param("fechaRequerida", LocalDate.now().plusDays(1).toString())
            .param("turno", "MANANA")
            .param("descripcion", "Apoyo con movilidad"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andReturn();

        mvc.perform(get("/").flashAttrs(post.getFlashMap()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("data-error-step=\"1\"")))
            .andExpect(content().string(containsString("id=\"distrito\"")))
            .andExpect(content().string(org.hamcrest.Matchers.matchesPattern(
                "(?s).*<input(?=[^>]*id=\"distrito\")(?=[^>]*value=\"\")[^>]*>.*")))
            .andExpect(content().string(containsString("Indica el distrito de atención.")));
    }

    @Test
    void edadFueraDeRangoReabrePrimerPasoConValorYErrorDeCampo() throws Exception {
        MvcResult post = mvc.perform(post("/solicitudes")
            .param("nombreContacto", "María Pérez")
            .param("telefonoContacto", "987654321")
            .param("nombrePaciente", "Rosa Pérez")
            .param("edadPaciente", "121")
            .param("tipoServicio", "ADULTO_MAYOR")
            .param("distrito", "Surco")
            .param("fechaRequerida", LocalDate.now().plusDays(1).toString())
            .param("turno", "MANANA")
            .param("descripcion", "Apoyo con movilidad"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andReturn();

        mvc.perform(get("/").flashAttrs(post.getFlashMap()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("data-error-step=\"0\"")))
            .andExpect(content().string(containsString("id=\"edadPaciente\"")))
            .andExpect(content().string(containsString("value=\"121\"")))
            .andExpect(content().string(containsString("Ingresa una edad entre 0 y 120.")));
    }
}
