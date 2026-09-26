package pe.edu.utp.alivio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ErrorPageTest {
    @Autowired MockMvc mvc;

    @Test
    void paginaDeErrorNoExponeDetallesTecnicos() throws Exception {
        mvc.perform(get("/error").accept(MediaType.TEXT_HTML)
                .requestAttr("jakarta.servlet.error.status_code", 500))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("No pudimos completar la operación")))
            .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Whitelabel"))));
    }

    @Test
    void paginaNoEncontradaOfreceVolverAlInicio() throws Exception {
        mvc.perform(get("/error").accept(MediaType.TEXT_HTML)
                .requestAttr("jakarta.servlet.error.status_code", 404))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Página no encontrada")))
            .andExpect(content().string(containsString("href=\"/\"")));
    }

    @Test
    void excepcionImprevistaRespondeConPaginaGenericaYEstado500() throws Exception {
        MockMvc aislado = MockMvcBuilders.standaloneSetup(new ControladorQueFalla())
            .setControllerAdvice(new GlobalExceptionHandler()).build();
        aislado.perform(get("/fallo-inesperado"))
            .andExpect(status().isInternalServerError())
            .andExpect(view().name("error/500"))
            .andExpect(model().attribute("mensaje", "No pudimos completar la operación. Inténtalo nuevamente."));
    }

    @Controller
    static class ControladorQueFalla {
        @GetMapping("/fallo-inesperado")
        String fallar() {
            throw new IllegalStateException("detalle privado del servidor");
        }
    }
}
