package pe.edu.utp.alivio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "alivio.admin.security.enabled=true",
    "alivio.admin.username=coordinador",
    "alivio.admin.password=clave-de-prueba"
})
@AutoConfigureMockMvc
class AdminVerificacionSecurityTest {
    @Autowired MockMvc mvc;
    @Test void historialPrivadoRequiereIngresoYRegistroRequiereCsrf() throws Exception {
        mvc.perform(get("/admin/profesionales/PRO-001/verificaciones"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/ingresar"));
        mvc.perform(post("/admin/profesionales/PRO-001/verificaciones").param("resultado", "RECHAZADA"))
            .andExpect(status().isForbidden());
    }
}
