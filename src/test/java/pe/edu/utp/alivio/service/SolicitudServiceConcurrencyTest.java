package pe.edu.utp.alivio.service;

import java.time.LocalDate;
import java.util.concurrent.*;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pe.edu.utp.alivio.model.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SolicitudServiceConcurrencyTest {
    @Autowired SolicitudService solicitudes;
    @Autowired ProfesionalService profesionales;
    @Autowired PlatformTransactionManager transactionManager;

    @RepeatedTest(3)
    void dosCoordinadoresNoAsignanLaMismaProfesionalAlMismoTurno() throws Exception {
        Profesional profesional = profesionales.registrar(new Profesional(null, "Ana",
            TipoProfesional.TECNICA, "987654321", "Surco", TipoServicio.ADULTO_MAYOR, true));
        Solicitud primera = solicitudes.crear(new Solicitud(null, "Rosa", "987654321", "María", 80,
            TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now(), TurnoAtencion.MANANA,
            "Apoyo", null, null));
        Solicitud segunda = solicitudes.crear(new Solicitud(null, "Lucía", "987654321", "Elena", 75,
            TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now(), TurnoAtencion.MANANA,
            "Apoyo", null, null));
        solicitudes.actualizarEstado(primera.getCodigo(), EstadoSolicitud.EN_EVALUACION);
        solicitudes.actualizarEstado(segunda.getCodigo(), EstadoSolicitud.EN_EVALUACION);
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> a = executor.submit(() -> intentarAsignar(inicio, primera, profesional));
            Future<Boolean> b = executor.submit(() -> intentarAsignar(inicio, segunda, profesional));
            inicio.countDown();
            assertEquals(1, (a.get(10, TimeUnit.SECONDS) ? 1 : 0)
                + (b.get(10, TimeUnit.SECONDS) ? 1 : 0));
            long asignadas = java.util.stream.Stream.of(primera, segunda)
                .map(s -> solicitudes.buscarPorCodigo(s.getCodigo()))
                .filter(s -> s.getEstado() == EstadoSolicitud.ASIGNADA).count();
            assertEquals(1, asignadas);
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            solicitudes.actualizarEstado(primera.getCodigo(), EstadoSolicitud.CANCELADA);
            solicitudes.actualizarEstado(segunda.getCodigo(), EstadoSolicitud.CANCELADA);
            solicitudes.eliminar(primera.getCodigo());
            solicitudes.eliminar(segunda.getCodigo());
            profesionales.eliminar(profesional.getCodigo());
        }
    }

    private boolean intentarAsignar(CountDownLatch inicio, Solicitud solicitud, Profesional profesional) throws InterruptedException {
        inicio.await();
        try {
            solicitudes.asignar(solicitud.getCodigo(), profesional.getCodigo());
            return true;
        } catch (IllegalArgumentException rechazo) {
            return false;
        }
    }

    @RepeatedTest(3)
    void cancelacionConcurrenteNoPuedeSerSobrescritaPorEvaluacion() throws Exception {
        verificar(false);
    }

    @RepeatedTest(3)
    void cancelacionConcurrenteNoPuedeSerSobrescritaPorAsignacion() throws Exception {
        verificar(true);
    }

    private void verificar(boolean asignar) throws Exception {
        Profesional profesional = profesionales.registrar(new Profesional(null, "Ana",
            TipoProfesional.TECNICA, "987654321", "Surco", TipoServicio.ADULTO_MAYOR, true));
        Solicitud solicitud = solicitudes.crear(new Solicitud(null, "Rosa", "987654321", "María", 80,
            TipoServicio.ADULTO_MAYOR, "Surco", LocalDate.now(), TurnoAtencion.MANANA,
            "Apoyo", null, null));
        if (asignar) solicitudes.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_EVALUACION);
        CountDownLatch canceladaSinCommit = new CountDownLatch(1);
        CountDownLatch confirmarCancelacion = new CountDownLatch(1);
        CountDownLatch competidoraIniciada = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> cancelacion = executor.submit(() -> new TransactionTemplate(transactionManager).execute(status -> {
                solicitudes.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.CANCELADA);
                canceladaSinCommit.countDown();
                esperar(confirmarCancelacion);
                return null;
            }));
            assertTrue(canceladaSinCommit.await(10, TimeUnit.SECONDS));
            Future<?> competidora = executor.submit(() -> {
                competidoraIniciada.countDown();
                if (asignar) solicitudes.asignar(solicitud.getCodigo(), profesional.getCodigo());
                else solicitudes.actualizarEstado(solicitud.getCodigo(), EstadoSolicitud.EN_EVALUACION);
            });
            assertTrue(competidoraIniciada.await(10, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> competidora.get(200, TimeUnit.MILLISECONDS));
            confirmarCancelacion.countDown();
            cancelacion.get(10, TimeUnit.SECONDS);
            ExecutionException rechazo = assertThrows(ExecutionException.class,
                () -> competidora.get(10, TimeUnit.SECONDS));
            assertInstanceOf(IllegalArgumentException.class, rechazo.getCause());
            Solicitud finalizada = solicitudes.buscarPorCodigo(solicitud.getCodigo());
            assertEquals(EstadoSolicitud.CANCELADA, finalizada.getEstado());
            assertNull(finalizada.getProfesionalAsignado());
        } finally {
            confirmarCancelacion.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            solicitudes.eliminar(solicitud.getCodigo());
            profesionales.eliminar(profesional.getCodigo());
        }
    }

    private void esperar(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) throw new AssertionError("La prueba no confirmó la cancelación");
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AssertionError(error);
        }
    }
}
