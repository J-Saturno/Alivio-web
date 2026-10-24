package pe.edu.utp.alivio.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.alivio.model.CodigoRegistro;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Solicitud;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    default Optional<Solicitud> findByCodigo(String codigo) {
        return CodigoRegistro.id(codigo, "SOL").flatMap(this::findById);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Solicitud e where e.id = :id")
    Optional<Solicitud> findByIdParaActualizar(Long id);

    boolean existsByProfesionalAsignadoId(Long profesionalId);

    @Query("select s from Solicitud s where s.profesionalAsignado.id = :profesionalId and s.fechaRequerida = :fecha and s.estado in :estados")
    List<Solicitud> findReservasActivas(@Param("profesionalId") Long profesionalId,
        @Param("fecha") LocalDate fecha, @Param("estados") List<EstadoSolicitud> estados);
}
