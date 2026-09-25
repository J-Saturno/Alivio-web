package pe.edu.utp.alivio.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.repository.SolicitudRepository;

@Service
public class SolicitudService {
    private final SolicitudRepository repository;
    private final ProfesionalService profesionalService;

    public SolicitudService(SolicitudRepository repository, ProfesionalService profesionalService) {
        this.repository = repository;
        this.profesionalService = profesionalService;
    }

    public Solicitud crear(Solicitud solicitud) {
        validar(solicitud);
        Solicitud nueva = new Solicitud(solicitud);
        nueva.setCodigo(null);
        nueva.setEstado(EstadoSolicitud.PENDIENTE);
        nueva.setProfesionalAsignado(null);
        return repository.save(nueva);
    }

    public List<Solicitud> listar(String texto, EstadoSolicitud estado) {
        String filtro = texto == null ? "" : texto.toLowerCase().trim();
        return repository.findAll().stream()
            .filter(s -> estado == null || s.getEstado() == estado)
            .filter(s -> filtro.isBlank()
                || s.getCodigo().toLowerCase().contains(filtro)
                || s.getNombreContacto().toLowerCase().contains(filtro)
                || s.getNombrePaciente().toLowerCase().contains(filtro))
            .toList();
    }

    public Solicitud buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
    }

    public Solicitud asignar(String solicitudCodigo, String profesionalCodigo) {
        Solicitud solicitud = buscarPorCodigo(solicitudCodigo);
        Profesional profesional = profesionalService.buscarPorCodigo(profesionalCodigo);
        if (!profesional.isDisponible()) {
            throw new IllegalArgumentException("Selecciona una profesional disponible");
        }
        if (solicitud.getEstado() != EstadoSolicitud.EN_EVALUACION) {
            throw new IllegalArgumentException("La solicitud debe estar en evaluación antes de asignar");
        }
        solicitud.setProfesionalAsignado(profesional);
        solicitud.setEstado(EstadoSolicitud.ASIGNADA);
        return repository.save(solicitud);
    }

    public Solicitud actualizarEstado(String codigo, EstadoSolicitud nuevoEstado) {
        Solicitud solicitud = buscarPorCodigo(codigo);
        if (nuevoEstado == EstadoSolicitud.ASIGNADA && solicitud.getProfesionalAsignado() == null) {
            throw new IllegalArgumentException("Asigna una profesional antes de continuar");
        }
        if (!transicionPermitida(solicitud.getEstado(), nuevoEstado)) {
            throw new IllegalArgumentException("El cambio de estado solicitado no está permitido");
        }
        solicitud.setEstado(nuevoEstado);
        return repository.save(solicitud);
    }

    private boolean transicionPermitida(EstadoSolicitud actual, EstadoSolicitud siguiente) {
        if (actual == null || siguiente == null) {
            return false;
        }
        return switch (actual) {
            case PENDIENTE -> siguiente == EstadoSolicitud.EN_EVALUACION
                || siguiente == EstadoSolicitud.CANCELADA;
            case EN_EVALUACION -> siguiente == EstadoSolicitud.CANCELADA;
            case ASIGNADA -> siguiente == EstadoSolicitud.EN_ATENCION
                || siguiente == EstadoSolicitud.CANCELADA;
            case EN_ATENCION -> siguiente == EstadoSolicitud.FINALIZADA
                || siguiente == EstadoSolicitud.CANCELADA;
            case FINALIZADA, CANCELADA -> false;
        };
    }

    private void validar(Solicitud solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }
        if (solicitud.getNombreContacto() == null || solicitud.getNombreContacto().isBlank()
                || solicitud.getNombrePaciente() == null || solicitud.getNombrePaciente().isBlank()) {
            throw new IllegalArgumentException("Los nombres son obligatorios");
        }
        if (solicitud.getTelefonoContacto() == null
                || !solicitud.getTelefonoContacto().matches("9\\d{8}")) {
            throw new IllegalArgumentException("El teléfono debe tener nueve dígitos");
        }
        if (solicitud.getTipoServicio() == null || solicitud.getTurno() == null) {
            throw new IllegalArgumentException("Selecciona servicio y turno");
        }
        if (solicitud.getDistrito() == null || solicitud.getDistrito().isBlank()
                || solicitud.getDescripcion() == null || solicitud.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("El distrito y la descripción son obligatorios");
        }
        if (solicitud.getFechaRequerida() == null
                || solicitud.getFechaRequerida().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha requerida no puede ser pasada");
        }
    }
}
