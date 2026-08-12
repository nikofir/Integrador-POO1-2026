package com.veterinaria.service;

import com.veterinaria.dto.ClienteDto;
import com.veterinaria.dto.RegistroMedicoDto;
import com.veterinaria.dto.TurnoDto;
import com.veterinaria.dto.VacunaDto;
import com.veterinaria.dto.VeterinarioDto;
import com.veterinaria.entity.Especie;
import com.veterinaria.exception.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TurnoServiceTest extends ServiceTestBase {

    private final ClienteService clientes = new ClienteService();
    private final MascotaService mascotas = new MascotaService();
    private final VeterinarioService veterinarios = new VeterinarioService();
    private final ServicioService servicios = new ServicioService();
    private final VacunaService vacunas = new VacunaService();
    private final TurnoService servicio = new TurnoService();

    private Long mascota;
    private Long veterinario;
    private Long consulta;
    private Long peluqueria;

    @BeforeEach
    void prepararDatos() {
        Long clienteId = clientes.registrar(new ClienteDto(null, "30000001", "Juan", "Perez",
                "1144556677", "juan@mail.com", null)).id();
        mascota = mascotas.registrar(clienteId, Especie.PERRO, "Rex", "Labrador",
                LocalDate.now().minusYears(3)).id();
        veterinario = veterinarios.registrar(new VeterinarioDto(null, "10001", "Ana", "Gomez",
                Set.of("Clinica general"))).id();
        consulta = servicios.crearConsultaMedica("Consulta general", new BigDecimal("15000"), 30).id();
        peluqueria = servicios.crearPeluqueria("Bano y corte", new BigDecimal("25000"), 60).id();
    }

    private LocalDateTime futuro(int dias, int hora) {
        return LocalDateTime.now().plusDays(dias).withHour(hora).withMinute(0)
                .withSecond(0).withNano(0);
    }

    @Test
    void crearTurnoValidoQuedaPendiente() {
        TurnoDto turno = servicio.crearTurno(mascota, veterinario, futuro(3, 10), List.of(consulta));
        assertNotNull(turno.id());
        assertEquals("Pendiente", turno.estado());
        assertEquals(1, turno.turnoServicios().size());
        assertEquals(0, new BigDecimal("15000").compareTo(turno.precioTotal()));
        assertEquals(30, turno.duracionTotal());
    }

    @Test
    void crearTurnoRechazaFechaPasada() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crearTurno(mascota, veterinario,
                LocalDateTime.now().minusHours(1), List.of(consulta)));
    }

    @Test
    void crearTurnoRechazaMascotaInactiva() {
        mascotas.marcarInactiva(mascota);
        assertThrows(ReglaNegocioException.class, () -> servicio.crearTurno(mascota, veterinario,
                futuro(3, 10), List.of(consulta)));
    }

    @Test
    void crearTurnoRechazaServicioRepetido() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crearTurno(mascota, veterinario,
                futuro(3, 10), List.of(consulta, consulta)));
    }

    @Test
    void crearTurnoRechazaSolapamientoDelVeterinario() {
        servicio.crearTurno(mascota, veterinario, futuro(3, 10), List.of(consulta));
        Long otraMascota = mascotas.registrar(clientes.listar().get(0).id(), Especie.GATO, "Misi",
                "Siames", LocalDate.now().minusYears(2)).id();
        assertThrows(ReglaNegocioException.class, () -> servicio.crearTurno(otraMascota, veterinario,
                futuro(3, 10).plusMinutes(15), List.of(consulta)));
    }

    @Test
    void crearTurnoRechazaSolapamientoDeLaMascota() {
        Long otroVeterinario = veterinarios.registrar(new VeterinarioDto(null, "10002", "Luis", "Diaz",
                Set.of("Cirugia"))).id();
        servicio.crearTurno(mascota, veterinario, futuro(3, 10), List.of(consulta));
        assertThrows(ReglaNegocioException.class, () -> servicio.crearTurno(mascota, otroVeterinario,
                futuro(3, 10).plusMinutes(15), List.of(consulta)));
    }

    @Test
    void crearTurnoPermiteTurnosConsecutivos() {
        servicio.crearTurno(mascota, veterinario, futuro(3, 10), List.of(consulta));
        TurnoDto segundo = servicio.crearTurno(mascota, veterinario, futuro(3, 10).plusMinutes(30),
                List.of(consulta));
        assertNotNull(segundo.id());
    }

    @Test
    void crearTurnoRechazaCupoDeGuarderiaCompleto() {
        Long guarderia = servicios.crearGuarderiaDia("Guarderia 8hs", new BigDecimal("30000"), 480, 1).id();
        servicio.crearTurno(mascota, veterinario, futuro(3, 8), List.of(guarderia));

        Long otraMascota = mascotas.registrar(clientes.listar().get(0).id(), Especie.GATO, "Misi",
                "Siames", LocalDate.now().minusYears(2)).id();
        Long otroVeterinario = veterinarios.registrar(new VeterinarioDto(null, "10002", "Luis", "Diaz",
                Set.of("Cirugia"))).id();

        assertThrows(ReglaNegocioException.class, () -> servicio.crearTurno(otraMascota, otroVeterinario,
                futuro(3, 9), List.of(guarderia)));
    }

    @Test
    void crearTurnoRechazaVacunaYaVigente() {
        Long vacunaId = vacunas.registrar(new VacunaDto(null, "Vacuna 1", "Enfermedad 1", 1)).id();
        Long aplicacion = servicios.crearAplicacionVacuna("Aplicacion 1", new BigDecimal("20000"), 15,
                vacunaId).id();

        TurnoDto primero = servicio.crearTurno(mascota, veterinario, futuro(2, 10), List.of(aplicacion));
        servicio.confirmar(primero.id());
        servicio.atender(primero.id());

        assertThrows(ReglaNegocioException.class, () -> servicio.crearTurno(mascota, veterinario,
                futuro(20, 10), List.of(aplicacion)));
    }

    @Test
    void cicloDeVidaConfirmarYAtender() {
        TurnoDto turno = servicio.crearTurno(mascota, veterinario, futuro(3, 10), List.of(consulta));
        TurnoDto confirmado = servicio.confirmar(turno.id());
        assertEquals("Confirmado", confirmado.estado());
        TurnoDto atendido = servicio.atender(turno.id());
        assertEquals("Atendido", atendido.estado());
    }

    @Test
    void cancelarPendienteSiemprePermitido() {
        TurnoDto turno = servicio.crearTurno(mascota, veterinario, futuro(3, 10), List.of(consulta));
        TurnoDto cancelado = servicio.cancelar(turno.id(), LocalDateTime.now());
        assertEquals("Cancelado", cancelado.estado());
    }

    @Test
    void cancelarConfirmadoDentroDeLas24HorasLanzaRegla() {
        TurnoDto turno = servicio.crearTurno(mascota, veterinario,
                LocalDateTime.now().plusHours(5), List.of(consulta));
        servicio.confirmar(turno.id());
        assertThrows(ReglaNegocioException.class, () -> servicio.cancelar(turno.id(), LocalDateTime.now()));
    }

    @Test
    void cancelarConfirmadoConPlazoValido() {
        TurnoDto turno = servicio.crearTurno(mascota, veterinario, futuro(3, 10), List.of(consulta));
        servicio.confirmar(turno.id());
        TurnoDto cancelado = servicio.cancelar(turno.id(), LocalDateTime.now());
        assertEquals("Cancelado", cancelado.estado());
    }

    @Test
    void registrarConsultaSoloEnTurnoAtendido() {
        TurnoDto turno = servicio.crearTurno(mascota, veterinario, futuro(3, 10), List.of(consulta));
        servicio.confirmar(turno.id());
        TurnoDto atendido = servicio.atender(turno.id());

        Long detalleId = atendido.turnoServicios().get(0).id();
        RegistroMedicoDto registro = servicio.registrarConsulta(turno.id(), detalleId,
                "Otitis", "Limpieza y antibioticos");
        assertNotNull(registro.id());
        assertEquals("Otitis", registro.diagnostico());
    }

    @Test
    void agregarServicioAmpliaElTurno() {
        TurnoDto turno = servicio.crearTurno(mascota, veterinario, futuro(3, 10), List.of(consulta));
        TurnoDto ampliado = servicio.agregarServicio(turno.id(), peluqueria);

        assertEquals(2, ampliado.turnoServicios().size());
        assertEquals(90, ampliado.duracionTotal());
        assertEquals(0, new BigDecimal("40000").compareTo(ampliado.precioTotal()));
    }

    @Test
    void quitarServicioReduceElTurno() {
        TurnoDto turno = servicio.crearTurno(mascota, veterinario, futuro(3, 10),
                List.of(consulta, peluqueria));
        TurnoDto persistido = servicio.buscar(turno.id());

        Long detalleId = persistido.turnoServicios().stream()
                .filter(d -> d.servicioId().equals(peluqueria))
                .findFirst().orElseThrow().id();
        TurnoDto reducido = servicio.quitarServicio(turno.id(), detalleId);

        assertEquals(1, reducido.turnoServicios().size());
        assertEquals(0, new BigDecimal("15000").compareTo(reducido.precioTotal()));
    }

    @Test
    void ingresosSoloCuentanTurnosAtendidos() {
        TurnoDto a = servicio.crearTurno(mascota, veterinario, futuro(2, 9), List.of(consulta));
        TurnoDto b = servicio.crearTurno(mascota, veterinario, futuro(2, 10), List.of(peluqueria));
        TurnoDto c = servicio.crearTurno(mascota, veterinario, futuro(2, 11), List.of(consulta));
        servicio.confirmar(a.id());
        servicio.atender(a.id());
        servicio.confirmar(b.id());
        servicio.atender(b.id());

        BigDecimal ingresos = servicio.ingresosEntre(LocalDateTime.now(), LocalDateTime.now().plusDays(5));
        assertEquals(0, new BigDecimal("40000").compareTo(ingresos));
        assertNotNull(c);
    }

    @Test
    void listarTurnosDelDia() {
        LocalDateTime dia = futuro(2, 9);
        servicio.crearTurno(mascota, veterinario, dia, List.of(consulta));
        servicio.crearTurno(mascota, veterinario, dia.withHour(11), List.of(peluqueria));

        assertEquals(2, servicio.listarPorDia(dia.toLocalDate()).size());
        assertEquals(0, servicio.listarPorDia(dia.toLocalDate().plusDays(10)).size());
    }

    @Test
    void historialSoloMuestraTurnosAtendidosConRegistro() {
        TurnoDto atendido = servicio.crearTurno(mascota, veterinario, futuro(2, 9), List.of(consulta));
        servicio.confirmar(atendido.id());
        TurnoDto atendidoDto = servicio.atender(atendido.id());
        Long detalleId = atendidoDto.turnoServicios().get(0).id();
        servicio.registrarConsulta(atendido.id(), detalleId, "Otitis", "Antibioticos");

        servicio.crearTurno(mascota, veterinario, futuro(3, 10), List.of(consulta));

        List<TurnoDto> historial = servicio.listarHistorial(mascota);
        assertEquals(1, historial.size());
        assertEquals("Atendido", historial.get(0).estado());
        assertEquals("Otitis", historial.get(0).turnoServicios().get(0).registroMedico().diagnostico());
        assertEquals(0, servicio.listarHistorial(999999L).size());
    }
}
