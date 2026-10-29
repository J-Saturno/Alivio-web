package pe.edu.utp.alivio.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.edu.utp.alivio.validacion.Textos;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;

public class ProfesionalForm {
    private String codigo;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre debe tener como máximo 100 caracteres.")
    @Pattern(regexp = Textos.NOMBRE_O_VACIO, message = "Ingresa un nombre con letras, espacios, apóstrofes o guiones.")
    private String nombreCompleto;
    @NotNull(message = "Selecciona un tipo de profesional válido")
    private TipoProfesional tipoProfesional;
    @NotBlank(message = "El teléfono debe tener nueve dígitos")
    @Pattern(regexp = "9[0-9]{8}", message = "El teléfono debe tener nueve dígitos")
    private String telefono;
    @NotBlank(message = "La zona de cobertura es obligatoria")
    @Size(max = 80, message = "La zona de cobertura debe tener como máximo 80 caracteres.")
    @Pattern(regexp = Textos.COBERTURA_O_VACIO, message = "Ingresa distritos separados por comas, sin elementos vacíos. Ejemplo: Ate, La Molina.")
    private String zonaCobertura;
    @NotNull(message = "Selecciona una especialidad válida")
    private TipoServicio especialidad;
    private boolean disponible;

    public Profesional toProfesional() {
        return new Profesional(codigo, nombreCompleto, tipoProfesional, telefono,
                zonaCobertura, especialidad, disponible);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = Textos.linea(nombreCompleto);
    }

    public TipoProfesional getTipoProfesional() {
        return tipoProfesional;
    }

    public void setTipoProfesional(TipoProfesional tipoProfesional) {
        this.tipoProfesional = tipoProfesional;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = Textos.telefono(telefono);
    }

    public String getZonaCobertura() {
        return zonaCobertura;
    }

    public void setZonaCobertura(String zonaCobertura) {
        this.zonaCobertura = Textos.cobertura(zonaCobertura);
    }

    public TipoServicio getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(TipoServicio especialidad) {
        this.especialidad = especialidad;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
