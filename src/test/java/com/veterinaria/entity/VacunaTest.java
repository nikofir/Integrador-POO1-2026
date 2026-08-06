package com.veterinaria.entity;

import com.veterinaria.exception.ValidacionException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VacunaTest {

    @Test
    void creaVacunaValida() {
        Vacuna vacuna = Vacuna.crear("Nobivac Rabia", "Rabia", 12);
        assertEquals("Nobivac Rabia", vacuna.getNombreComercial());
        assertEquals("Rabia", vacuna.getEnfermedadPrevenida());
        assertEquals(12, vacuna.getPeriodicidadMeses());
    }

    @Test
    void nombreComercialVacioLanzaExcepcion() {
        assertThrows(ValidacionException.class, () -> Vacuna.crear("", "Rabia", 12));
    }

    @Test
    void enfermedadVaciaLanzaExcepcion() {
        assertThrows(ValidacionException.class, () -> Vacuna.crear("Nobivac Rabia", "", 12));
    }

    @Test
    void periodicidadCeroLanzaExcepcion() {
        assertThrows(ValidacionException.class, () -> Vacuna.crear("Nobivac Rabia", "Rabia", 0));
    }

    @Test
    void sinAplicacionPreviaNoEstaVigente() {
        Vacuna vacuna = Vacuna.crear("Nobivac Rabia", "Rabia", 12);
        assertFalse(vacuna.estaVigente(null, LocalDate.now()));
    }

    @Test
    void aplicacionRecienteSigueVigente() {
        Vacuna vacuna = Vacuna.crear("Nobivac Rabia", "Rabia", 12);
        LocalDate ultimaAplicacion = LocalDate.now().minusMonths(6);
        assertTrue(vacuna.estaVigente(ultimaAplicacion, LocalDate.now()));
    }

    @Test
    void aplicacionVencidaNoEstaVigente() {
        Vacuna vacuna = Vacuna.crear("Nobivac Rabia", "Rabia", 12);
        LocalDate ultimaAplicacion = LocalDate.now().minusMonths(13);
        assertFalse(vacuna.estaVigente(ultimaAplicacion, LocalDate.now()));
    }

    @Test
    void enElLimiteExactoSigueVigente() {
        Vacuna vacuna = Vacuna.crear("Nobivac Rabia", "Rabia", 12);
        LocalDate ultimaAplicacion = LocalDate.now().minusMonths(12);
        assertTrue(vacuna.estaVigente(ultimaAplicacion, LocalDate.now()));
    }
}
