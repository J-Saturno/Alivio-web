package pe.edu.utp.alivio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

@Configuration
public class AdminSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            @Value("${alivio.admin.security.enabled:true}") boolean enabled) throws Exception {
        if (!enabled) {
            return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable()).build();
        }
        var csrfRequestHandler = new XorCsrfTokenRequestAttributeHandler();
        csrfRequestHandler.setCsrfRequestAttributeName(null);
        return http.csrf(csrf -> csrf.csrfTokenRequestHandler(csrfRequestHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/ingresar").permitAll()
                .requestMatchers("/admin/**").authenticated()
                .anyRequest().permitAll())
            .formLogin(form -> form.loginPage("/admin/ingresar")
                .loginProcessingUrl("/admin/ingresar")
                .defaultSuccessUrl("/admin/solicitudes", true))
            .logout(logout -> logout.logoutUrl("/admin/salir")
                .logoutSuccessUrl("/admin/ingresar?salida"))
            .build();
    }

    @Bean
    UserDetailsService adminUsers(@Value("${alivio.admin.security.enabled:true}") boolean enabled,
            @Value("${alivio.admin.username:}") String username,
            @Value("${alivio.admin.password:}") String password) {
        if (!enabled) return new InMemoryUserDetailsManager();
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Configura alivio.admin.username y alivio.admin.password en application-local.properties, o ALIVIO_ADMIN_USERNAME y ALIVIO_ADMIN_PASSWORD en el entorno, para iniciar Alivio");
        }
        var encoder = new BCryptPasswordEncoder();
        return new InMemoryUserDetailsManager(User.withUsername(username)
            .password(encoder.encode(password)).roles("ADMIN").build());
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
