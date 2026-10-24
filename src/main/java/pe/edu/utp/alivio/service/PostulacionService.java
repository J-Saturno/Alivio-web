package pe.edu.utp.alivio.service;

import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.utp.alivio.model.EstadoPostulacion;
import pe.edu.utp.alivio.model.PostulacionProfesional;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.repository.PostulacionRepository;
import pe.edu.utp.alivio.repository.ProfesionalRepository;

@Service
@Transactional(readOnly = true)
public class PostulacionService {
    private static final long MAX_CV_BYTES = 5L * 1024 * 1024;
    private final PostulacionRepository postulaciones;
    private final ProfesionalRepository profesionales;

    public PostulacionService(PostulacionRepository postulaciones, ProfesionalRepository profesionales) {
        this.postulaciones = postulaciones;
        this.profesionales = profesionales;
    }

    @Transactional
    public PostulacionProfesional registrar(PostulacionProfesional postulacion, MultipartFile cv) {
        if (cv == null || cv.isEmpty() || cv.getSize() > MAX_CV_BYTES
                || cv.getOriginalFilename() == null
                || !cv.getOriginalFilename().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")
                || !"application/pdf".equals(cv.getContentType())) {
            throw new IllegalArgumentException("Adjunta un CV PDF de hasta 5 MB");
        }
        try {
            byte[] contenido = cv.getBytes();
            if (contenido.length > MAX_CV_BYTES || contenido.length < 5
                    || contenido[0] != '%' || contenido[1] != 'P' || contenido[2] != 'D'
                    || contenido[3] != 'F' || contenido[4] != '-') {
                throw new IllegalArgumentException("El CV debe ser un archivo PDF válido de hasta 5 MB");
            }
            postulacion.setCvPdf(contenido);
            postulacion.setEstado(EstadoPostulacion.PENDIENTE);
            return postulaciones.saveAndFlush(postulacion);
        } catch (IOException error) {
            throw new IllegalArgumentException("No pudimos leer el CV. Inténtalo de nuevo", error);
        }
    }

    public List<PostulacionProfesional> listar() {
        return postulaciones.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public PostulacionProfesional buscar(Long id) {
        return postulaciones.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Postulación no encontrada"));
    }

    @Transactional
    public void aceptar(Long id, String nota) {
        PostulacionProfesional postulacion = pendiente(id);
        postulacion.setNotaRevision(validarNota(nota));
        Profesional profesional = new Profesional(null, postulacion.getNombreCompleto(),
            postulacion.getTipoProfesional(), postulacion.getTelefono(),
            postulacion.getZonaCobertura(), postulacion.getEspecialidad(), false);
        Profesional creada = profesionales.saveAndFlush(profesional);
        postulacion.setProfesionalCreadaId(creada.getId());
        postulacion.setEstado(EstadoPostulacion.ACEPTADA);
        postulaciones.flush();
    }

    @Transactional
    public void rechazar(Long id, String nota) {
        PostulacionProfesional postulacion = pendiente(id);
        postulacion.setNotaRevision(validarNota(nota));
        postulacion.setEstado(EstadoPostulacion.RECHAZADA);
        postulaciones.flush();
    }

    private PostulacionProfesional pendiente(Long id) {
        PostulacionProfesional postulacion = postulaciones.findByIdParaActualizar(id)
            .orElseThrow(() -> new IllegalArgumentException("Postulación no encontrada"));
        if (postulacion.getEstado() != EstadoPostulacion.PENDIENTE) {
            throw new IllegalArgumentException("Esta postulación ya fue revisada");
        }
        return postulacion;
    }

    private String validarNota(String nota) {
        if (nota == null || nota.isBlank() || nota.length() > 500) {
            throw new IllegalArgumentException("Registra una nota de revisión de hasta 500 caracteres");
        }
        return nota.trim();
    }
}
