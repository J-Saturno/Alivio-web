package pe.edu.utp.alivio.service;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.model.TurnoAtencion;
import pe.edu.utp.alivio.repository.EventoSolicitudRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(SeguimientoLecturaConcurrenteTest.PausaLecturaConfiguration.class)
class SeguimientoLecturaConcurrenteTest {
    @Autowired SolicitudService solicitudes;
    @Autowired SeguimientoService seguimiento;
    @Autowired PausaLectura pausa;

    @Test
    void cancelacionEntreLecturasNoMezclaEstadoNuevoConHistorialAnterior() throws Exception {
        Solicitud solicitud = solicitudes.crear(new Solicitud(null, "Contacto concurrente", "987654321",
            "Paciente concurrente", 75, TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now().plusDays(1),
            TurnoAtencion.MANANA, "Apoyo", null, null));
        ExecutorService executor = Executors.newSingleThreadExecutor(tarea -> new Thread(tarea, "lector-seguimiento"));
        try {
            String token = seguimiento.emitir(solicitud.getCodigo());
            pausa.armada.set(true);
            Future<SeguimientoService.ResumenSeguimiento> lectura = executor.submit(() ->
                seguimiento.consultar(token).orElseThrow());
            assertTrue(pausa.historialLeido.await(10, TimeUnit.SECONDS),
                "El lector debe detenerse después de consultar el historial real");

            // Esta llamada termina su propia transacción antes de reanudar la lectura.
            solicitudes.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.CANCELADA);
            pausa.continuar.countDown();
            var resumen = lectura.get(10, TimeUnit.SECONDS);
            assertFalse(resumen.historial().isEmpty());
            var ultimo = resumen.historial().getLast();
            assertAll(
                () -> assertEquals(ultimo.estado(), resumen.estado(), "El estado debe corresponder al último evento visible"),
                () -> assertEquals(ultimo.fecha(), resumen.actualizado(), "La actualización debe corresponder al mismo evento"),
                () -> assertEquals(EstadoSolicitud.PENDIENTE, resumen.estado(), "La consulta iniciada antes del cambio conserva su instantánea"));

            var siguiente = seguimiento.consultar(token).orElseThrow();
            assertEquals(EstadoSolicitud.CANCELADA, siguiente.estado());
            assertEquals(EstadoSolicitud.CANCELADA, siguiente.historial().getLast().estado());
        } finally {
            pausa.continuar.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            solicitudes.eliminar(solicitud.getCodigo());
        }
    }

    static class PausaLectura {
        final AtomicBoolean armada = new AtomicBoolean();
        final CountDownLatch historialLeido = new CountDownLatch(1);
        final CountDownLatch continuar = new CountDownLatch(1);

        void despuesDeConsultar() throws InterruptedException {
            if ("lector-seguimiento".equals(Thread.currentThread().getName()) && armada.compareAndSet(true, false)) {
                historialLeido.countDown();
                if (!continuar.await(15, TimeUnit.SECONDS)) {
                    throw new AssertionError("La prueba no liberó la consulta de seguimiento");
                }
            }
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class PausaLecturaConfiguration {
        @Bean
        PausaLectura pausaLectura() {
            return new PausaLectura();
        }

        @Bean
        static BeanPostProcessor pausarRepositorioEventos(ObjectProvider<PausaLectura> pausa) {
            return new BeanPostProcessor() {
                @Override
                public Object postProcessAfterInitialization(Object bean, String beanName) {
                    if (!(bean instanceof EventoSolicitudRepository)) return bean;
                    ProxyFactory proxy = new ProxyFactory(bean);
                    proxy.addAdvice((MethodInterceptor) invocation -> {
                        Object resultado = invocation.proceed();
                        if ("findBySolicitudIdOrderByIdAsc".equals(invocation.getMethod().getName())) {
                            pausa.getObject().despuesDeConsultar();
                        }
                        return resultado;
                    });
                    return proxy.getProxy(bean.getClass().getClassLoader());
                }
            };
        }
    }
}
