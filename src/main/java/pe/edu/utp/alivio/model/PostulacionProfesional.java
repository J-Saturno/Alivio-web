package pe.edu.utp.alivio.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "postulaciones_profesionales")
public class PostulacionProfesional {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "postulacion_seq")
    @SequenceGenerator(name = "postulacion_seq", sequenceName = "postulacion_seq", allocationSize = 1)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nombreCompleto;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoProfesional tipoProfesional;
    @Column(nullable = false, length = 9)
    private String telefono;
    @Column(nullable = false, length = 80)
    private String zonaCobertura;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoServicio especialidad;
    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] cvPdf;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPostulacion estado = EstadoPostulacion.PENDIENTE;
    @Column(length = 500)
    private String notaRevision;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPostulacion;
    @Column(name = "profesional_creada_id")
    private Long profesionalCreadaId;

    public Long getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String valor) { nombreCompleto = valor; }
    public TipoProfesional getTipoProfesional() { return tipoProfesional; }
    public void setTipoProfesional(TipoProfesional valor) { tipoProfesional = valor; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String valor) { telefono = valor; }
    public String getZonaCobertura() { return zonaCobertura; }
    public void setZonaCobertura(String valor) { zonaCobertura = valor; }
    public TipoServicio getEspecialidad() { return especialidad; }
    public void setEspecialidad(TipoServicio valor) { especialidad = valor; }
    public byte[] getCvPdf() { return cvPdf; }
    public void setCvPdf(byte[] valor) { cvPdf = valor; }
    public EstadoPostulacion getEstado() { return estado; }
    public void setEstado(EstadoPostulacion valor) { estado = valor; }
    public String getNotaRevision() { return notaRevision; }
    public void setNotaRevision(String valor) { notaRevision = valor; }
    public LocalDateTime getFechaPostulacion() { return fechaPostulacion; }
    public Long getProfesionalCreadaId() { return profesionalCreadaId; }
    public void setProfesionalCreadaId(Long valor) { profesionalCreadaId = valor; }
}
