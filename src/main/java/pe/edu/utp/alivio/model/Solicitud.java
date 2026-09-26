package pe.edu.utp.alivio.model;

import java.time.LocalDate;

public class Solicitud {
    private String codigo;
    private String nombreContacto;
    private String telefonoContacto;
    private String nombrePaciente;
    private Integer edadPaciente;
    private TipoServicio tipoServicio;
    private String distrito;
    private LocalDate fechaRequerida;
    private TurnoAtencion turno;
    private String descripcion;
    private EstadoSolicitud estado;
    private Profesional profesionalAsignado;

    public Solicitud() {
    }

    public Solicitud(String codigo, String nombreContacto, String telefonoContacto,
                     String nombrePaciente, Integer edadPaciente, TipoServicio tipoServicio,
                     String distrito, LocalDate fechaRequerida, TurnoAtencion turno,
                     String descripcion, EstadoSolicitud estado, Profesional profesionalAsignado) {
        this.codigo = codigo;
        this.nombreContacto = nombreContacto;
        this.telefonoContacto = telefonoContacto;
        this.nombrePaciente = nombrePaciente;
        this.edadPaciente = edadPaciente;
        this.tipoServicio = tipoServicio;
        this.distrito = distrito;
        this.fechaRequerida = fechaRequerida;
        this.turno = turno;
        this.descripcion = descripcion;
        this.estado = estado;
        this.profesionalAsignado = profesionalAsignado;
    }

    public Solicitud(Solicitud original) {
        this(original.codigo, original.nombreContacto, original.telefonoContacto,
            original.nombrePaciente, original.edadPaciente, original.tipoServicio,
            original.distrito, original.fechaRequerida, original.turno, original.descripcion,
            original.estado, copiarProfesional(original.profesionalAsignado));
    }

    private static Profesional copiarProfesional(Profesional profesional) {
        if (profesional == null) {
            return null;
        }
        return new Profesional(profesional.getCodigo(), profesional.getNombreCompleto(),
            profesional.getTipoProfesional(), profesional.getTelefono(), profesional.getZonaCobertura(),
            profesional.getEspecialidad(), profesional.isDisponible());
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombreContacto() { return nombreContacto; }
    public void setNombreContacto(String nombreContacto) { this.nombreContacto = nombreContacto; }
    public String getTelefonoContacto() { return telefonoContacto; }
    public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }
    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }
    public Integer getEdadPaciente() { return edadPaciente; }
    public void setEdadPaciente(Integer edadPaciente) { this.edadPaciente = edadPaciente; }
    public TipoServicio getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(TipoServicio tipoServicio) { this.tipoServicio = tipoServicio; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }
    public LocalDate getFechaRequerida() { return fechaRequerida; }
    public void setFechaRequerida(LocalDate fechaRequerida) { this.fechaRequerida = fechaRequerida; }
    public TurnoAtencion getTurno() { return turno; }
    public void setTurno(TurnoAtencion turno) { this.turno = turno; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }
    public Profesional getProfesionalAsignado() { return profesionalAsignado; }
    public void setProfesionalAsignado(Profesional profesionalAsignado) { this.profesionalAsignado = profesionalAsignado; }
}
