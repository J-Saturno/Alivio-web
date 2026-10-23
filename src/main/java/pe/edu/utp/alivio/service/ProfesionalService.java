package pe.edu.utp.alivio.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import pe.edu.utp.alivio.model.CodigoRegistro;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.repository.ProfesionalRepository;
import pe.edu.utp.alivio.repository.SolicitudRepository;

@Service
@Transactional(readOnly = true)
public class ProfesionalService {
    private final ProfesionalRepository repository;
    private final SolicitudRepository solicitudes;

    public ProfesionalService(ProfesionalRepository repository, SolicitudRepository solicitudes) {
        this.repository = repository;
        this.solicitudes = solicitudes;
    }

    @Transactional
    public Profesional registrar(Profesional profesional) {
        validar(profesional);
        Profesional nueva = new Profesional(profesional);
        nueva.setCodigo(null);
        return new Profesional(repository.saveAndFlush(nueva));
    }

    @Transactional
    public Profesional actualizar(String codigo, Profesional cambios) {
        validar(cambios);
        Profesional actual = bloquear(codigo);
        actual.setNombreCompleto(cambios.getNombreCompleto());
        actual.setTipoProfesional(cambios.getTipoProfesional());
        actual.setTelefono(cambios.getTelefono());
        actual.setZonaCobertura(cambios.getZonaCobertura());
        actual.setEspecialidad(cambios.getEspecialidad());
        actual.setDisponible(cambios.isDisponible());
        repository.flush();
        return new Profesional(actual);
    }

    public List<Profesional> listarTodos() {
        return repository.findAll(Sort.by("id")).stream().map(Profesional::new).toList();
    }

    public List<Profesional> listarDisponibles() {
        return listarTodos().stream().filter(Profesional::isDisponible).toList();
    }

    public Profesional buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo).map(Profesional::new)
            .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrada"));
    }

    @Transactional
    public Profesional cambiarDisponibilidad(String codigo) {
        Profesional profesional = bloquear(codigo);
        profesional.setDisponible(!profesional.isDisponible());
        repository.flush();
        return new Profesional(profesional);
    }

    Profesional bloquear(String codigo) {
        return CodigoRegistro.id(codigo, "PRO").flatMap(repository::findByIdParaActualizar)
            .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrada"));
    }

    @Transactional
    public void eliminar(String codigo) {
        Profesional profesional = bloquear(codigo);
        if (solicitudes.existsByProfesionalAsignadoId(profesional.getId())) {
            throw new IllegalArgumentException("No se puede eliminar una profesional vinculada a solicitudes");
        }
        try {
            repository.delete(profesional);
            repository.flush();
        } catch (DataIntegrityViolationException error) {
            throw new IllegalArgumentException("No se puede eliminar una profesional vinculada a solicitudes");
        }
    }

    private void validar(Profesional profesional) {
        if (profesional == null) throw new IllegalArgumentException("La profesional es obligatoria");
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
        if (profesional.getNombreCompleto().length() > 100) {
            throw new IllegalArgumentException("El nombre debe tener como máximo 100 caracteres.");
        }
        if (profesional.getZonaCobertura().length() > 80) {
            throw new IllegalArgumentException("La zona de cobertura debe tener como máximo 80 caracteres.");
        }
    }
}
