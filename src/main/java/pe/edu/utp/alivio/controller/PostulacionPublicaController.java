package pe.edu.utp.alivio.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.service.PostulacionService;
import pe.edu.utp.alivio.web.PostulacionForm;

@Controller
public class PostulacionPublicaController {
    private final PostulacionService service;

    public PostulacionPublicaController(PostulacionService service) {
        this.service = service;
    }

    @GetMapping("/postular")
    public String formulario(Model model) {
        if (!model.containsAttribute("postulacionForm")) model.addAttribute("postulacionForm", new PostulacionForm());
        model.addAttribute("tiposProfesional", TipoProfesional.values());
        model.addAttribute("especialidades", TipoServicio.values());
        return "postular";
    }

    @PostMapping("/postulaciones")
    public String registrar(@Valid @ModelAttribute("postulacionForm") PostulacionForm form,
            BindingResult binding, @RequestParam(required = false) MultipartFile cv,
            Model model, RedirectAttributes redirect) {
        if (!binding.hasErrors()) {
            try {
                service.registrar(form.toPostulacion(), cv);
                redirect.addFlashAttribute("mensaje", "Recibimos tu postulación. El equipo de Alivio revisará tus datos y te contactará por llamada o mensaje.");
                return "redirect:/postular";
            } catch (IllegalArgumentException error) {
                model.addAttribute("errorCv", error.getMessage());
            }
        }
        return formulario(model);
    }
}
