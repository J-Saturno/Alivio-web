package pe.edu.utp.alivio.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.utp.alivio.model.VerificacionProfesional;

public interface VerificacionRepository extends JpaRepository<VerificacionProfesional, Long> {
    List<VerificacionProfesional> findByProfesionalIdOrderByFechaDescIdDesc(Long profesionalId);
    boolean existsByProfesionalId(Long profesionalId);

    @Query("select v from VerificacionProfesional v join fetch v.profesional where not exists "
        + "(select otra.id from VerificacionProfesional otra where otra.profesional = v.profesional "
        + "and (otra.fecha > v.fecha or (otra.fecha = v.fecha and otra.id > v.id)))")
    List<VerificacionProfesional> findUltimas();
}
