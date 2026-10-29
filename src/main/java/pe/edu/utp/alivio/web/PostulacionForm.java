package pe.edu.utp.alivio.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.edu.utp.alivio.validacion.Textos;
import pe.edu.utp.alivio.model.PostulacionProfesional;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;

public class PostulacionForm {
    @NotBlank(message = "Indica tu nombre completo")
    @Size(max = 100, message = "El nombre debe tener como máximo 100 caracteres")
    @Pattern(regexp = Textos.NOMBRE_O_VACIO, message = "Ingresa un nombre con letras, espacios, apóstrofes o guiones.")
    private String nombreCompleto;
    @NotNull(message = "Selecciona tu tipo profesional")
    private TipoProfesional tipoProfesional;
    @NotBlank(message = "Indica tu celular")
    @Pattern(regexp = "9[0-9]{8}", message = "Ingresa un celular de nueve dígitos que empiece con 9")
    private String telefono;
    @NotBlank(message = "Indica tu zona de cobertura")
    @Size(max = 80, message = "La zona debe tener como máximo 80 caracteres")
    @Pattern(regexp = Textos.COBERTURA_O_VACIO, message = "Ingresa distritos separados por comas, sin elementos vacíos. Ejemplo: Ate, La Molina.")
    private String zonaCobertura;
    @NotNull(message = "Selecciona tu especialidad")
    private TipoServicio especialidad;

    public PostulacionProfesional toPostulacion() {
        var postulacion = new PostulacionProfesional();
        postulacion.setNombreCompleto(nombreCompleto);
        postulacion.setTipoProfesional(tipoProfesional);
        postulacion.setTelefono(telefono);
        postulacion.setZonaCobertura(zonaCobertura);
        postulacion.setEspecialidad(especialidad);
        return postulacion;
    }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String valor) { nombreCompleto = Textos.linea(valor); }
    public TipoProfesional getTipoProfesional() { return tipoProfesional; }
    public void setTipoProfesional(TipoProfesional valor) { tipoProfesional = valor; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String valor) { telefono = Textos.telefono(valor); }
    public String getZonaCobertura() { return zonaCobertura; }
    public void setZonaCobertura(String valor) { zonaCobertura = Textos.cobertura(valor); }
    public TipoServicio getEspecialidad() { return especialidad; }
    public void setEspecialidad(TipoServicio valor) { especialidad = valor; }
}
