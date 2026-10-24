package pe.edu.utp.alivio.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeguimientoConfig {
    @Bean
    Clock seguimientoClock() { return Clock.systemDefaultZone(); }
}
