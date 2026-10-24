package pe.edu.utp.alivio.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "verificaciones_profesionales")
public class VerificacionProfesional {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "verificacion_seq")
    @SequenceGenerator(name = "verificacion_seq", sequenceName = "verificacion_seq", allocationSize = 1)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesional_id", nullable = false, updatable = false)
    private Profesional profesional;
    @Column(nullable = false, updatable = false)
    private boolean identidadComprobada;
    @Column(nullable = false, updatable = false)
    private boolean credencialComprobada;
    @Column(nullable = false, updatable = false)
    private boolean referenciasComprobadas;
    @Column(updatable = false)
    private Integer aniosExperiencia;
    @Column(length = 100, updatable = false)
    private String referenciaCredencial;
    @Column(length = 1000, updatable = false)
    private String notaEvidencia;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private ResultadoVerificacion resultado;
    @Column(nullable = false, length = 100, updatable = false)
    private String revisor;
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    protected VerificacionProfesional() { }

    public VerificacionProfesional(Profesional profesional, boolean identidadComprobada,
            boolean credencialComprobada, boolean referenciasComprobadas, Integer aniosExperiencia,
            String referenciaCredencial, String notaEvidencia, ResultadoVerificacion resultado,
            String revisor, LocalDateTime fecha) {
        this.profesional = profesional;
        this.identidadComprobada = identidadComprobada;
        this.credencialComprobada = credencialComprobada;
        this.referenciasComprobadas = referenciasComprobadas;
        this.aniosExperiencia = aniosExperiencia;
        this.referenciaCredencial = referenciaCredencial;
        this.notaEvidencia = notaEvidencia;
        this.resultado = resultado;
        this.revisor = revisor;
        this.fecha = fecha;
    }
    public Long getId() { return id; }
    public Profesional getProfesional() { return profesional; }
    public boolean isIdentidadComprobada() { return identidadComprobada; }
    public boolean isCredencialComprobada() { return credencialComprobada; }
    public boolean isReferenciasComprobadas() { return referenciasComprobadas; }
    public Integer getAniosExperiencia() { return aniosExperiencia; }
    public String getReferenciaCredencial() { return referenciaCredencial; }
    public String getNotaEvidencia() { return notaEvidencia; }
    public ResultadoVerificacion getResultado() { return resultado; }
    public String getRevisor() { return revisor; }
    public LocalDateTime getFecha() { return fecha; }
}
