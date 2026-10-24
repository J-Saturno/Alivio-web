package pe.edu.utp.alivio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminLoginController {
    @GetMapping("/admin/ingresar")
    public String ingresar() {
        return "admin/ingresar";
    }
}
