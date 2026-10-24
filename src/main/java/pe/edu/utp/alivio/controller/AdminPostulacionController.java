package pe.edu.utp.alivio.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.alivio.service.PostulacionService;

@Controller
@RequestMapping("/admin/postulaciones")
public class AdminPostulacionController {
    private final PostulacionService service;

    public AdminPostulacionController(PostulacionService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("postulaciones", service.listar());
        return "admin/postulaciones";
    }

    @GetMapping("/{id}/cv")
    public ResponseEntity<byte[]> descargarCv(@PathVariable Long id) {
        byte[] contenido = service.buscar(id).getCvPdf();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cv-postulacion-" + id + ".pdf\"")
            .header("X-Content-Type-Options", "nosniff")
            .contentType(MediaType.APPLICATION_PDF)
            .body(contenido);
    }

    @PostMapping("/{id}/aceptar")
    public String aceptar(@PathVariable Long id, @RequestParam(defaultValue = "") String notaRevision,
            RedirectAttributes redirect) {
        try {
            service.aceptar(id, notaRevision);
            redirect.addFlashAttribute("mensaje", "Postulación aceptada. Contacta a la profesional por llamada o mensaje antes de coordinar su disponibilidad.");
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", error.getMessage());
        }
        return "redirect:/admin/postulaciones";
    }

    @PostMapping("/{id}/rechazar")
    public String rechazar(@PathVariable Long id, @RequestParam(defaultValue = "") String notaRevision,
            RedirectAttributes redirect) {
        try {
            service.rechazar(id, notaRevision);
            redirect.addFlashAttribute("mensaje", "Postulación rechazada. Comunica la decisión por llamada o mensaje.");
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", error.getMessage());
        }
        return "redirect:/admin/postulaciones";
    }
}
