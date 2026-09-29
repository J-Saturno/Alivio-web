package pe.edu.utp.alivio.web;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.model.TurnoAtencion;

public class SolicitudForm {
    private String nombreContacto;
    private String telefonoContacto;
    private String nombrePaciente;
    private Integer edadPaciente;
    private TipoServicio tipoServicio;
    private String distrito;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaRequerida;
    private TurnoAtencion turno;
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
