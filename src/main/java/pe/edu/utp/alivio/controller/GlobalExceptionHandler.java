package pe.edu.utp.alivio.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    public String cvDemasiadoGrande(Model model) {
        model.addAttribute("postulacionForm", new pe.edu.utp.alivio.web.PostulacionForm());
        model.addAttribute("tiposProfesional", pe.edu.utp.alivio.model.TipoProfesional.values());
        model.addAttribute("especialidades", pe.edu.utp.alivio.model.TipoServicio.values());
        model.addAttribute("errorCv", "El CV debe ser un PDF de hasta 5 MB");
        return "postular";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String errorGeneral(Exception error, Model model) {
        model.addAttribute("mensaje", "No pudimos completar la operación. Inténtalo nuevamente.");
        return "error/500";
    }
}
