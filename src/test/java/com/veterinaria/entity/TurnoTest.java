package com.veterinaria.entity;

import com.veterinaria.exception.EntidadInvalidaException;
import com.veterinaria.exception.ReglaNegocioException;
import com.veterinaria.exception.ValidacionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TurnoTest {

    private static final BigDecimal PRECIO_CONSULTA = new BigDecimal("15000.00");
    private static final BigDecimal PRECIO_PELUQUERIA = new BigDecimal("25000.00");

    private Cliente cliente() {
        return Cliente.crear("30123456", "Juan", "Perez", "1144556677", "juan@mail.com", null);
    }

    private Mascota mascota(Cliente cliente) {
        Mascota mascota = Mascota.crear(cliente, Especie.PERRO, "Rex", "Labrador",
                LocalDate.now().minusYears(3), "M-2026-0001");
        cliente.agregarMascota(mascota);
        return mascota;
    }

    private Veterinario veterinario() {
        return Veterinario.crear("12345", "Ana", "Gomez", EnumSet.of(Especialidad.CLINICA_GENERAL));
    }

    private List<Servicio> servicios() {
        return List.of(
                ConsultaMedica.crear("Consulta general", PRECIO_CONSULTA, 30),
                Peluqueria.crear("Bano y corte", PRECIO_PELUQUERIA, 60));
    }

    private Turno turnoValido() {
        return Turno.crear(mascota(cliente()), veterinario(), LocalDateTime.now().plusDays(5), servicios());
    }

    @Test
    void creaTurnoValidoEnEstadoPendiente() {
        Turno turno = turnoValido();
        assertEquals(EstadoTurno.PENDIENTE, turno.getEstado());
        assertEquals(2, turno.getTurnoServicios().size());
    }

    @Test
    void calculaPrecioTotal() {
        Turno turno = turnoValido();
        assertEquals(0, new BigDecimal("40000.00").compareTo(turno.calcularPrecioTotal()));
    }

    @Test
    void calculaDuracionTotal() {
        Turno turno = turnoValido();
        assertEquals(90, turno.calcularDuracionTotal());
    }

    @Test
    void sinServiciosLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Turno.crear(mascota(cliente()), veterinario(), LocalDateTime.now().plusDays(5), List.of()));
    }

    @Test
    void conServicioDuplicadoLanzaExcepcion() {
        ConsultaMedica consulta = ConsultaMedica.crear("Consulta general", PRECIO_CONSULTA, 30);
        assertThrows(ValidacionException.class,
                () -> Turno.crear(mascota(cliente()), veterinario(), LocalDateTime.now().plusDays(5),
                        List.of(consulta, consulta)));
    }

    @Test
    void fechaPasadaLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Turno.crear(mascota(cliente()), veterinario(), LocalDateTime.now().minusDays(1), servicios()));
    }

    @Test
    void mascotaInactivaLanzaExcepcion() {
        Cliente cliente = cliente();
        Mascota mascota = mascota(cliente);
        mascota.marcarInactiva();
        assertThrows(ReglaNegocioException.class,
                () -> Turno.crear(mascota, veterinario(), LocalDateTime.now().plusDays(5), servicios()));
    }

    @Test
    void confirmarSoloDesdePendiente() {
        Turno turno = turnoValido();
        turno.confirmar();
        assertEquals(EstadoTurno.CONFIRMADO, turno.getEstado());
        assertThrows(ReglaNegocioException.class, turno::confirmar);
    }

    @Test
    void atenderRequiereConfirmado() {
        Turno turno = turnoValido();
        assertThrows(ReglaNegocioException.class, turno::atender);
        turno.confirmar();
        turno.atender();
        assertEquals(EstadoTurno.ATENDIDO, turno.getEstado());
        assertThrows(ReglaNegocioException.class, turno::atender);
    }

    @Test
    void cancelarPendienteEnCualquierMomento() {
        Turno turno = Turno.crear(mascota(cliente()), veterinario(), LocalDateTime.now().plusHours(1), servicios());
        turno.cancelar(LocalDateTime.now());
        assertEquals(EstadoTurno.CANCELADO, turno.getEstado());
    }

    @Test
    void cancelarConfirmadoConAnticipacionSuficiente() {
        Turno turno = turnoValido();
        turno.confirmar();
        turno.cancelar(LocalDateTime.now());
        assertEquals(EstadoTurno.CANCELADO, turno.getEstado());
    }

    @Test
    void cancelarConfirmadoFueraDeTerminoLanzaExcepcion() {
        Turno turno = Turno.crear(mascota(cliente()), veterinario(), LocalDateTime.now().plusHours(12), servicios());
        turno.confirmar();
        assertThrows(ReglaNegocioException.class, () -> turno.cancelar(LocalDateTime.now()));
    }

    @Test
    void cancelarTurnoAtendidoLanzaExcepcion() {
        Turno turno = turnoValido();
        turno.confirmar();
        turno.atender();
        assertThrows(ReglaNegocioException.class, () -> turno.cancelar(LocalDateTime.now()));
    }

    @Test
    void cancelarTurnoYaCanceladoLanzaExcepcion() {
        Turno turno = turnoValido();
        turno.cancelar(LocalDateTime.now());
        assertThrows(ReglaNegocioException.class, () -> turno.cancelar(LocalDateTime.now()));
    }

    @Test
    void agregarServicioDuplicadoLanzaExcepcion() {
        Turno turno = turnoValido();
        Servicio consulta = turno.getTurnoServicios().get(0).getServicio();
        assertThrows(ReglaNegocioException.class, () -> turno.agregarServicio(consulta));
    }

    @Test
    void agregarServicioEnTurnoTerminalLanzaExcepcion() {
        Turno turno = turnoValido();
        turno.confirmar();
        turno.atender();
        assertThrows(ReglaNegocioException.class,
                () -> turno.agregarServicio(ConsultaMedica.crear("Control", PRECIO_CONSULTA, 20)));
    }

    @Test
    void quitarServicioDelTurno() {
        Turno turno = turnoValido();
        TurnoServicio aQuitar = turno.getTurnoServicios().get(0);
        turno.quitarServicio(aQuitar);
        assertEquals(1, turno.getTurnoServicios().size());
        assertEquals(0, PRECIO_PELUQUERIA.compareTo(turno.calcularPrecioTotal()));
    }

    @Test
    void quitarServicioAjenoAlTurnoLanzaExcepcion() {
        Turno turno = turnoValido();
        Turno otroTurno = Turno.crear(mascota(cliente()), veterinario(), LocalDateTime.now().plusDays(3), servicios());
        assertThrows(EntidadInvalidaException.class,
                () -> turno.quitarServicio(otroTurno.getTurnoServicios().get(0)));
    }

    @Test
    void registroMedicoSoloEnTurnosAtendidos() {
        Turno turno = turnoValido();
        RegistroMedico registro = RegistroMedico.crear("Otitis leve", "Limpieza y antibioticos");

        assertThrows(ReglaNegocioException.class,
                () -> turno.registrarRegistroMedico(turno.getTurnoServicios().get(0), registro));
    }

    @Test
    void registroMedicoEnTurnoAtendido() {
        Turno turno = turnoValido();
        turno.confirmar();
        turno.atender();

        TurnoServicio turnoServicio = turno.getTurnoServicios().get(0);
        RegistroMedico registro = RegistroMedico.crear("Otitis leve", "Limpieza y antibioticos");
        turno.registrarRegistroMedico(turnoServicio, registro);

        assertEquals(registro, turnoServicio.getRegistroMedico());
        assertEquals("Otitis leve", turnoServicio.getRegistroMedico().getDiagnostico());
    }

    @Test
    void precioHistoricoFijaInstantaneaAlReservar() {
        ConsultaMedica consulta = ConsultaMedica.crear("Consulta general", new BigDecimal("10000.00"), 30);
        Turno turno = Turno.crear(mascota(cliente()), veterinario(), LocalDateTime.now().plusDays(2), List.of(consulta));
        assertEquals(0, new BigDecimal("10000.00").compareTo(turno.getTurnoServicios().get(0).getPrecioHistorico()));
    }
}
