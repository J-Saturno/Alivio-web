package pe.edu.utp.alivio.service;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.repository.ProfesionalRepository;

@Service
public class ProfesionalService {
    private final ProfesionalRepository repository;

    public ProfesionalService(ProfesionalRepository repository) {
        this.repository = repository;
    }

    public Profesional registrar(Profesional profesional) {
        validar(profesional);
        profesional.setCodigo(null);
        return repository.save(profesional);
    }

    public Profesional actualizar(String codigo, Profesional cambios) {
        validar(cambios);
        cambios.setCodigo(codigo);
        return repository.save(cambios);
    }

    public List<Profesional> listarTodos() {
        return repository.findAll();
    }

    public List<Profesional> listarDisponibles() {
        return repository.findAll().stream().filter(Profesional::isDisponible).toList();
    }

    public Profesional buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo)
            .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrada"));
    }

    public Profesional cambiarDisponibilidad(String codigo) {
        Profesional profesional = buscarPorCodigo(codigo);
        profesional.setDisponible(!profesional.isDisponible());
        return repository.save(profesional);
    }

    private void validar(Profesional profesional) {
        if (profesional.getNombreCompleto() == null || profesional.getNombreCompleto().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (profesional.getTelefono() == null || !profesional.getTelefono().matches("9\\d{8}")) {
            throw new IllegalArgumentException("El teléfono debe tener nueve dígitos");
        }
        if (profesional.getTipoProfesional() == null || profesional.getEspecialidad() == null) {
            throw new IllegalArgumentException("Selecciona tipo y especialidad");
        }
        if (profesional.getZonaCobertura() == null || profesional.getZonaCobertura().isBlank()) {
            throw new IllegalArgumentException("La zona de cobertura es obligatoria");
        }
    }
}
