package pe.edu.utp.alivio.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.service.ProfesionalService;
import pe.edu.utp.alivio.web.ProfesionalForm;

@Controller
@RequestMapping("/admin/profesionales")
public class AdminProfesionalController {
    private final ProfesionalService service;

    public AdminProfesionalController(ProfesionalService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("profesionales", service.listarTodos());
        model.addAttribute("tiposProfesional", TipoProfesional.values());
        model.addAttribute("especialidades", TipoServicio.values());
        if (!model.containsAttribute("nuevoProfesional")) {
            model.addAttribute("nuevoProfesional", new ProfesionalForm());
        }
        if (!model.containsAttribute("edicionProfesional")) {
            model.addAttribute("edicionProfesional", new ProfesionalForm());
        }
        if (!model.containsAttribute("valoresRechazados")) {
            model.addAttribute("valoresRechazados", Map.of());
        }
        if (!model.containsAttribute("erroresCampo")) {
            model.addAttribute("erroresCampo", Map.of());
        }
        return "admin/profesionales";
    }

    @PostMapping
    public String registrar(@ModelAttribute("nuevoProfesional") ProfesionalForm form,
                            BindingResult binding, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            preservarError(form, binding, redirect, "registro", null, null);
            return "redirect:/admin/profesionales";
        }
        try {
            service.registrar(form.toProfesional());
            redirect.addFlashAttribute("mensaje", "Profesional registrada");
        } catch (IllegalArgumentException error) {
            preservarError(form, binding, redirect, "registro", null, error.getMessage());
        }
        return "redirect:/admin/profesionales";
    }

    @PostMapping("/{codigo}")
    public String actualizar(@PathVariable String codigo, @ModelAttribute("edicionProfesional") ProfesionalForm form,
                             BindingResult binding, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            preservarError(form, binding, redirect, "edicion", codigo, null);
            return "redirect:/admin/profesionales";
        }
        try {
            service.buscarPorCodigo(codigo);
            service.actualizar(codigo, form.toProfesional());
            redirect.addFlashAttribute("mensaje", "Profesional actualizada");
        } catch (IllegalArgumentException error) {
            preservarError(form, binding, redirect, "edicion", codigo, error.getMessage());
        }
        return "redirect:/admin/profesionales";
    }

    @PostMapping("/{codigo}/disponibilidad")
    public String cambiarDisponibilidad(@PathVariable String codigo, RedirectAttributes redirect) {
        try {
            service.cambiarDisponibilidad(codigo);
            redirect.addFlashAttribute("mensaje", "Disponibilidad actualizada");
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", error.getMessage());
        }
        return "redirect:/admin/profesionales";
    }

    private void preservarError(ProfesionalForm form, BindingResult binding, RedirectAttributes redirect,
                               String modal, String codigo, String mensajeServicio) {
        Map<String, String> rechazados = new HashMap<>();
        Map<String, String> errores = new HashMap<>();
        for (FieldError fieldError : binding.getFieldErrors()) {
            if (fieldError.getRejectedValue() != null) {
                rechazados.put(fieldError.getField(), fieldError.getRejectedValue().toString());
            }
            errores.put(fieldError.getField(), mensajeCampo(fieldError.getField()));
        }
        if (mensajeServicio != null) {
            switch (mensajeServicio) {
                case "El nombre es obligatorio" -> errores.put("nombreCompleto", mensajeServicio);
                case "El teléfono debe tener nueve dígitos" -> errores.put("telefono", mensajeServicio);
                case "La zona de cobertura es obligatoria" -> errores.put("zonaCobertura", mensajeServicio);
                case "Selecciona tipo y especialidad" -> {
                    if (form.getTipoProfesional() == null) errores.put("tipoProfesional", "Selecciona un tipo de profesional válido");
                    if (form.getEspecialidad() == null) errores.put("especialidad", "Selecciona una especialidad válida");
                }
                default -> { }
            }
        }
        redirect.addFlashAttribute(modal.equals("registro") ? "nuevoProfesional" : "edicionProfesional", form);
        redirect.addFlashAttribute("modalError", modal);
        if (codigo != null) redirect.addFlashAttribute("codigoEdicion", codigo);
        redirect.addFlashAttribute("valoresRechazados", rechazados);
        redirect.addFlashAttribute("erroresCampo", errores);
        redirect.addFlashAttribute("error", mensajeServicio == null
            ? "Revisa los campos señalados e inténtalo de nuevo" : mensajeServicio);
    }

    private String mensajeCampo(String campo) {
        return switch (campo) {
            case "tipoProfesional" -> "Selecciona un tipo de profesional válido";
            case "especialidad" -> "Selecciona una especialidad válida";
            case "disponible" -> "Indica si la profesional está disponible";
            case "telefono" -> "El teléfono debe tener nueve dígitos";
            default -> "Revisa este campo";
        };
    }
}
