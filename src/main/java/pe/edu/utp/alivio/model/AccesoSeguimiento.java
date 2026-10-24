package pe.edu.utp.alivio.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "accesos_seguimiento")
public class AccesoSeguimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "acceso_seguimiento_seq")
    @SequenceGenerator(name = "acceso_seguimiento_seq", sequenceName = "acceso_seguimiento_seq", allocationSize = 1)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false, unique = true)
    private Solicitud solicitud;
    @Column(length = 64, unique = true)
    private String tokenHash;
    private LocalDateTime expiraEn;
    @Column(nullable = false)
    private LocalDateTime emitidoEn;

    public Solicitud getSolicitud() { return solicitud; }
    public void setSolicitud(Solicitud solicitud) { this.solicitud = solicitud; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public LocalDateTime getExpiraEn() { return expiraEn; }
    public void setExpiraEn(LocalDateTime expiraEn) { this.expiraEn = expiraEn; }
    public LocalDateTime getEmitidoEn() { return emitidoEn; }
    public void setEmitidoEn(LocalDateTime emitidoEn) { this.emitidoEn = emitidoEn; }
}
