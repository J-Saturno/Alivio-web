package pe.edu.utp.alivio.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.alivio.model.PostulacionProfesional;

public interface PostulacionRepository extends JpaRepository<PostulacionProfesional, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PostulacionProfesional p where p.id = :id")
    Optional<PostulacionProfesional> findByIdParaActualizar(@Param("id") Long id);
}
