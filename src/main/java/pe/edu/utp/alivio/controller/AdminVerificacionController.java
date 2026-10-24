package pe.edu.utp.alivio.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.alivio.model.ResultadoVerificacion;
import pe.edu.utp.alivio.service.ProfesionalService;
import pe.edu.utp.alivio.service.VerificacionService;
import pe.edu.utp.alivio.web.VerificacionForm;

@Controller
@RequestMapping("/admin/profesionales/{codigo}/verificaciones")
public class AdminVerificacionController {
    private final VerificacionService service;
    private final ProfesionalService profesionales;

    public AdminVerificacionController(VerificacionService service, ProfesionalService profesionales) {
        this.service = service;
        this.profesionales = profesionales;
    }

    @GetMapping
    public String mostrar(@PathVariable String codigo, Model model, RedirectAttributes redirect) {
        model.addAttribute("verificacionForm", new VerificacionForm());
        return pantalla(codigo, model, redirect);
    }

    @PostMapping
    public String registrar(@PathVariable String codigo,
            @Valid @ModelAttribute("verificacionForm") VerificacionForm form, BindingResult binding,
            Principal principal, Model model, RedirectAttributes redirect) {
        if (!binding.hasErrors()) {
            try {
                service.registrar(codigo, form, principal == null ? null : principal.getName());
                redirect.addFlashAttribute("mensaje", "Revisión registrada en el historial");
                return "redirect:/admin/profesionales/" + codigo + "/verificaciones";
            } catch (IllegalArgumentException error) {
                binding.reject("verificacion", error.getMessage());
            }
        }
        Map<String, String> errores = new HashMap<>();
        Map<String, String> rechazados = new HashMap<>();
        for (var error : binding.getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.isBindingFailure() ? switch (error.getField()) {
                case "resultado" -> "Selecciona un resultado válido";
                case "aniosExperiencia" -> "Indica una cantidad entera entre 0 y 80 años";
                default -> "Indica si esta comprobación fue realizada";
            } : error.getDefaultMessage());
            if (error.isBindingFailure() && error.getRejectedValue() != null) {
                rechazados.put(error.getField(), error.getRejectedValue().toString());
            }
        }
        model.addAttribute("erroresCampo", errores);
        model.addAttribute("valoresRechazados", rechazados);
        return pantalla(codigo, model, redirect);
    }

    private String pantalla(String codigo, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("profesional", profesionales.buscarPorCodigo(codigo));
            model.addAttribute("historial", service.historial(codigo));
            model.addAttribute("resultados", ResultadoVerificacion.values());
            if (!model.containsAttribute("erroresCampo")) model.addAttribute("erroresCampo", Map.of());
            if (!model.containsAttribute("valoresRechazados")) model.addAttribute("valoresRechazados", Map.of());
            return "admin/verificaciones";
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", error.getMessage());
            return "redirect:/admin/profesionales";
        }
    }
}
