package pe.edu.utp.alivio.service;

import java.time.LocalDate;
import pe.edu.utp.alivio.validacion.Textos;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import pe.edu.utp.alivio.model.CodigoRegistro;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.repository.SolicitudRepository;

@Service
@Transactional(readOnly = true)
public class SolicitudService {
    private final SolicitudRepository repository;
    private final ProfesionalService profesionalService;
    private final CompatibilidadAsignacionService compatibilidad;
    private final SeguimientoService seguimiento;

    public SolicitudService(SolicitudRepository repository, ProfesionalService profesionalService,
            CompatibilidadAsignacionService compatibilidad, SeguimientoService seguimiento) {
        this.repository = repository;
        this.profesionalService = profesionalService;
        this.compatibilidad = compatibilidad;
        this.seguimiento = seguimiento;
    }

    @Transactional
    public Solicitud crear(Solicitud solicitud) {
        validar(solicitud);
        Solicitud nueva = new Solicitud(solicitud);
        nueva.setCodigo(null);
        nueva.setEstado(EstadoSolicitud.PENDIENTE);
        nueva.setProfesionalAsignado(null);
        Solicitud guardada = repository.saveAndFlush(nueva);
        seguimiento.registrarEvento(guardada);
        return new Solicitud(guardada);
    }

    public record RegistroSolicitud(Solicitud solicitud, String token) {}

    @Transactional
    public RegistroSolicitud crearConSeguimiento(Solicitud solicitud) {
        Solicitud creada = crear(solicitud);
        return new RegistroSolicitud(creada, seguimiento.emitir(creada.getCodigo()));
    }

    public List<Solicitud> listar(String texto, EstadoSolicitud estado) {
        String filtro = texto == null ? "" : texto.toLowerCase().trim();
        return repository.findAll(Sort.sort(Solicitud.class).by(Solicitud::getId).ascending()).stream()
            .filter(s -> estado == null || s.getEstado() == estado)
            .filter(s -> filtro.isBlank()
                || s.getCodigo().toLowerCase().contains(filtro)
                || s.getNombreContacto().toLowerCase().contains(filtro)
                || s.getNombrePaciente().toLowerCase().contains(filtro))
            .map(Solicitud::new).toList();
    }

