package pe.edu.utp.alivio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.service.ProfesionalService;

@Controller
@RequestMapping("/admin/profesionales")
public class AdminEliminacionProfesionalController {
    private final ProfesionalService profesionales;

    public AdminEliminacionProfesionalController(ProfesionalService profesionales) {
        this.profesionales = profesionales;
    }

    @GetMapping("/{codigo}/eliminar")
    public String confirmar(@PathVariable String codigo, Model model, RedirectAttributes redirect) {
        try {
            Profesional profesional = profesionales.buscarPorCodigo(codigo);
            model.addAttribute("titulo", "Eliminar profesional " + profesional.getCodigo());
            model.addAttribute("detalle", "Se eliminará el registro de " + profesional.getNombreCompleto()
                + ". Solo se permite si no tiene solicitudes vinculadas ni historial de verificaciones.");
            model.addAttribute("postAction", "/admin/profesionales/" + profesional.getCodigo() + "/eliminar");
            model.addAttribute("volver", "/admin/profesionales");
            return "admin/confirmar-eliminacion";
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", error.getMessage());
            return "redirect:/admin/profesionales";
        }
    }

    @PostMapping("/{codigo}/eliminar")
    public String eliminar(@PathVariable String codigo, RedirectAttributes redirect) {
        try {
            profesionales.eliminar(codigo);
            redirect.addFlashAttribute("mensaje", "Profesional " + codigo + " eliminada");
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", error.getMessage());
        }
        return "redirect:/admin/profesionales";
    }
}
