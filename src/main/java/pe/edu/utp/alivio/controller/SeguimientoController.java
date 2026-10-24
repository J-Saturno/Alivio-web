package pe.edu.utp.alivio.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pe.edu.utp.alivio.service.SeguimientoService;

@Controller
public class SeguimientoController {
    private final SeguimientoService seguimiento;

    public SeguimientoController(SeguimientoService seguimiento) {
        this.seguimiento = seguimiento;
    }

    @GetMapping("/seguimiento/{token}")
    public String consultar(@PathVariable String token, Model model, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("X-Robots-Tag", "noindex, nofollow");
        var resumen = seguimiento.consultar(token);
        if (resumen.isPresent()) {
            model.addAttribute("resumen", resumen.get());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        return "seguimiento";
    }
}
