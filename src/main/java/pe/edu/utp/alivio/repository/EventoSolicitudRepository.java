package pe.edu.utp.alivio.repository;

import java.util.List;
import org.springframework.data.jpa.repository.*;
import pe.edu.utp.alivio.model.EventoSolicitud;

public interface EventoSolicitudRepository extends JpaRepository<EventoSolicitud, Long> {
    List<EventoSolicitud> findBySolicitudIdOrderByIdAsc(Long solicitudId);
}
