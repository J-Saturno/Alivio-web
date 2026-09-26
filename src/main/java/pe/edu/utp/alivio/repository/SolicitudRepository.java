package pe.edu.utp.alivio.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Repository;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Solicitud;

@Repository
public class SolicitudRepository {
    private final List<Solicitud> solicitudes = new ArrayList<>();
    private final AtomicInteger secuencia = new AtomicInteger(1);

    public synchronized Solicitud save(Solicitud solicitud) {
        Solicitud copia = new Solicitud(solicitud);
        if (copia.getEstado() == null) {
            copia.setEstado(EstadoSolicitud.PENDIENTE);
        }
        if (copia.getCodigo() == null || copia.getCodigo().isBlank()) {
            copia.setCodigo("SOL-%03d".formatted(secuencia.getAndIncrement()));
            solicitudes.add(copia);
            return new Solicitud(copia);
        }
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).getCodigo().equals(copia.getCodigo())) {
                solicitudes.set(i, copia);
                return new Solicitud(copia);
            }
        }
        throw new java.util.NoSuchElementException("Solicitud no encontrada");
    }

    public synchronized Solicitud update(String codigo, UnaryOperator<Solicitud> cambio) {
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).getCodigo().equals(codigo)) {
                Solicitud actualizada = cambio.apply(new Solicitud(solicitudes.get(i)));
                actualizada.setCodigo(codigo);
                Solicitud almacenada = new Solicitud(actualizada);
                solicitudes.set(i, almacenada);
                return new Solicitud(almacenada);
            }
        }
        throw new IllegalArgumentException("Solicitud no encontrada");
    }

    public synchronized List<Solicitud> findAll() {
        return solicitudes.stream().map(Solicitud::new).toList();
    }

    public synchronized Optional<Solicitud> findByCodigo(String codigo) {
        return solicitudes.stream().filter(s -> s.getCodigo().equals(codigo))
            .findFirst().map(Solicitud::new);
    }
}
