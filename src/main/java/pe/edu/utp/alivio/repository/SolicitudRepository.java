package pe.edu.utp.alivio.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Repository;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Solicitud;

@Repository
public class SolicitudRepository {
    private final List<Solicitud> solicitudes = new ArrayList<>();
    private final AtomicInteger secuencia = new AtomicInteger(1);

    public synchronized Solicitud save(Solicitud solicitud) {
        if (solicitud.getEstado() == null) {
            solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        }
        if (solicitud.getCodigo() == null || solicitud.getCodigo().isBlank()) {
            solicitud.setCodigo("SOL-%03d".formatted(secuencia.getAndIncrement()));
            solicitudes.add(solicitud);
            return solicitud;
        }
        Solicitud existente = findByCodigo(solicitud.getCodigo()).orElseThrow();
        solicitudes.set(solicitudes.indexOf(existente), solicitud);
        return solicitud;
    }

    public List<Solicitud> findAll() {
        return new ArrayList<>(solicitudes);
    }

    public Optional<Solicitud> findByCodigo(String codigo) {
        return solicitudes.stream().filter(s -> s.getCodigo().equals(codigo)).findFirst();
    }
}
