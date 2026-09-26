package pe.edu.utp.alivio.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.service.ProfesionalService;
import pe.edu.utp.alivio.service.SolicitudService;

@Controller
@RequestMapping("/admin/solicitudes")
public class AdminSolicitudController {
    private final SolicitudService solicitudes;
    private final ProfesionalService profesionales;

    public AdminSolicitudController(SolicitudService solicitudes, ProfesionalService profesionales) {
        this.solicitudes = solicitudes;
        this.profesionales = profesionales;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "") String estado, Model model) {
        EstadoSolicitud seleccionado = null;
        if (!estado.isBlank()) {
            try {
                seleccionado = EstadoSolicitud.valueOf(estado);
            } catch (IllegalArgumentException error) {
                model.addAttribute("error", "Selecciona un estado válido para filtrar");
            }
        }
        List<Solicitud> todas = solicitudes.listar("", null);
        model.addAttribute("solicitudes", solicitudes.listar(q, seleccionado));
        model.addAttribute("profesionalesDisponibles", profesionales.listarDisponibles());
        model.addAttribute("estados", EstadoSolicitud.values());
        model.addAttribute("q", q);
        model.addAttribute("estadoSeleccionado", seleccionado);
        model.addAttribute("pendientes", contar(todas, EstadoSolicitud.PENDIENTE));
        model.addAttribute("asignadas", contar(todas, EstadoSolicitud.ASIGNADA));
        model.addAttribute("finalizadas", contar(todas, EstadoSolicitud.FINALIZADA));
        return "admin/solicitudes";
    }

    @PostMapping("/{codigo}/asignar")
    public String asignar(@PathVariable String codigo,
                          @RequestParam(defaultValue = "") String profesionalCodigo,
                          RedirectAttributes redirect) {
        if (profesionalCodigo.isBlank()) {
            redirect.addFlashAttribute("error", "Selecciona una profesional disponible");
        } else {
            try {
                solicitudes.asignar(codigo, profesionalCodigo);
                redirect.addFlashAttribute("mensaje", "Profesional asignada a la solicitud " + codigo);
            } catch (IllegalArgumentException error) {
                redirect.addFlashAttribute("error", error.getMessage());
            }
        }
        return "redirect:/admin/solicitudes";
    }

    @PostMapping("/{codigo}/estado")
    public String actualizarEstado(@PathVariable String codigo,
                                   @RequestParam(defaultValue = "") String estado,
                                   RedirectAttributes redirect) {
        try {
            EstadoSolicitud siguiente = EstadoSolicitud.valueOf(estado);
            solicitudes.actualizarEstado(codigo, siguiente);
            redirect.addFlashAttribute("mensaje", "Estado de la solicitud " + codigo + " actualizado");
        } catch (IllegalArgumentException error) {
            String mensaje = esEstadoValido(estado) ? error.getMessage() : "Selecciona un estado válido";
            redirect.addFlashAttribute("error", mensaje);
        }
        return "redirect:/admin/solicitudes";
    }

    private long contar(List<Solicitud> solicitudes, EstadoSolicitud estado) {
        return solicitudes.stream().filter(s -> s.getEstado() == estado).count();
    }

    private boolean esEstadoValido(String estado) {
        try {
            EstadoSolicitud.valueOf(estado);
            return true;
        } catch (IllegalArgumentException error) {
            return false;
        }
    }
}
