package com.veterinaria.service;

import com.veterinaria.dto.VacunaDto;
import com.veterinaria.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VacunaServiceTest extends ServiceTestBase {

    private final VacunaService servicio = new VacunaService();

    @Test
    void registrarCreaVacuna() {
        VacunaDto creada = servicio.registrar(new VacunaDto(null, "Vacuna 1", "Enfermedad 1", 12));
        assertNotNull(creada.id());
        assertEquals("Vacuna 1", creada.nombreComercial());
        assertEquals(12, creada.periodicidadMeses());
    }

    @Test
    void registrarRechazaNombreComercialDuplicado() {
        servicio.registrar(new VacunaDto(null, "Vacuna 1", "Enfermedad 1", 12));
        assertThrows(ReglaNegocioException.class, () ->
                servicio.registrar(new VacunaDto(null, "Vacuna 1", "Otra enfermedad", 6)));
    }

    @Test
    void listarDevuelveLasVacunas() {
        assertTrue(servicio.listar().isEmpty());
        VacunaDto creada = servicio.registrar(new VacunaDto(null, "Vacuna 1", "Enfermedad 1", 12));
        assertEquals(1, servicio.listar().size());
        assertEquals(creada.id(), servicio.buscar(creada.id()).id());
    }
}
