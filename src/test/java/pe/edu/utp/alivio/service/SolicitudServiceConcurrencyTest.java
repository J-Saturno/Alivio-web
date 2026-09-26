package pe.edu.utp.alivio.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.model.TurnoAtencion;
import pe.edu.utp.alivio.repository.ProfesionalRepository;
import pe.edu.utp.alivio.repository.SolicitudRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitudServiceConcurrencyTest {
    @Test
    void cancelacionConcurrenteNoPuedeSerSobrescritaPorEvaluacion() throws Exception {
        verificarCancelacionConcurrente(false);
    }

    @Test
    void cancelacionConcurrenteNoPuedeSerSobrescritaPorAsignacion() throws Exception {
        verificarCancelacionConcurrente(true);
    }

    private void verificarCancelacionConcurrente(boolean asignar) throws Exception {
        RepositorioConCarreraForzada repository = new RepositorioConCarreraForzada();
        ProfesionalService profesionales = new ProfesionalService(new ProfesionalRepository());
        SolicitudService service = new SolicitudService(repository, profesionales);
        Profesional profesional = profesionales.registrar(new Profesional(null, "Ana",
            TipoProfesional.TECNICA, "987654321", "Surco", TipoServicio.ADULTO_MAYOR, true));
        Solicitud solicitud = service.crear(new Solicitud(null, "Rosa", "987654321", "María", 80,
            TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now(), TurnoAtencion.MANANA,
            "Apoyo", null, null));
        if (asignar) {
            service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_EVALUACION);
        }
        repository.armar();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Throwable> cancelacion = pool.submit(() -> ejecutar(() ->
                service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.CANCELADA)));
            Future<Throwable> competidora = pool.submit(() -> ejecutar(() -> {
                if (asignar) {
                    service.asignar(solicitud.getCodigo(), profesional.getCodigo());
                } else {
                    service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_EVALUACION);
                }
            }));

            assertNull(cancelacion.get(10, TimeUnit.SECONDS));
            Throwable errorCompetidora = competidora.get(10, TimeUnit.SECONDS);
            assertTrue(errorCompetidora == null || errorCompetidora instanceof IllegalArgumentException);
        } finally {
            repository.desarmar();
            pool.shutdownNow();
        }
        assertEquals(EstadoSolicitud.CANCELADA,
            service.buscarPorCodigo(solicitud.getCodigo()).getEstado());
        assertNull(service.buscarPorCodigo(solicitud.getCodigo()).getProfesionalAsignado());
    }

    private Throwable ejecutar(Runnable accion) {
        try {
            accion.run();
            return null;
        } catch (RuntimeException error) {
            return error;
        }
    }

    private static class RepositorioConCarreraForzada extends SolicitudRepository {
        private final CountDownLatch ambasLecturas = new CountDownLatch(2);
        private final CountDownLatch canceladaGuardada = new CountDownLatch(1);
        private final AtomicInteger lecturas = new AtomicInteger();
        private volatile boolean armado;

        void armar() { armado = true; }
        void desarmar() { armado = false; }

        @Override
        public Optional<Solicitud> findByCodigo(String codigo) {
            Optional<Solicitud> resultado = super.findByCodigo(codigo);
            if (armado && lecturas.getAndIncrement() < 2) {
                ambasLecturas.countDown();
                esperar(ambasLecturas);
            }
            return resultado;
        }

        @Override
        public Solicitud save(Solicitud solicitud) {
            if (armado && (solicitud.getEstado() == EstadoSolicitud.EN_EVALUACION
                    || solicitud.getEstado() == EstadoSolicitud.ASIGNADA)) {
                esperar(canceladaGuardada);
            }
            Solicitud guardada = super.save(solicitud);
            if (armado && solicitud.getEstado() == EstadoSolicitud.CANCELADA) {
                canceladaGuardada.countDown();
            }
            return guardada;
        }

        private void esperar(CountDownLatch latch) {
            try {
                if (!latch.await(5, TimeUnit.SECONDS)) {
                    throw new AssertionError("No se alcanzó la intercalación esperada");
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new AssertionError(error);
            }
        }
    }
}
