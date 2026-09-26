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
        Profesional copia = new Profesional(profesional);
        if (copia.getCodigo() == null || copia.getCodigo().isBlank()) {
            copia.setCodigo("PRO-%03d".formatted(secuencia.getAndIncrement()));
            profesionales.add(copia);
            return new Profesional(copia);
        }
        for (int i = 0; i < profesionales.size(); i++) {
            if (profesionales.get(i).getCodigo().equals(copia.getCodigo())) {
                profesionales.set(i, copia);
                return new Profesional(copia);
            }
        }
        throw new java.util.NoSuchElementException("Profesional no encontrada");
    }

    public synchronized List<Profesional> findAll() {
        return profesionales.stream().map(Profesional::new).toList();
    }

    public synchronized Optional<Profesional> findByCodigo(String codigo) {
        return profesionales.stream().filter(p -> p.getCodigo().equals(codigo))
            .findFirst().map(Profesional::new);
    }
}
