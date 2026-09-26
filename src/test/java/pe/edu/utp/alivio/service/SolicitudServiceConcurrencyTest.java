package pe.edu.utp.alivio.service;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.RepeatedTest;
import pe.edu.utp.alivio.model.EstadoSolicitud;
import pe.edu.utp.alivio.model.Profesional;
import pe.edu.utp.alivio.model.Solicitud;
import pe.edu.utp.alivio.model.TipoProfesional;
import pe.edu.utp.alivio.model.TipoServicio;
import pe.edu.utp.alivio.model.TurnoAtencion;
import pe.edu.utp.alivio.repository.ProfesionalRepository;
import pe.edu.utp.alivio.repository.SolicitudRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class SolicitudServiceConcurrencyTest {
    private static final int TIMEOUT_SEGUNDOS = 20;

    @RepeatedTest(25)
    void cancelacionConcurrenteNoPuedeSerSobrescritaPorEvaluacion() throws Exception {
        verificarCancelacionConcurrente(false);
    }

    @RepeatedTest(25)
    void cancelacionConcurrenteNoPuedeSerSobrescritaPorAsignacion() throws Exception {
        verificarCancelacionConcurrente(true);
    }

    private void verificarCancelacionConcurrente(boolean asignar) throws Exception {
        RepositorioConBloqueoAntesDeUpdate repository = new RepositorioConBloqueoAntesDeUpdate();
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
        FutureTask<Throwable> competidora = new FutureTask<>(() -> ejecutar(() -> {
            if (asignar) {
                service.asignar(solicitud.getCodigo(), profesional.getCodigo());
            } else {
                service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_EVALUACION);
            }
        }));
        Thread hiloCompetidor = new Thread(competidora, "competidora");
        hiloCompetidor.setDaemon(true);
        hiloCompetidor.start();
        try {
            repository.esperarIntentoCompetidor();
            FutureTask<Throwable> cancelacion = new FutureTask<>(() -> ejecutar(() ->
                service.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.CANCELADA)));
            Thread hiloCancelacion = new Thread(cancelacion, "cancelacion");
            hiloCancelacion.setDaemon(true);
            hiloCancelacion.start();
            assertNull(cancelacion.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS));
            assertEquals(EstadoSolicitud.CANCELADA,
                service.buscarPorCodigo(solicitud.getCodigo()).getEstado());
            repository.liberarCompetidor();
            assertInstanceOf(IllegalArgumentException.class,
                competidora.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS));
        } finally {
            repository.liberarCompetidor();
            hiloCompetidor.interrupt();
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

    private static class RepositorioConBloqueoAntesDeUpdate extends SolicitudRepository {
        private final CountDownLatch intentoCompetidor = new CountDownLatch(1);
        private final CountDownLatch liberarCompetidor = new CountDownLatch(1);

        @Override
        public Solicitud update(String codigo, UnaryOperator<Solicitud> cambio) {
            if (Thread.currentThread().getName().equals("competidora")) {
                intentoCompetidor.countDown();
                esperar(liberarCompetidor);
            }
            return super.update(codigo, cambio);
        }

        void esperarIntentoCompetidor() {
            esperar(intentoCompetidor);
        }

        void liberarCompetidor() {
            liberarCompetidor.countDown();
        }

        private void esperar(CountDownLatch latch) {
            try {
                if (!latch.await(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)) {
                    throw new AssertionError("No se alcanzó la actualización competidora");
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new AssertionError(error);
            }
        }
    }
}
