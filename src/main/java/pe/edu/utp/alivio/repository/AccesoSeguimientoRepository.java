package pe.edu.utp.alivio.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import pe.edu.utp.alivio.model.AccesoSeguimiento;

public interface AccesoSeguimientoRepository extends JpaRepository<AccesoSeguimiento, Long> {
    Optional<AccesoSeguimiento> findBySolicitudId(Long solicitudId);
    Optional<AccesoSeguimiento> findByTokenHash(String tokenHash);
}
