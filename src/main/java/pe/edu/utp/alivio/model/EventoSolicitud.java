package pe.edu.utp.alivio.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_solicitud")
public class EventoSolicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evento_solicitud_seq")
    @SequenceGenerator(name = "evento_solicitud_seq", sequenceName = "evento_solicitud_seq", allocationSize = 1)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40, updatable = false)
    private EstadoSolicitud estado;
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    public EventoSolicitud() {}
    public EventoSolicitud(Solicitud solicitud, EstadoSolicitud estado, LocalDateTime fecha) {
        this.solicitud = solicitud;
        this.estado = estado;
        this.fecha = fecha;
    }
    public EstadoSolicitud getEstado() { return estado; }
    public LocalDateTime getFecha() { return fecha; }
}
