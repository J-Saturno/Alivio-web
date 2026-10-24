package pe.edu.utp.alivio.web;

import java.time.LocalDate;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.model.TurnoAtencion;

public class SolicitudForm {
    @NotBlank(message = "Indica el nombre de contacto.")
    @Size(max = 100, message = "El nombre de contacto debe tener como máximo 100 caracteres.")
    private String nombreContacto;
    @NotBlank(message = "Ingresa un celular de nueve dígitos que empiece con 9.")
    @Pattern(regexp = "9[0-9]{8}", message = "Ingresa un celular de nueve dígitos que empiece con 9.")
    private String telefonoContacto;
    @NotBlank(message = "Indica el nombre del paciente.")
    @Size(max = 100, message = "El nombre del paciente debe tener como máximo 100 caracteres.")
    private String nombrePaciente;
    @Min(value = 0, message = "Ingresa una edad entre 0 y 120.")
    @Max(value = 120, message = "Ingresa una edad entre 0 y 120.")
    private Integer edadPaciente;
    @NotNull(message = "Selecciona uno de los servicios disponibles.")
    private TipoServicio tipoServicio;
    @NotBlank(message = "Indica el distrito de atención.")
    @Size(max = 80, message = "El distrito debe tener como máximo 80 caracteres.")
    private String distrito;
    @NotNull(message = "Selecciona hoy o una fecha posterior.")
    @FutureOrPresent(message = "Selecciona hoy o una fecha posterior.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaRequerida;
    @NotNull(message = "Selecciona un turno válido.")
    private TurnoAtencion turno;
    @NotBlank(message = "Describe brevemente la necesidad de atención.")
    @Size(max = 500, message = "La descripción debe tener como máximo 500 caracteres.")
    private String descripcion;

    public Solicitud toSolicitud() {
        Solicitud solicitud = new Solicitud();
        solicitud.setNombreContacto(nombreContacto);
        solicitud.setTelefonoContacto(telefonoContacto);
        solicitud.setNombrePaciente(nombrePaciente);
        solicitud.setEdadPaciente(edadPaciente);
        solicitud.setTipoServicio(tipoServicio);
        solicitud.setDistrito(distrito);
        solicitud.setFechaRequerida(fechaRequerida);
        solicitud.setTurno(turno);
        solicitud.setDescripcion(descripcion);
        return solicitud;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }

    public Integer getEdadPaciente() {
        return edadPaciente;
    }

    public void setEdadPaciente(Integer edadPaciente) {
        this.edadPaciente = edadPaciente;
    }

    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(TipoServicio tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public LocalDate getFechaRequerida() {
        return fechaRequerida;
    }

    public void setFechaRequerida(LocalDate fechaRequerida) {
        this.fechaRequerida = fechaRequerida;
    }

    public TurnoAtencion getTurno() {
        return turno;
    }

    public void setTurno(TurnoAtencion turno) {
        this.turno = turno;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
