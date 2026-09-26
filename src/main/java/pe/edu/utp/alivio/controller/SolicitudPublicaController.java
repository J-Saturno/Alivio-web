package pe.edu.utp.alivio.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.service.SolicitudService;
import pe.edu.utp.alivio.service.SolicitudValidationException;
import pe.edu.utp.alivio.web.SolicitudForm;

@Controller
public class SolicitudPublicaController {
    private final SolicitudService service;

    public SolicitudPublicaController(SolicitudService service) {
        this.service = service;
    }

    @PostMapping("/solicitudes")
    public String registrar(@ModelAttribute SolicitudForm form, BindingResult binding,
                            RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            Map<String, String> valoresRechazados = new HashMap<>();
            Map<String, String> errores = new LinkedHashMap<>();
            for (FieldError error : binding.getFieldErrors()) {
                if (error.getRejectedValue() != null) {
                    valoresRechazados.put(error.getField(), error.getRejectedValue().toString());
                }
                errores.put(error.getField(), mensajeCampo(error.getField()));
            }
            mostrarErrores(redirect, form, errores, valoresRechazados);
            return "redirect:/";
        }
        try {
            Solicitud creada = service.crear(form.toSolicitud());
            redirect.addFlashAttribute("solicitudCreada", creada.getCodigo());
        } catch (SolicitudValidationException error) {
            mostrarErrores(redirect, form, error.getErrores(), Map.of());
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("errorSolicitud", "No pudimos registrar la solicitud. Revisa los datos e inténtalo de nuevo.");
            redirect.addFlashAttribute("formAnterior", form);
        }
        return "redirect:/";
    }

    private void mostrarErrores(RedirectAttributes redirect, SolicitudForm form,
                               Map<String, String> errores, Map<String, String> valoresRechazados) {
        redirect.addFlashAttribute("errorSolicitud", "Revisa los datos ingresados e inténtalo de nuevo");
        redirect.addFlashAttribute("formAnterior", form);
        redirect.addFlashAttribute("erroresSolicitud", errores);
        redirect.addFlashAttribute("valoresRechazados", valoresRechazados);
        boolean primerPaso = errores.keySet().stream().anyMatch(campo ->
            campo.equals("nombreContacto") || campo.equals("telefonoContacto")
                || campo.equals("nombrePaciente") || campo.equals("edadPaciente")
                || campo.equals("tipoServicio"));
        redirect.addFlashAttribute("pasoErrorSolicitud", primerPaso ? 0 : 1);
    }

    private String mensajeCampo(String campo) {
        return switch (campo) {
            case "edadPaciente" -> "Ingresa una edad válida entre 0 y 120, o deja el campo vacío.";
            case "tipoServicio" -> "Selecciona uno de los servicios disponibles.";
            case "fechaRequerida" -> "Ingresa una fecha válida en formato AAAA-MM-DD.";
            case "turno" -> "Selecciona un turno válido.";
            case "telefonoContacto" -> "Ingresa un celular de nueve dígitos que empiece con 9.";
            case "nombreContacto" -> "Indica el nombre de contacto.";
            case "nombrePaciente" -> "Indica el nombre del paciente.";
            case "distrito" -> "Indica el distrito de atención.";
            case "descripcion" -> "Describe brevemente la necesidad de atención.";
            default -> "Revisa este campo.";
        };
    }
}
