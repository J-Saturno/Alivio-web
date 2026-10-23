package pe.edu.utp.alivio.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import pe.edu.utp.alivio.model.CodigoRegistro;
import pe.edu.utp.alivio.model.Profesional;

public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {
    default Optional<Profesional> findByCodigo(String codigo) {
        return CodigoRegistro.id(codigo, "PRO").flatMap(this::findById);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Profesional e where e.id = :id")
    Optional<Profesional> findByIdParaActualizar(Long id);
}
