package pe.edu.utp.alivio.controller;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.alivio.model.*;
import pe.edu.utp.alivio.service.SeguimientoService;
import pe.edu.utp.alivio.service.SolicitudService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "alivio.admin.security.enabled=true",
    "alivio.admin.username=coordinador",
    "alivio.admin.password=clave-de-prueba"
})
@AutoConfigureMockMvc
@Transactional
class SeguimientoControllerTest {
    @Autowired MockMvc mvc;
    @Autowired SolicitudService solicitudes;
    @Autowired SeguimientoService seguimiento;

    @Test
    void enlacePublicoMuestraSoloEstadoEHistorialSinDatosPersonales() throws Exception {
        Solicitud solicitud = solicitud();
        String token = seguimiento.emitir(solicitud.getCodigo());
        solicitudes.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_EVALUACION);

        MvcResult respuesta = mvc.perform(get("/seguimiento/{token}", token))
            .andExpect(status().isOk())
            .andExpect(view().name("seguimiento"))
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(header().string("Referrer-Policy", "no-referrer"))
            .andExpect(header().string("X-Robots-Tag", "noindex, nofollow"))
            .andReturn();
        assertThat(respuesta.getResponse().getContentAsString())
            .contains(solicitud.getCodigo(), "En evaluaci", "Historial", "desde", "noindex, nofollow", "no-referrer")
            .doesNotContain("Paciente Confidencial", "Contacto Reservado", "912345678", "Diagnostico privado",
                "https://", "http://", token, "nombrePaciente", "telefonoContacto");
    }

    @Test
    void enlacesInvalidosRevocadosORenovadosRespondenIgualSinCache() throws Exception {
        Solicitud solicitud = solicitud();
        String anterior = seguimiento.emitir(solicitud.getCodigo());
        String vigente = seguimiento.emitir(solicitud.getCodigo());
        String desconocido = "A".repeat(43);
        String generico = paginaInvalida(desconocido);
        assertThat(paginaInvalida(anterior)).isEqualTo(generico);
        seguimiento.revocar(solicitud.getCodigo());
        assertThat(paginaInvalida(vigente)).isEqualTo(generico);
        assertThat(paginaInvalida("SOL-001")).isEqualTo(generico);
    }

    @Test
    void administracionEmiteSoloPorPostYNoRecuperaEnlaceEnListado() throws Exception {
        Solicitud solicitud = solicitud();
        MockHttpSession sesion = ingresar();
        CsrfToken csrf = csrf(sesion);
        MvcResult emitido = mvc.perform(post("/admin/solicitudes/{codigo}/seguimiento/emitir", solicitud.getCodigo())
            .session(sesion).param(csrf.getParameterName(), csrf.getToken()))
            .andExpect(redirectedUrl("/admin/solicitudes"))
            .andExpect(flash().attributeExists("enlaceSeguimiento")).andReturn();
        String enlace = (String) emitido.getFlashMap().get("enlaceSeguimiento");
        assertThat(enlace).startsWith("/seguimiento/");
        assertThat(seguimiento.consultar(enlace.substring("/seguimiento/".length()))).isPresent();
        MvcResult primeraVista = mvc.perform(get("/admin/solicitudes").session(sesion))
            .andExpect(status().isOk()).andReturn();
        assertThat(primeraVista.getResponse().getContentAsString()).contains(enlace, "30 días");
        MvcResult listado = mvc.perform(get("/admin/solicitudes").session(sesion))
            .andExpect(status().isOk()).andReturn();
        assertThat(listado.getResponse().getContentAsString()).doesNotContain(enlace);
        csrf = csrf(sesion);
        mvc.perform(post("/admin/solicitudes/{codigo}/seguimiento/revocar", solicitud.getCodigo())
            .session(sesion).param(csrf.getParameterName(), csrf.getToken()))
            .andExpect(redirectedUrl("/admin/solicitudes"))
            .andExpect(flash().attributeExists("mensaje"));
        assertThat(seguimiento.consultar(enlace.substring("/seguimiento/".length()))).isEmpty();
    }

    @Test
    void emisionYRevocacionExigenAutenticacionYCsrf() throws Exception {
        String codigo = solicitud().getCodigo();
        MvcResult login = mvc.perform(get("/admin/ingresar")).andReturn();
        CsrfToken csrf = (CsrfToken) login.getRequest().getAttribute("_csrf");
        MockHttpSession anonima = (MockHttpSession) login.getRequest().getSession(false);
        for (String accion : new String[] {"emitir", "revocar"}) {
            mvc.perform(post("/admin/solicitudes/{codigo}/seguimiento/{accion}", codigo, accion)
                .session(anonima).param(csrf.getParameterName(), csrf.getToken()))
                .andExpect(redirectedUrl("/admin/ingresar"));
        }
        MockHttpSession sesion = ingresar();
        for (String accion : new String[] {"emitir", "revocar"}) {
            mvc.perform(post("/admin/solicitudes/{codigo}/seguimiento/{accion}", codigo, accion).session(sesion))
                .andExpect(status().isForbidden());
        }
    }

    @Test
    void solicitudInexistenteMuestraErrorAmigable() throws Exception {
        MockHttpSession sesion = ingresar();
        for (String accion : new String[] {"emitir", "revocar"}) {
            CsrfToken csrf = csrf(sesion);
            mvc.perform(post("/admin/solicitudes/SOL-999999/seguimiento/{accion}", accion)
                .session(sesion).param(csrf.getParameterName(), csrf.getToken()))
                .andExpect(redirectedUrl("/admin/solicitudes"))
                .andExpect(flash().attribute("error", "Solicitud no encontrada"));
        }
    }

    private String paginaInvalida(String token) throws Exception {
        return mvc.perform(get("/seguimiento/{token}", token)).andExpect(status().isNotFound())
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(header().string("Referrer-Policy", "no-referrer"))
            .andExpect(header().string("X-Robots-Tag", "noindex, nofollow"))
            .andReturn().getResponse().getContentAsString();
    }

    private MockHttpSession ingresar() throws Exception {
        MvcResult pagina = mvc.perform(get("/admin/ingresar")).andReturn();
        CsrfToken csrf = (CsrfToken) pagina.getRequest().getAttribute("_csrf");
        MockHttpSession sesion = (MockHttpSession) pagina.getRequest().getSession(false);
        mvc.perform(post("/admin/ingresar").session(sesion)
            .param(csrf.getParameterName(), csrf.getToken())
            .param("username", "coordinador").param("password", "clave-de-prueba"))
            .andExpect(redirectedUrl("/admin/solicitudes"));
        return sesion;
    }

    private CsrfToken csrf(MockHttpSession sesion) throws Exception {
        return (CsrfToken) mvc.perform(get("/admin/solicitudes").session(sesion)).andReturn()
            .getRequest().getAttribute("_csrf");
    }

    private Solicitud solicitud() {
        return solicitudes.crear(new Solicitud(null, "Contacto Reservado", "912345678", "Paciente Confidencial", 72,
            TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now().plusDays(1), TurnoAtencion.MANANA,
            "Diagnostico privado", null, null));
    }
}
