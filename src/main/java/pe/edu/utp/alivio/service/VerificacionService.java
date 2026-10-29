package pe.edu.utp.alivio.service;

import pe.edu.utp.alivio.validacion.Textos;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.alivio.model.*;
import pe.edu.utp.alivio.repository.VerificacionRepository;
import pe.edu.utp.alivio.web.VerificacionForm;

@Service
@Transactional(readOnly = true)
public class VerificacionService {
    private final VerificacionRepository repository;
    private final ProfesionalService profesionales;

    public VerificacionService(VerificacionRepository repository, ProfesionalService profesionales) {
        this.repository = repository;
        this.profesionales = profesionales;
    }

    public List<VerificacionProfesional> historial(String codigo) {
        return repository.findByProfesionalIdOrderByFechaDescIdDesc(profesionales.buscarPorCodigo(codigo).getId());
    }

    public Map<String, String> estadosActuales() {
        Map<String, String> estados = new HashMap<>();
        for (var revision : repository.findUltimas()) {
            estados.put(revision.getProfesional().getCodigo(), revision.getResultado().name());
        }
        return estados;
    }

    @Transactional
    public VerificacionProfesional registrar(String codigo, VerificacionForm form, String revisor) {
        validar(form, revisor);
        Profesional profesional = profesionales.bloquear(codigo);
        return repository.saveAndFlush(new VerificacionProfesional(profesional,
            form.isIdentidadComprobada(), form.isCredencialComprobada(), form.isReferenciasComprobadas(),
            form.getAniosExperiencia(), limpiar(form.getReferenciaCredencial()), limpiar(form.getNotaEvidencia()),
            form.getResultado(), revisor.trim(), LocalDateTime.now()));
    }

    private void validar(VerificacionForm form, String revisor) {
        if (revisor == null || revisor.isBlank() || revisor.trim().length() > 100) {
            throw new IllegalArgumentException("Se requiere un usuario autenticado válido para registrar la revisión");
        }
        if (form == null || form.getResultado() == null) {
            throw new IllegalArgumentException("Selecciona un resultado válido");
        }
        form.setReferenciaCredencial(Textos.linea(form.getReferenciaCredencial()));
        form.setNotaEvidencia(Textos.parrafos(form.getNotaEvidencia()));
        if (form.getAniosExperiencia() != null && (form.getAniosExperiencia() < 0 || form.getAniosExperiencia() > 80)) {
            throw new IllegalArgumentException("La experiencia debe estar entre 0 y 80 años");
        }
        if (form.getReferenciaCredencial() != null && form.getReferenciaCredencial().length() > 100) {
            throw new IllegalArgumentException("La referencia debe tener como máximo 100 caracteres");
        }
        if (form.getNotaEvidencia() != null && form.getNotaEvidencia().length() > 1000) {
            throw new IllegalArgumentException("La evidencia debe tener como máximo 1000 caracteres");
        }
        if (form.getResultado() == ResultadoVerificacion.APROBADA &&
            (!form.isIdentidadComprobada() || !form.isCredencialComprobada() || !form.isReferenciasComprobadas()
                || form.getNotaEvidencia() == null || form.getNotaEvidencia().isBlank())) {
            throw new IllegalArgumentException("Para aprobar confirma identidad, credencial y referencias y describe la evidencia revisada");
        }
    }
    private String limpiar(String valor) { return valor == null ? null : valor.trim(); }
}
