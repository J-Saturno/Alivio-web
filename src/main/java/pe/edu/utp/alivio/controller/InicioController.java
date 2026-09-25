package pe.edu.utp.alivio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.model.TurnoAtencion;
import pe.edu.utp.alivio.web.SolicitudForm;
import java.util.Map;

@Controller
public class InicioController {
    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("tiposServicio", TipoServicio.values());
        model.addAttribute("turnos", TurnoAtencion.values());
        model.addAttribute("etiquetasServicio", Map.of(
            TipoServicio.ADULTO_MAYOR, "Cuidado del adulto mayor",
            TipoServicio.CURACIONES_POSTOPERATORIO, "Curaciones y cuidado postoperatorio",
            TipoServicio.INYECTABLES_TRATAMIENTOS, "Inyectables y tratamientos indicados",
            TipoServicio.ACOMPANAMIENTO_DOMICILIARIO, "Acompañamiento domiciliario"));
        model.addAttribute("etiquetasTurno", Map.of(
            TurnoAtencion.MANANA, "Mañana", TurnoAtencion.TARDE, "Tarde",
            TurnoAtencion.NOCHE, "Noche", TurnoAtencion.DOCE_HORAS, "12 horas",
            TurnoAtencion.VEINTICUATRO_HORAS, "24 horas"));
        if (!model.containsAttribute("formAnterior")) {
            model.addAttribute("formAnterior", new SolicitudForm());
        }
        return "inicio";
    }
}
