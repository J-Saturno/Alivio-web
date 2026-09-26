package pe.edu.utp.alivio.controller;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SolicitudRedirectIntegrationTest {
    @Value("${local.server.port}") int port;

    @Test
    void primerRegistroRedirigeSinSesionEnUrlYConservaConfirmacion() throws Exception {
        HttpClient client = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
        String datos = "nombreContacto=Prueba&telefonoContacto=987654321&nombrePaciente=Paciente"
            + "&tipoServicio=ADULTO_MAYOR&distrito=Surco&fechaRequerida="
            + LocalDate.now().plusDays(1) + "&turno=MANANA&descripcion=Apoyo";
        HttpRequest registro = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/solicitudes"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(datos)).build();
        HttpResponse<String> respuesta = client.send(registro, HttpResponse.BodyHandlers.ofString());

        assertEquals(302, respuesta.statusCode());
        URI destino = URI.create(respuesta.headers().firstValue("Location").orElseThrow());
        assertEquals("/", destino.getPath());
        assertTrue(!destino.toString().contains(";jsessionid="));
        HttpRequest inicio = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/")).GET().build();
        HttpResponse<String> portada = client.send(inicio, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, portada.statusCode());
        assertTrue(portada.body().contains("Solicitud recibida"));
        assertTrue(portada.body().contains("SOL-"));
    }
}
