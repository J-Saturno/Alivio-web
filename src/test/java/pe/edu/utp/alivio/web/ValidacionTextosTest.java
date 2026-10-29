package pe.edu.utp.alivio.web;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

class ValidacionTextosTest {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest
    @ValueSource(strings = {"12345", "@@@", "\u00a0", "\u200b", "Ana123", "\u0301", "\u034f", "\u3164"})
    void rechazaNombresSinContenidoHumano(String nombre) {
        var form = new ProfesionalForm();
        form.setNombreCompleto(nombre);
        assertThat(VALIDATOR.validateProperty(form, "nombreCompleto")).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"María José", "Óscar Ñáñez", "Anne-Marie O’Connor", "D'Angelo", "Ana M.", "李 明", "J. R. Tolkien"})
    void conservaNombresLegitimos(String nombre) {
        var form = new ProfesionalForm();
        form.setNombreCompleto(nombre);
        assertThat(VALIDATOR.validateProperty(form, "nombreCompleto")).isEmpty();
    }

    @Test void normalizaEspaciosDeNombresYCelularesEnLosTresFormularios() {
        var solicitud = new SolicitudForm();
        solicitud.setNombreContacto(" \u00a0Ana   Torres\u200b ");
        solicitud.setNombrePaciente("  José   Pérez  ");
        solicitud.setTelefonoContacto(" 912 345 678 ");
        var profesional = new ProfesionalForm();
        profesional.setNombreCompleto("  Ana   Torres  ");
        profesional.setTelefono("912\u00a0345\u00a0678");
        var postulacion = new PostulacionForm();
        postulacion.setNombreCompleto("  Ana   Torres  ");
        postulacion.setTelefono(" 912345678 ");
        assertThat(solicitud.toSolicitud().getNombreContacto()).isEqualTo("Ana Torres");
        assertThat(solicitud.getNombrePaciente()).isEqualTo("José Pérez");
        assertThat(profesional.toProfesional().getNombreCompleto()).isEqualTo("Ana Torres");
        assertThat(postulacion.toPostulacion().getNombreCompleto()).isEqualTo("Ana Torres");
        assertThat(solicitud.getTelefonoContacto()).isEqualTo("912345678");
        assertThat(profesional.getTelefono()).isEqualTo("912345678");
        assertThat(postulacion.getTelefono()).isEqualTo("912345678");
    }

    @ParameterizedTest
    @ValueSource(strings = {",,,", "Ate,,Surco", "123", "Ate,", "@Surco", "Ate, 123"})
    void rechazaCoberturaSinDistritosValidos(String zona) {
        var form = new ProfesionalForm();
        form.setZonaCobertura(zona);
        assertThat(VALIDATOR.validateProperty(form, "zonaCobertura")).isNotEmpty();
    }

    @Test void normalizaCoberturaYConservaDistritosConNumeros() {
        var form = new ProfesionalForm();
        form.setZonaCobertura(" Ate - Molina ; Santa   Anita ");
        assertThat(form.getZonaCobertura()).isEqualTo("Ate, Molina, Santa Anita");
        assertThat(VALIDATOR.validateProperty(form, "zonaCobertura")).isEmpty();
        var solicitud = new SolicitudForm();
        solicitud.setDistrito(" 26 de Octubre ");
        assertThat(solicitud.getDistrito()).isEqualTo("26 de Octubre");
        assertThat(VALIDATOR.validateProperty(solicitud, "distrito")).isEmpty();
        solicitud.setDistrito("123");
        assertThat(VALIDATOR.validateProperty(solicitud, "distrito")).isNotEmpty();
    }

    @Test void descripcionConservaParrafosYRechazaVacioInvisible() {
        var form = new SolicitudForm();
        form.setDescripcion("  Necesita   apoyo.\r\n\r\n Tiene control mañana.  ");
        assertThat(form.getDescripcion()).isEqualTo("Necesita apoyo.\n\nTiene control mañana.");
        form.setDescripcion("\u00a0\u200b");
        assertThat(VALIDATOR.validateProperty(form, "descripcion")).isNotEmpty();
    }

    @Test void postulacionTambienValidaNombreYDistritos() {
        var form = new PostulacionForm();
        form.setNombreCompleto("123");
        form.setZonaCobertura(",,,");
        assertThat(VALIDATOR.validateProperty(form, "nombreCompleto")).isNotEmpty();
        assertThat(VALIDATOR.validateProperty(form, "zonaCobertura")).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"\u034f", "\u0301", "\ufe0f", "\u3164"})
    void descripcionNoAceptaMarcasSueltasNiRellenosInvisibles(String invisible) {
        var form = new SolicitudForm();
        form.setDescripcion(invisible);
        assertThat(VALIDATOR.validateProperty(form, "descripcion")).isNotEmpty();
    }
}
