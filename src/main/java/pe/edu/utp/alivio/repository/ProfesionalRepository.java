package pe.edu.utp.alivio.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Repository;
import pe.edu.utp.alivio.model.Profesional;

@Repository
public class ProfesionalRepository {
    private final List<Profesional> profesionales = new ArrayList<>();
    private final AtomicInteger secuencia = new AtomicInteger(1);

    public synchronized Profesional save(Profesional profesional) {
        if (profesional.getCodigo() == null || profesional.getCodigo().isBlank()) {
            profesional.setCodigo("PRO-%03d".formatted(secuencia.getAndIncrement()));
            profesionales.add(profesional);
            return profesional;
        }
        Profesional existente = findByCodigo(profesional.getCodigo()).orElseThrow();
        profesionales.set(profesionales.indexOf(existente), profesional);
        return profesional;
    }

    public List<Profesional> findAll() {
        return new ArrayList<>(profesionales);
    }

    public Optional<Profesional> findByCodigo(String codigo) {
        return profesionales.stream().filter(p -> p.getCodigo().equals(codigo)).findFirst();
    }
}
