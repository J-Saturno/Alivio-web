package pe.edu.utp.alivio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Entity
@Table(name = "solicitudes")
public class Solicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "solicitud_seq")
    @SequenceGenerator(name = "solicitud_seq", sequenceName = "solicitud_seq", allocationSize = 1)
    private Long id;
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombreContacto;
    @NotBlank
    @Pattern(regexp = "9[0-9]{8}")
    @Column(nullable = false, length = 9)
    private String telefonoContacto;
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombrePaciente;
    @Min(0)
    @Max(120)
    private Integer edadPaciente;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoServicio tipoServicio;
    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String distrito;
    @NotNull
    @Column(nullable = false)
    private LocalDate fechaRequerida;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TurnoAtencion turno;
    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String descripcion;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EstadoSolicitud estado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", foreignKey = @ForeignKey(name = "fk_solicitud_profesional"))
    private Profesional profesionalAsignado;

    public Solicitud() {
    }

    public Solicitud(String codigo, String nombreContacto, String telefonoContacto,
                     String nombrePaciente, Integer edadPaciente, TipoServicio tipoServicio,
                     String distrito, LocalDate fechaRequerida, TurnoAtencion turno,
                     String descripcion, EstadoSolicitud estado, Profesional profesionalAsignado) {
        this.id = CodigoRegistro.id(codigo, "SOL").orElse(null);
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
        this(original.getCodigo(), original.nombreContacto, original.telefonoContacto,
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

    public Long getId() { return id; }

    public String getCodigo() { return id == null ? null : "SOL-%03d".formatted(id); }
    public void setCodigo(String codigo) { this.id = CodigoRegistro.id(codigo, "SOL").orElse(null); }
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
