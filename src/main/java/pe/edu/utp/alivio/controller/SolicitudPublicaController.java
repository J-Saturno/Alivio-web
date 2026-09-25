package pe.edu.utp.alivio.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.service.SolicitudService;
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
            for (FieldError error : binding.getFieldErrors()) {
                if (error.getRejectedValue() != null) {
                    valoresRechazados.put(error.getField(), error.getRejectedValue().toString());
                }
            }
            redirect.addFlashAttribute("errorSolicitud", "Revisa los datos ingresados e inténtalo de nuevo");
            redirect.addFlashAttribute("formAnterior", form);
            redirect.addFlashAttribute("valoresRechazados", valoresRechazados);
            boolean errorPrimerPaso = binding.getFieldErrors().stream().anyMatch(error ->
                error.getField().equals("edadPaciente") || error.getField().equals("tipoServicio")
                || error.getField().equals("nombreContacto") || error.getField().equals("telefonoContacto")
                || error.getField().equals("nombrePaciente"));
            redirect.addFlashAttribute("pasoErrorSolicitud", errorPrimerPaso ? 0 : 1);
            return "redirect:/";
        }
        try {
            Solicitud creada = service.crear(form.toSolicitud());
            redirect.addFlashAttribute("solicitudCreada", creada.getCodigo());
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("errorSolicitud", error.getMessage());
            redirect.addFlashAttribute("formAnterior", form);
        }
        return "redirect:/";
    }
}
