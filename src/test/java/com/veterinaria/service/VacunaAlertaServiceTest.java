package com.veterinaria.service;

import com.veterinaria.dto.AlertaVacunaDto;
import com.veterinaria.dto.ClienteDto;
import com.veterinaria.dto.TurnoDto;
import com.veterinaria.dto.VacunaDto;
import com.veterinaria.dto.VeterinarioDto;
import com.veterinaria.entity.Especie;
import com.veterinaria.entity.EstadoVacuna;
import com.veterinaria.util.JpaUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VacunaAlertaServiceTest extends ServiceTestBase {

    private final ClienteService clientes = new ClienteService();
    private final MascotaService mascotas = new MascotaService();
    private final VeterinarioService veterinarios = new VeterinarioService();
    private final ServicioService servicios = new ServicioService();
    private final VacunaService vacunas = new VacunaService();
    private final TurnoService turnos = new TurnoService();
    private final VacunaAlertaService servicio = new VacunaAlertaService();

    private Long mascota;
    private Long veterinario;
    private Long aplicacion;

    @BeforeEach
    void prepararDatos() {
        Long clienteId = clientes.registrar(new ClienteDto(null, "30000002", "Lucia", "Mendez",
                "1133445566", "lucia@mail.com", null)).id();
        mascota = mascotas.registrar(clienteId, Especie.PERRO, "Rex", "Labrador",
                LocalDate.now().minusYears(3)).id();
        veterinario = veterinarios.registrar(new VeterinarioDto(null, "10002", "Ana", "Gomez",
                Set.of("Clinica general"))).id();
        Long vacunaId = vacunas.registrar(new VacunaDto(null, "Antirrabica", "Rabia", 1)).id();
        aplicacion = servicios.crearAplicacionVacuna("Aplicacion antirrabica",
                new BigDecimal("20000"), 15, vacunaId).id();
    }

    private LocalDateTime futuro(int dias) {
        return LocalDateTime.now().plusDays(dias).withHour(10).withMinute(0)
                .withSecond(0).withNano(0);
    }

    private Long aplicarVacuna(Long servicioId, int dia) {
        TurnoDto turno = turnos.crearTurno(mascota, veterinario, futuro(dia), List.of(servicioId));
        turnos.confirmar(turno.id());
        turnos.atender(turno.id());
        return turno.id();
    }

    private void retrocederTurno(Long turnoId, LocalDateTime fecha) {
        JpaUtil.enTransaccion(em -> {
            em.createQuery("UPDATE Turno t SET t.fechaHora = :f WHERE t.id = :id")
                    .setParameter("f", fecha)
                    .setParameter("id", turnoId)
                    .executeUpdate();
            return null;
        });
    }

    @Test
    void vacunaRecientementeAplicadaNoGeneraAlerta() {
        aplicarVacuna(aplicacion, 1);
        assertTrue(servicio.listarAlertas().isEmpty());
    }

    @Test
    void vacunaSinAplicacionesNoGeneraAlerta() {
        assertTrue(servicio.listarAlertas().isEmpty());
    }

    @Test
    void vacunaVencidaGeneraAlertaVencida() {
        Long turnoId = aplicarVacuna(aplicacion, 1);
        retrocederTurno(turnoId, LocalDateTime.now().minusMonths(2));

        List<AlertaVacunaDto> alertas = servicio.listarAlertas();
        assertEquals(1, alertas.size());
        AlertaVacunaDto alerta = alertas.get(0);
        assertEquals("Rex", alerta.mascotaNombre());
        assertEquals("Antirrabica", alerta.vacunaNombre());
        assertEquals(EstadoVacuna.VENCIDA, alerta.estado());
        assertTrue(alerta.esVencida());
        assertTrue(alerta.diasRestantes() < 0);
    }

    @Test
    void vacunaProximaAVencerGeneraAlertaProxima() {
        Long turnoId = aplicarVacuna(aplicacion, 1);
        retrocederTurno(turnoId, LocalDateTime.now().minusMonths(1).plusDays(5));

        List<AlertaVacunaDto> alertas = servicio.listarAlertas();
        assertEquals(1, alertas.size());
        assertEquals(EstadoVacuna.PROXIMA_A_VENCER, alertas.get(0).estado());
        assertTrue(alertas.get(0).diasRestantes() > 0);
    }

    @Test
    void ordenaLasAlertasPorUrgencia() {
        Long turnoVencida = aplicarVacuna(aplicacion, 1);
        retrocederTurno(turnoVencida, LocalDateTime.now().minusMonths(2));

        Long segundaVacuna = vacunas.registrar(
                new VacunaDto(null, "Triple felina", "Panleucopenia", 1)).id();
        Long segundaAplicacion = servicios.crearAplicacionVacuna("Aplicacion triple felina",
                new BigDecimal("22000"), 15, segundaVacuna).id();
        Long turnoProxima = aplicarVacuna(segundaAplicacion, 2);
        retrocederTurno(turnoProxima, LocalDateTime.now().minusMonths(1).plusDays(5));

        List<AlertaVacunaDto> alertas = servicio.listarAlertas();
        assertEquals(2, alertas.size());
        assertEquals(EstadoVacuna.VENCIDA, alertas.get(0).estado());
        assertEquals(EstadoVacuna.PROXIMA_A_VENCER, alertas.get(1).estado());
    }
}