    public Solicitud buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo).map(Solicitud::new)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
    }

    @Transactional
    public Solicitud asignar(String solicitudCodigo, String profesionalCodigo) {
        Solicitud solicitud = bloquear(solicitudCodigo);
        Profesional profesional = profesionalService.bloquear(profesionalCodigo);
        if (!profesional.isDisponible()) {
            throw new IllegalArgumentException("Selecciona una profesional disponible");
        }
        if (solicitud.getEstado() != EstadoSolicitud.EN_EVALUACION) {
            throw new IllegalArgumentException("La solicitud debe estar en evaluación antes de asignar");
        }
        String motivo = compatibilidad.motivoIncompatibilidad(solicitud, profesional);
        if (motivo != null) throw new IllegalArgumentException(motivo);
        solicitud.setProfesionalAsignado(profesional);
        solicitud.setEstado(EstadoSolicitud.ASIGNADA);
        repository.flush();
        seguimiento.registrarEvento(solicitud);
        return new Solicitud(solicitud);
    }

    public List<Profesional> candidatasCompatibles(String codigo) {
        return compatibilidad.candidatas(buscarPorCodigo(codigo));
    }

    @Transactional
    public Solicitud actualizarEstado(String codigo, EstadoSolicitud nuevoEstado) {
        Solicitud solicitud = bloquear(codigo);
        if (nuevoEstado == EstadoSolicitud.ASIGNADA && solicitud.getProfesionalAsignado() == null) {
            throw new IllegalArgumentException("Asigna una profesional antes de continuar");
        }
        if (!transicionPermitida(solicitud.getEstado(), nuevoEstado)) {
            throw new IllegalArgumentException("El cambio de estado solicitado no está permitido");
        }
        solicitud.setEstado(nuevoEstado);
        repository.flush();
        seguimiento.registrarEvento(solicitud);
        return new Solicitud(solicitud);
    }

    private Solicitud bloquear(String codigo) {
        return CodigoRegistro.id(codigo, "SOL").flatMap(repository::findByIdParaActualizar)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
    }

    @Transactional
    public Solicitud actualizar(String codigo, Solicitud cambios) {
        Solicitud actual = bloquear(codigo);
        if (actual.getEstado() != EstadoSolicitud.PENDIENTE && actual.getEstado() != EstadoSolicitud.EN_EVALUACION) {
            throw new IllegalArgumentException("Solo se pueden editar solicitudes pendientes o en evaluación");
        }
        validar(cambios);
        actual.setNombreContacto(cambios.getNombreContacto());
        actual.setTelefonoContacto(cambios.getTelefonoContacto());
        actual.setNombrePaciente(cambios.getNombrePaciente());
        actual.setEdadPaciente(cambios.getEdadPaciente());
        actual.setTipoServicio(cambios.getTipoServicio());
        actual.setDistrito(cambios.getDistrito());
        actual.setFechaRequerida(cambios.getFechaRequerida());
        actual.setTurno(cambios.getTurno());
        actual.setDescripcion(cambios.getDescripcion());
        repository.flush();
        return new Solicitud(actual);
    }

    @Transactional
    public void eliminar(String codigo) {
        Solicitud actual = bloquear(codigo);
        if (actual.getEstado() != EstadoSolicitud.PENDIENTE && actual.getEstado() != EstadoSolicitud.CANCELADA) {
            throw new IllegalArgumentException("Solo se pueden eliminar solicitudes pendientes o canceladas");
        }
        seguimiento.eliminarDatos(actual);
        repository.delete(actual);
        repository.flush();
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
        solicitud.setNombreContacto(Textos.linea(solicitud.getNombreContacto()));
        solicitud.setNombrePaciente(Textos.linea(solicitud.getNombrePaciente()));
        solicitud.setTelefonoContacto(Textos.telefono(solicitud.getTelefonoContacto()));
        solicitud.setDistrito(Textos.linea(solicitud.getDistrito()));
        solicitud.setDescripcion(Textos.parrafos(solicitud.getDescripcion()));
        Map<String, String> errores = new LinkedHashMap<>();
        if (solicitud.getNombreContacto() == null || solicitud.getNombreContacto().isBlank()) {
            errores.put("nombreContacto", "Indica el nombre de contacto.");
        } else if (solicitud.getNombreContacto().length() > 100) {
            errores.put("nombreContacto", "El nombre de contacto debe tener como máximo 100 caracteres.");
        } else if (!Textos.nombreValido(solicitud.getNombreContacto())) {
            errores.put("nombreContacto", "Ingresa un nombre con letras, espacios, apóstrofes o guiones.");
        }
        if (solicitud.getNombrePaciente() == null || solicitud.getNombrePaciente().isBlank()) {
            errores.put("nombrePaciente", "Indica el nombre del paciente.");
        } else if (solicitud.getNombrePaciente().length() > 100) {
            errores.put("nombrePaciente", "El nombre del paciente debe tener como máximo 100 caracteres.");
        } else if (!Textos.nombreValido(solicitud.getNombrePaciente())) {
            errores.put("nombrePaciente", "Ingresa un nombre con letras, espacios, apóstrofes o guiones.");
        }
        if (solicitud.getTelefonoContacto() == null
                || !solicitud.getTelefonoContacto().matches("9\\d{8}")) {
            errores.put("telefonoContacto", "Ingresa un celular de nueve dígitos que empiece con 9.");
        }
        if (solicitud.getEdadPaciente() != null
                && (solicitud.getEdadPaciente() < 0 || solicitud.getEdadPaciente() > 120)) {
            errores.put("edadPaciente", "Ingresa una edad entre 0 y 120.");
        }
        if (solicitud.getTipoServicio() == null) {
            errores.put("tipoServicio", "Selecciona uno de los servicios disponibles.");
        }
        if (solicitud.getDistrito() == null || solicitud.getDistrito().isBlank()) {
            errores.put("distrito", "Indica el distrito de atención.");
        } else if (solicitud.getDistrito().length() > 80) {
            errores.put("distrito", "El distrito debe tener como máximo 80 caracteres.");
        } else if (!Textos.distritoValido(solicitud.getDistrito())) {
            errores.put("distrito", "Ingresa el nombre de un distrito, por ejemplo La Molina.");
        }
        if (solicitud.getFechaRequerida() == null
                || solicitud.getFechaRequerida().isBefore(LocalDate.now())) {
            errores.put("fechaRequerida", "Selecciona hoy o una fecha posterior.");
        }
        if (solicitud.getTurno() == null) {
            errores.put("turno", "Selecciona un turno válido.");
        }
        if (solicitud.getDescripcion() == null || solicitud.getDescripcion().isBlank()) {
            errores.put("descripcion", "Describe brevemente la necesidad de atención.");
        } else if (solicitud.getDescripcion().length() > 500) {
            errores.put("descripcion", "La descripción debe tener como máximo 500 caracteres.");
        }
        if (!errores.isEmpty()) {
            throw new SolicitudValidationException(errores);
        }
    }
}
