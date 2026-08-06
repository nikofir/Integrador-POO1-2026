package com.veterinaria.persistencia;

import com.veterinaria.entity.AplicacionVacuna;
import com.veterinaria.entity.Cliente;
import com.veterinaria.entity.ConsultaMedica;
import com.veterinaria.entity.EstadoTurno;
import com.veterinaria.entity.GuarderiaDia;
import com.veterinaria.entity.Mascota;
import com.veterinaria.entity.Peluqueria;
import com.veterinaria.entity.Servicio;
import com.veterinaria.entity.Turno;
import com.veterinaria.entity.Vacuna;
import com.veterinaria.entity.Veterinario;
import com.veterinaria.repository.ClienteRepository;
import com.veterinaria.repository.ServicioRepository;
import com.veterinaria.repository.TurnoRepository;
import com.veterinaria.repository.VacunaRepository;
import com.veterinaria.repository.VeterinarioRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnoRepositoryTest extends PersistenciaBaseTest {

    @Override
    protected String nombreBase() {
        return "turnos_test";
    }

    private final ClienteRepository repositorioClientes = new ClienteRepository();
    private final VeterinarioRepository repositorioVeterinarios = new VeterinarioRepository();
    private final ServicioRepository repositorioServicios = new ServicioRepository();
    private final VacunaRepository repositorioVacunas = new VacunaRepository();
    private final TurnoRepository repositorio = new TurnoRepository();

    private Turno persistirTurno(LocalDateTime fechaHora, List<Servicio> servicios, boolean atender) {
        return persistirTurno(1, fechaHora, servicios, atender, List.of());
    }

    private Mascota mascotaPersistida(Cliente cliente, int n) {
        Mascota mascota = DatosPrueba.mascota(cliente, n);
        cliente.agregarMascota(mascota);
        return mascota;
    }

    private Turno persistirTurno(int nCliente, LocalDateTime fechaHora, List<Servicio> servicios, boolean atender) {
        return persistirTurno(nCliente, fechaHora, servicios, atender, List.of());
    }

    private Turno persistirTurno(int nCliente, LocalDateTime fechaHora, List<Servicio> servicios, boolean atender,
                                 List<Vacuna> vacunas) {
        return enTransaccion(em -> {
            Cliente cliente = DatosPrueba.cliente(nCliente);
            Mascota mascota = mascotaPersistida(cliente, nCliente);
            repositorioClientes.guardar(cliente, em);

            Veterinario veterinario = DatosPrueba.veterinario(nCliente);
            repositorioVeterinarios.guardar(veterinario, em);

            for (Vacuna vacuna : vacunas) {
                repositorioVacunas.guardar(vacuna, em);
            }
            for (Servicio servicio : servicios) {
                repositorioServicios.guardar(servicio, em);
            }

            Turno turno = Turno.crear(mascota, veterinario, fechaHora, servicios);
            if (atender) {
                turno.confirmar();
                turno.atender();
            }
            Turno guardado = repositorio.guardar(turno, em);
            em.flush();
            return guardado;
        });
    }

    @Test
    void guardaTurnoConServiciosYLosRecupera() {
        ConsultaMedica consulta = DatosPrueba.consultaMedica();
        Turno turno = persistirTurno(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0),
                List.of(consulta), false);

        Turno recuperado = enTransaccion(em -> repositorio.buscarConDetalles(em, turno.getId()).orElseThrow());
        assertNotNull(recuperado);
        assertEquals(1, recuperado.getTurnoServicios().size());
        assertEquals("Consulta general", recuperado.getTurnoServicios().get(0).getServicio().getNombre());
        assertEquals(0, new BigDecimal("15000.00").compareTo(recuperado.calcularPrecioTotal()));
        assertEquals(30, recuperado.calcularDuracionTotal());
    }

    @Test
    void cuentaCupoGuarderiaDelDia() {
        GuarderiaDia guarderia = GuarderiaDia.crear("Guarderia 8hs", new BigDecimal("30000.00"), 480, 10);
        LocalDate dia = LocalDate.now().plusDays(1);

        persistirTurno(1, dia.atTime(10, 0), List.of(guarderia), false);
        Turno cancelado = persistirTurno(2, dia.atTime(11, 0), List.of(guarderia), false);
        enTransaccion(em -> {
            Turno t = repositorio.buscarConDetalles(em, cancelado.getId()).orElseThrow();
            t.cancelar(LocalDateTime.now());
            repositorio.guardar(t, em);
            return null;
        });
        persistirTurno(3, dia.atTime(12, 0), List.of(guarderia), false);

        long cupoOcupado = enTransaccion(em -> repositorio.contarGuarderiaEnDia(em, dia));
        assertEquals(2L, cupoOcupado);
    }

    @Test
    void ultimaAplicacionVacuna() {
        Vacuna vacuna = DatosPrueba.vacuna(1);
        AplicacionVacuna aplicacion = AplicacionVacuna.crear("Aplicacion 1", new BigDecimal("20000.00"), 15, vacuna);
        LocalDateTime fecha = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0);

        Turno turno = persistirTurno(1, fecha, List.of(aplicacion), true, List.of(vacuna));
        Long mascotaId = turno.getMascota().getId();

        LocalDateTime ultima = enTransaccion(em -> repositorio.ultimaAplicacionVacuna(em, mascotaId, vacuna.getId()));
        assertNotNull(ultima);
        assertEquals(fecha.truncatedTo(java.time.temporal.ChronoUnit.MILLIS),
                ultima.truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
    }

    @Test
    void sinAplicacionPreviaDevuelveNull() {
        Turno turno = persistirTurno(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0),
                List.of(DatosPrueba.consultaMedica()), true);
        Vacuna vacuna = enTransaccion(em -> repositorioVacunas.guardar(DatosPrueba.vacuna(2), em));

        LocalDateTime ultima = enTransaccion(em ->
                repositorio.ultimaAplicacionVacuna(em, turno.getMascota().getId(), vacuna.getId()));
        assertNull(ultima);
    }

    @Test
    void listaCandidatosSolapadosDelMismoVeterinario() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0);
        Turno turno = persistirTurno(inicio, List.of(DatosPrueba.consultaMedica()), false);

        List<EstadoTurno> activos = List.of(EstadoTurno.PENDIENTE, EstadoTurno.CONFIRMADO);
        List<Turno> candidatos = enTransaccion(em -> repositorio.listarCandidatosSolapados(
                em, "veterinario", turno.getVeterinario().getId(), inicio, inicio.plusMinutes(30), activos));

        assertTrue(candidatos.stream().anyMatch(t -> t.getId().equals(turno.getId())));

        List<Turno> candidatosMascota = enTransaccion(em -> repositorio.listarCandidatosSolapados(
                em, "mascota", turno.getMascota().getId(), inicio, inicio.plusMinutes(30), activos));
        assertTrue(candidatosMascota.stream().anyMatch(t -> t.getId().equals(turno.getId())));
    }

    @Test
    void sumaIngresosSoloDeTurnosAtendidos() {
        LocalDateTime desde = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
        LocalDateTime hasta = LocalDateTime.now().plusDays(1).withHour(20).withMinute(0);

        persistirTurno(1, desde.withMinute(30), List.of(DatosPrueba.consultaMedica()), true);
        Peluqueria peluqueria = Peluqueria.crear("Bano y corte", new BigDecimal("25000.00"), 60);
        persistirTurno(2, desde.withHour(11), List.of(peluqueria), true);
        persistirTurno(3, desde.withHour(15), List.of(DatosPrueba.consultaMedica()), false);

        BigDecimal ingresos = enTransaccion(em -> repositorio.sumarIngresos(em, desde, hasta));
        assertEquals(0, new BigDecimal("40000.00").compareTo(ingresos));
    }

    @Test
    void listaTurnosDeUnDia() {
        LocalDate dia = LocalDate.now().plusDays(2);
        persistirTurno(1, dia.atTime(9, 0), List.of(DatosPrueba.consultaMedica()), false);
        persistirTurno(2, dia.atTime(11, 0), List.of(DatosPrueba.consultaMedica()), false);

        int cantidad = enTransaccion(em -> repositorio.listarPorDia(em, dia).size());
        assertEquals(2, cantidad);
    }
}
