package pe.edu.utp.alivio.config;

import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.model.TurnoAtencion;
import pe.edu.utp.alivio.service.ProfesionalService;
import pe.edu.utp.alivio.service.SolicitudService;

@Configuration
public class DatosDemoConfig {
    @Bean
    public CommandLineRunner datosDemo(ProfesionalService profesionales, SolicitudService solicitudes) {
        return args -> {
            if (!profesionales.listarTodos().isEmpty() || !solicitudes.listar("", null).isEmpty()) {
                return;
            }

            profesionales.registrar(new Profesional(null, "Ana Torres", TipoProfesional.LICENCIADA,
                "912345678", "Surco", TipoServicio.ADULTO_MAYOR, true));
            profesionales.registrar(new Profesional(null, "Rosa Medina", TipoProfesional.TECNICA,
                "923456789", "Miraflores", TipoServicio.CURACIONES_POSTOPERATORIO, true));
            profesionales.registrar(new Profesional(null, "Elena Vargas", TipoProfesional.TECNICA,
                "934567890", "San Borja", TipoServicio.ACOMPANAMIENTO_DOMICILIARIO, false));

            solicitudes.crear(new Solicitud(null, "María Salas", "945678901", "Julia Salas", 76,
                TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now().plusDays(1),
                TurnoAtencion.MANANA, "Necesita apoyo y compañía durante la mañana.", null, null));
            solicitudes.crear(new Solicitud(null, "Carlos Ríos", "956789012", "Teresa Ríos", 64,
                TipoServicio.CURACIONES_POSTOPERATORIO, "Miraflores", LocalDate.now().plusDays(2),
                TurnoAtencion.TARDE, "Solicita coordinación para curaciones indicadas.", null, null));
        };
    }
}
