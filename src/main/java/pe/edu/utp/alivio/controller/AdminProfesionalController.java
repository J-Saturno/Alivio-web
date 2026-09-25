package pe.edu.utp.alivio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
        return "admin/profesionales";
    }

    @PostMapping
    public String registrar(@ModelAttribute ProfesionalForm form, BindingResult binding, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("error", "Revisa los datos de la profesional e inténtalo de nuevo");
            redirect.addFlashAttribute("nuevoProfesional", form);
            return "redirect:/admin/profesionales";
        }
        try {
            service.registrar(form.toProfesional());
            redirect.addFlashAttribute("mensaje", "Profesional registrada");
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", error.getMessage());
            redirect.addFlashAttribute("nuevoProfesional", form);
        }
        return "redirect:/admin/profesionales";
    }

    @PostMapping("/{codigo}")
    public String actualizar(@PathVariable String codigo, @ModelAttribute ProfesionalForm form,
                             BindingResult binding, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("error", "Revisa los datos de la profesional e inténtalo de nuevo");
            return "redirect:/admin/profesionales";
        }
        try {
            service.buscarPorCodigo(codigo);
            service.actualizar(codigo, form.toProfesional());
            redirect.addFlashAttribute("mensaje", "Profesional actualizada");
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", error.getMessage());
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
}
