package pe.edu.utp.alivio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "profesionales")
public class Profesional {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profesional_seq")
    @SequenceGenerator(name = "profesional_seq", sequenceName = "profesional_seq", allocationSize = 1)
    private Long id;
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombreCompleto;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoProfesional tipoProfesional;
    @NotBlank
    @Pattern(regexp = "9[0-9]{8}")
    @Column(nullable = false, length = 9)
    private String telefono;
    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String zonaCobertura;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoServicio especialidad;
    private boolean disponible;

    public Profesional() {
    }

    public Profesional(String codigo, String nombreCompleto, TipoProfesional tipoProfesional,
                       String telefono, String zonaCobertura, TipoServicio especialidad,
                       boolean disponible) {
        this.id = CodigoRegistro.id(codigo, "PRO").orElse(null);
        this.nombreCompleto = nombreCompleto;
        this.tipoProfesional = tipoProfesional;
        this.telefono = telefono;
        this.zonaCobertura = zonaCobertura;
        this.especialidad = especialidad;
        this.disponible = disponible;
    }

    public Profesional(Profesional original) {
        this(original.getCodigo(), original.getNombreCompleto(), original.getTipoProfesional(),
            original.getTelefono(), original.getZonaCobertura(), original.getEspecialidad(), original.isDisponible());
    }

    public Long getId() { return id; }

    public String getCodigo() {
        return id == null ? null : "PRO-%03d".formatted(id);
    }

    public void setCodigo(String codigo) {
        this.id = CodigoRegistro.id(codigo, "PRO").orElse(null);
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
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
        this.telefono = telefono;
    }

    public String getZonaCobertura() {
        return zonaCobertura;
    }

    public void setZonaCobertura(String zonaCobertura) {
        this.zonaCobertura = zonaCobertura;
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
