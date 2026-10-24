package pe.edu.utp.alivio.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import pe.edu.utp.alivio.model.*;
import pe.edu.utp.alivio.repository.*;

@Service
@Transactional(readOnly = true)
public class SeguimientoService {
    private final SolicitudRepository solicitudes;
    private final AccesoSeguimientoRepository accesos;
    private final EventoSolicitudRepository eventos;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public SeguimientoService(SolicitudRepository solicitudes, AccesoSeguimientoRepository accesos,
            EventoSolicitudRepository eventos, Clock clock) {
        this.solicitudes = solicitudes;
        this.accesos = accesos;
        this.eventos = eventos;
        this.clock = clock;
    }

    public record Paso(EstadoSolicitud estado, LocalDateTime fecha) {}
    public record ResumenSeguimiento(String codigo, EstadoSolicitud estado, LocalDateTime actualizado,
            List<Paso> historial, LocalDateTime desde) {}

    /** El token se devuelve una sola vez; no debe registrarse en logs. */
    @Transactional
    public String emitir(String codigo) {
        Solicitud solicitud = bloquear(codigo);
        AccesoSeguimiento acceso = accesos.findBySolicitudId(solicitud.getId()).orElseGet(() -> {
            AccesoSeguimiento nuevo = new AccesoSeguimiento();
            nuevo.setSolicitud(solicitud);
            return nuevo;
        });
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LocalDateTime ahora = LocalDateTime.now(clock);
        acceso.setTokenHash(hash(token));
        acceso.setEmitidoEn(ahora);
        acceso.setExpiraEn(ahora.plusDays(30));
        accesos.saveAndFlush(acceso);
        if (eventos.findBySolicitudIdOrderByIdAsc(solicitud.getId()).isEmpty()) {
            registrarEvento(solicitud);
        }
        return token;
    }

    @Transactional
    public void revocar(String codigo) {
        Solicitud solicitud = bloquear(codigo);
        accesos.findBySolicitudId(solicitud.getId()).ifPresent(acceso -> {
            acceso.setTokenHash(null);
            acceso.setExpiraEn(null);
        });
        accesos.flush();
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Optional<ResumenSeguimiento> consultar(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) return Optional.empty();
        return accesos.findByTokenHash(hash(token))
            .filter(a -> a.getExpiraEn() != null && a.getExpiraEn().isAfter(LocalDateTime.now(clock)))
            .map(acceso -> {
                Solicitud solicitud = acceso.getSolicitud();
                List<Paso> historial = eventos.findBySolicitudIdOrderByIdAsc(solicitud.getId()).stream()
                    .map(e -> new Paso(e.getEstado(), e.getFecha())).toList();
                LocalDateTime desde = historial.isEmpty() ? acceso.getEmitidoEn() : historial.getFirst().fecha();
                LocalDateTime actualizado = historial.isEmpty() ? acceso.getEmitidoEn() : historial.getLast().fecha();
                EstadoSolicitud estado = historial.isEmpty() ? solicitud.getEstado() : historial.getLast().estado();
                return new ResumenSeguimiento(solicitud.getCodigo(), estado, actualizado, historial, desde);
            });
    }

    @Transactional
    public void registrarEvento(Solicitud solicitud) {
        eventos.saveAndFlush(new EventoSolicitud(solicitud, solicitud.getEstado(), LocalDateTime.now(clock)));
    }

    @Transactional
    public void eliminarDatos(Solicitud solicitud) {
        accesos.findBySolicitudId(solicitud.getId()).ifPresent(accesos::delete);
        eventos.deleteAll(eventos.findBySolicitudIdOrderByIdAsc(solicitud.getId()));
        eventos.flush();
        accesos.flush();
    }

    private Solicitud bloquear(String codigo) {
        return CodigoRegistro.id(codigo, "SOL").flatMap(solicitudes::findByIdParaActualizar)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("No se pudo preparar el seguimiento", error);
        }
    }
}
