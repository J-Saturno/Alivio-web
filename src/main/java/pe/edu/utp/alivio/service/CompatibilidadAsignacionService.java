package pe.edu.utp.alivio.service;

import java.text.Normalizer;
import pe.edu.utp.alivio.validacion.Textos;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.model.TurnoAtencion;
import pe.edu.utp.alivio.repository.SolicitudRepository;

@Service
@Transactional(readOnly = true)
public class CompatibilidadAsignacionService {
    private final SolicitudRepository solicitudes;
    private final ProfesionalService profesionales;

    public CompatibilidadAsignacionService(SolicitudRepository solicitudes, ProfesionalService profesionales) {
        this.solicitudes = solicitudes;
        this.profesionales = profesionales;
    }

    public List<Profesional> candidatas(Solicitud solicitud) {
        if (solicitud.getEstado() != EstadoSolicitud.EN_EVALUACION) return List.of();
        return profesionales.listarDisponibles().stream()
            .filter(p -> motivoIncompatibilidad(solicitud, p) == null).toList();
    }

    public String motivoIncompatibilidad(Solicitud solicitud, Profesional profesional) {
        if (!profesional.isDisponible()) return "La profesional no está disponible";
        if (profesional.getEspecialidad() != solicitud.getTipoServicio()) {
            return "La especialidad no coincide con el servicio solicitado";
        }
        String distrito = normalizar(solicitud.getDistrito());
        boolean cubreDistrito = List.of(Textos.cobertura(profesional.getZonaCobertura()).split(","))
            .stream().map(this::normalizar).anyMatch(distrito::equals);
        if (!cubreDistrito) return "La profesional no cubre el distrito solicitado";
        boolean ocupada = solicitudes.findReservasActivas(profesional.getId(), solicitud.getFechaRequerida(),
                List.of(EstadoSolicitud.ASIGNADA, EstadoSolicitud.EN_ATENCION))
            .stream().anyMatch(reserva -> seSuperponen(reserva.getTurno(), solicitud.getTurno()));
        if (ocupada) return "La profesional ya tiene una atención en ese horario";
        return null;
    }

    private String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(Textos.linea(texto).toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return sinTildes.replaceAll("\\p{M}+", "").replaceAll("\\s+", " ");
    }

    private boolean seSuperponen(TurnoAtencion primero, TurnoAtencion segundo) {
        return primero == segundo || primero == TurnoAtencion.DOCE_HORAS
            || segundo == TurnoAtencion.DOCE_HORAS
            || primero == TurnoAtencion.VEINTICUATRO_HORAS
            || segundo == TurnoAtencion.VEINTICUATRO_HORAS;
    }
}
