package pe.edu.utp.alivio.service;

import java.util.Map;

public class SolicitudValidationException extends IllegalArgumentException {
    private final Map<String, String> errores;

    public SolicitudValidationException(Map<String, String> errores) {
        super("Revisa los datos ingresados e inténtalo de nuevo");
        this.errores = Map.copyOf(errores);
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
