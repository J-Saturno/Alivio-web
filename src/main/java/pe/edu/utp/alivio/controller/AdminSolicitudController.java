package pe.edu.utp.alivio.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.model.TurnoAtencion;
import pe.edu.utp.alivio.service.SolicitudService;
import pe.edu.utp.alivio.service.SeguimientoService;
import pe.edu.utp.alivio.service.SolicitudValidationException;
import pe.edu.utp.alivio.web.SolicitudForm;

@Controller
@RequestMapping("/admin/solicitudes")
public class AdminSolicitudController {
    private final SolicitudService solicitudes;
    private final SeguimientoService seguimiento;

    public AdminSolicitudController(SolicitudService solicitudes, SeguimientoService seguimiento) {
        this.solicitudes = solicitudes;
        this.seguimiento = seguimiento;
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
        model.addAttribute("estados", EstadoSolicitud.values());
        model.addAttribute("q", q);
        model.addAttribute("estadoSeleccionado", seleccionado);
        model.addAttribute("pendientes", contar(todas, EstadoSolicitud.PENDIENTE));
        model.addAttribute("asignadas", contar(todas, EstadoSolicitud.ASIGNADA));
        model.addAttribute("finalizadas", contar(todas, EstadoSolicitud.FINALIZADA));
        return "admin/solicitudes";
    }

    @GetMapping("/{codigo}/candidatas")
    public ResponseEntity<List<Candidata>> candidatas(@PathVariable String codigo) {
        try {
            List<Candidata> opciones = solicitudes.candidatasCompatibles(codigo).stream()
                .map(p -> new Candidata(p.getCodigo(), p.getNombreCompleto(), p.getZonaCobertura()))
                .toList();
            return ResponseEntity.ok(opciones);
        } catch (IllegalArgumentException error) {
            return ResponseEntity.notFound().build();
        }
    }

    public record Candidata(String codigo, String nombre, String zonaCobertura) {}

    @PostMapping("/{codigo}/seguimiento/emitir")
    public String emitirSeguimiento(@PathVariable String codigo, RedirectAttributes redirect) {
        try {
            String token = seguimiento.emitir(codigo);
            redirect.addFlashAttribute("enlaceSeguimiento", "/seguimiento/" + token);
            redirect.addFlashAttribute("mensaje", "Enlace privado generado para la solicitud " + codigo);
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", "Solicitud no encontrada");
        }
        return "redirect:/admin/solicitudes";
    }

    @PostMapping("/{codigo}/seguimiento/revocar")
    public String revocarSeguimiento(@PathVariable String codigo, RedirectAttributes redirect) {
        try {
            seguimiento.revocar(codigo);
            redirect.addFlashAttribute("mensaje", "Enlace de seguimiento revocado para la solicitud " + codigo);
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", "Solicitud no encontrada");
        }
        return "redirect:/admin/solicitudes";
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

    @GetMapping("/{codigo}/editar")
    public String editar(@PathVariable String codigo, Model model, RedirectAttributes redirect) {
        try {
            Solicitud solicitud = solicitudes.buscarPorCodigo(codigo);
            if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE
                    && solicitud.getEstado() != EstadoSolicitud.EN_EVALUACION) {
                throw new IllegalArgumentException("Solo se pueden editar solicitudes pendientes o en evaluación");
            }
            SolicitudForm form = new SolicitudForm();
            form.setNombreContacto(solicitud.getNombreContacto());
            form.setTelefonoContacto(solicitud.getTelefonoContacto());
            form.setNombrePaciente(solicitud.getNombrePaciente());
            form.setEdadPaciente(solicitud.getEdadPaciente());
            form.setTipoServicio(solicitud.getTipoServicio());
            form.setDistrito(solicitud.getDistrito());
            form.setFechaRequerida(solicitud.getFechaRequerida());
            form.setTurno(solicitud.getTurno());
            form.setDescripcion(solicitud.getDescripcion());
            model.addAttribute("solicitudForm", form);
            return formulario(codigo, model);
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", error.getMessage());
            return "redirect:/admin/solicitudes";
        }
    }

    @PostMapping("/{codigo}/editar")
    public String guardarEdicion(@PathVariable String codigo,
                                @Valid @ModelAttribute("solicitudForm") SolicitudForm form,
                                BindingResult binding, Model model, RedirectAttributes redirect) {
        if (!binding.hasErrors()) {
            try {
                solicitudes.actualizar(codigo, form.toSolicitud());
                redirect.addFlashAttribute("mensaje", "Solicitud " + codigo + " actualizada");
                return "redirect:/admin/solicitudes";
            } catch (SolicitudValidationException error) {
                error.getErrores().forEach((campo, mensaje) -> binding.rejectValue(campo, "validacion", mensaje));
            } catch (IllegalArgumentException error) {
                model.addAttribute("error", error.getMessage());
            }
        }
        Map<String, String> valoresRechazados = new HashMap<>();
        binding.getFieldErrors().stream().filter(error -> error.isBindingFailure() && error.getRejectedValue() != null)
            .forEach(error -> valoresRechazados.put(error.getField(), error.getRejectedValue().toString()));
        model.addAttribute("valoresRechazados", valoresRechazados);
        return formulario(codigo, model);
    }

    @GetMapping("/{codigo}/eliminar")
    public String confirmarEliminacion(@PathVariable String codigo, Model model, RedirectAttributes redirect) {
        try {
            Solicitud solicitud = solicitudes.buscarPorCodigo(codigo);
            if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE
                    && solicitud.getEstado() != EstadoSolicitud.CANCELADA) {
                throw new IllegalArgumentException("Solo se pueden eliminar solicitudes pendientes o canceladas");
            }
            model.addAttribute("titulo", "Eliminar solicitud " + codigo);
            model.addAttribute("detalle", "Se eliminará la solicitud de " + solicitud.getNombrePaciente() + ".");
            model.addAttribute("postAction", "/admin/solicitudes/" + solicitud.getCodigo() + "/eliminar");
            model.addAttribute("volver", "/admin/solicitudes");
            return "admin/confirmar-eliminacion";
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", error.getMessage());
            return "redirect:/admin/solicitudes";
        }
    }

    @PostMapping("/{codigo}/eliminar")
    public String eliminar(@PathVariable String codigo, RedirectAttributes redirect) {
        try {
            solicitudes.eliminar(codigo);
            redirect.addFlashAttribute("mensaje", "Solicitud " + codigo + " eliminada");
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", error.getMessage());
        }
        return "redirect:/admin/solicitudes";
    }

    private String formulario(String codigo, Model model) {
        if (!model.containsAttribute("valoresRechazados")) {
            model.addAttribute("valoresRechazados", Map.of());
        }
        model.addAttribute("codigo", codigo);
        model.addAttribute("tiposServicio", TipoServicio.values());
        model.addAttribute("turnos", TurnoAtencion.values());
        return "admin/solicitud-formulario";
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
