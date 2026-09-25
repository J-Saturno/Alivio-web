package pe.edu.utp.alivio.service;

import java.util.List;
import org.junit.jupiter.api.Test;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.repository.ProfesionalRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfesionalServiceTest {
    @Test
    void listaSoloProfesionalesDisponibles() {
        ProfesionalService service = new ProfesionalService(new ProfesionalRepository());
        service.registrar(profesional("Ana", true));
        service.registrar(profesional("Rosa", false));

        assertEquals(List.of("Ana"), service.listarDisponibles().stream()
            .map(Profesional::getNombreCompleto).toList());
        assertEquals(2, service.listarTodos().size());
    }

    @Test
    void rechazaTelefonoInvalido() {
        ProfesionalService service = new ProfesionalService(new ProfesionalRepository());
        Profesional profesional = profesional("Ana", true);
        profesional.setTelefono("123");

        assertThrows(IllegalArgumentException.class, () -> service.registrar(profesional));
        assertTrue(service.listarTodos().isEmpty());
    }

    @Test
    void actualizaPorCodigoSinCrearOtroRegistro() {
        ProfesionalService service = new ProfesionalService(new ProfesionalRepository());
        String codigo = service.registrar(profesional("Ana", true)).getCodigo();
        Profesional cambios = profesional("Ana Torres", false);

        Profesional actualizada = service.actualizar(codigo, cambios);

        assertEquals(codigo, actualizada.getCodigo());
        assertEquals("Ana Torres", service.buscarPorCodigo(codigo).getNombreCompleto());
        assertEquals(1, service.listarTodos().size());
    }

    @Test
    void alternaDisponibilidad() {
        ProfesionalService service = new ProfesionalService(new ProfesionalRepository());
        String codigo = service.registrar(profesional("Ana", true)).getCodigo();

        assertFalse(service.cambiarDisponibilidad(codigo).isDisponible());
        assertTrue(service.cambiarDisponibilidad(codigo).isDisponible());
    }

    @Test
    void buscarCodigoInexistenteDaError() {
        ProfesionalService service = new ProfesionalService(new ProfesionalRepository());

        assertThrows(IllegalArgumentException.class, () -> service.buscarPorCodigo("PRO-999"));
    }

    @Test
    void rechazaCamposObligatoriosAusentes() {
        ProfesionalService service = new ProfesionalService(new ProfesionalRepository());
        Profesional sinNombre = profesional(" ", true);
        Profesional sinZona = profesional("Ana", true);
        sinZona.setZonaCobertura(" ");
        Profesional sinTipo = profesional("Ana", true);
        sinTipo.setTipoProfesional(null);
        Profesional sinEspecialidad = profesional("Ana", true);
        sinEspecialidad.setEspecialidad(null);

        assertThrows(IllegalArgumentException.class, () -> service.registrar(sinNombre));
        assertThrows(IllegalArgumentException.class, () -> service.registrar(sinZona));
        assertThrows(IllegalArgumentException.class, () -> service.registrar(sinTipo));
        assertThrows(IllegalArgumentException.class, () -> service.registrar(sinEspecialidad));
        assertTrue(service.listarTodos().isEmpty());
    }

    private Profesional profesional(String nombre, boolean disponible) {
        return new Profesional(null, nombre, TipoProfesional.TECNICA, "987654321", "Surco",
            TipoServicio.ADULTO_MAYOR, disponible);
    }
}
