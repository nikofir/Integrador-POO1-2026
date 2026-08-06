package com.veterinaria.service;

import com.veterinaria.dto.ServicioDto;
import com.veterinaria.dto.VacunaDto;
import com.veterinaria.exception.EntidadNoEncontradaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioServiceTest extends ServiceTestBase {

    private final ServicioService servicio = new ServicioService();
    private final VacunaService vacunas = new VacunaService();

    @Test
    void crearTodosLosTiposDeServicio() {
        ServicioDto consulta = servicio.crearConsultaMedica("Consulta general", new BigDecimal("15000"), 30);
        ServicioDto peluqueria = servicio.crearPeluqueria("Bano y corte", new BigDecimal("25000"), 60);
        ServicioDto guarderia = servicio.crearGuarderiaDia("Guarderia 8hs", new BigDecimal("30000"), 480, 10);
        Long vacunaId = vacunas.registrar(new VacunaDto(null, "Vacuna 1", "Enfermedad 1", 12)).id();
        ServicioDto aplicacion = servicio.crearAplicacionVacuna("Aplicacion 1", new BigDecimal("20000"), 15, vacunaId);

        assertEquals("Consulta medica", consulta.tipo());
        assertEquals("Peluqueria", peluqueria.tipo());
        assertEquals("Guarderia por dia", guarderia.tipo());
        assertEquals(10, guarderia.cupoMaximo());
        assertEquals("Aplicacion de vacuna", aplicacion.tipo());
        assertEquals("Vacuna 1", aplicacion.vacunaNombre());
        assertNull(consulta.cupoMaximo());
        assertNull(consulta.vacunaNombre());
    }

    @Test
    void crearAplicacionConVacunaInexistenteLanzaNoEncontrada() {
        assertThrows(EntidadNoEncontradaException.class, () ->
                servicio.crearAplicacionVacuna("Aplicacion 1", new BigDecimal("20000"), 15, 999L));
    }

    @Test
    void actualizarModificaElPrecioYLaDuracion() {
        ServicioDto consulta = servicio.crearConsultaMedica("Consulta general", new BigDecimal("15000"), 30);
        ServicioDto actualizado = servicio.actualizar(consulta.id(), "Consulta general",
                new BigDecimal("18000"), 45);

        assertEquals(0, new BigDecimal("18000").compareTo(actualizado.precioBase()));
        assertEquals(45, actualizado.duracionMinutos());
    }

    @Test
    void actualizarCupoDeUnaGuarderia() {
        ServicioDto guarderia = servicio.crearGuarderiaDia("Guarderia 8hs", new BigDecimal("30000"), 480, 10);
        ServicioDto actualizada = servicio.actualizarCupoGuarderia(guarderia.id(), 5);
        assertEquals(5, actualizada.cupoMaximo());
    }

    @Test
    void listarCatalogoDevuelveTodosLosServicios() {
        assertTrue(servicio.listarCatalogo().isEmpty());
        servicio.crearConsultaMedica("Consulta general", new BigDecimal("15000"), 30);
        servicio.crearPeluqueria("Bano y corte", new BigDecimal("25000"), 60);
        assertEquals(2, servicio.listarCatalogo().size());
    }
}
