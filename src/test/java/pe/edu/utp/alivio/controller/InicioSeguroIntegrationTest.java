package pe.edu.utp.alivio.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "alivio.admin.security.enabled=true",
    "alivio.admin.username=verificador",
    "alivio.admin.password=Temporal-Solo-Prueba-2026"
})
class InicioSeguroIntegrationTest {
    @Value("${local.server.port}") int port;

    @Test
    void portadaConSeguridadActivaRespondeCompleta() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/"))
            .GET().build();
        HttpResponse<String> response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("</html>"));
        assertTrue(response.body().contains("name=\"_csrf\""));
    }
}
