package com.veterinaria.entity;

import com.veterinaria.exception.EntidadInvalidaException;
import com.veterinaria.exception.ValidacionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicioTest {

    @Test
    void creaConsultaMedicaValida() {
        ConsultaMedica consulta = ConsultaMedica.crear("Consulta general", new BigDecimal("15000.00"), 30);
        assertEquals("Consulta general", consulta.getNombre());
        assertEquals(0, new BigDecimal("15000.00").compareTo(consulta.getPrecioBase()));
        assertEquals(30, consulta.getDuracionMinutos());
        assertEquals("Consulta medica", consulta.getTipo());
    }

    @Test
    void precioCeroLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> ConsultaMedica.crear("Consulta general", BigDecimal.ZERO, 30));
    }

    @Test
    void precioNegativoLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> ConsultaMedica.crear("Consulta general", new BigDecimal("-100"), 30));
    }

    @Test
    void duracionCeroLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> ConsultaMedica.crear("Consulta general", new BigDecimal("15000.00"), 0));
    }

    @Test
    void duracionNegativaLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> ConsultaMedica.crear("Consulta general", new BigDecimal("15000.00"), -10));
    }

    @Test
    void nombreVacioLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> ConsultaMedica.crear("", new BigDecimal("15000.00"), 30));
    }

    @Test
    void guarderiaConCupoInvalidoLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> GuarderiaDia.crear("Guarderia 8hs", new BigDecimal("30000.00"), 480, 0));
        assertThrows(ValidacionException.class,
                () -> GuarderiaDia.crear("Guarderia 8hs", new BigDecimal("30000.00"), 480, 500));
    }

    @Test
    void guarderiaValidaCupo() {
        GuarderiaDia guarderia = GuarderiaDia.crear("Guarderia 8hs", new BigDecimal("30000.00"), 480, 10);
        assertEquals(10, guarderia.getCupoMaximo());
    }

    @Test
    void guarderiaConCupoLlenoLanzaExcepcion() {
        GuarderiaDia guarderia = GuarderiaDia.crear("Guarderia 8hs", new BigDecimal("30000.00"), 480, 2);
        guarderia.validarCupoDisponible(1);
        assertThrows(com.veterinaria.exception.ReglaNegocioException.class,
                () -> guarderia.validarCupoDisponible(2));
    }

    @Test
    void aplicacionVacunaSinVacunaLanzaExcepcion() {
        assertThrows(EntidadInvalidaException.class,
                () -> AplicacionVacuna.crear("Vacuna antirrabica", new BigDecimal("20000.00"), 15, null));
    }

    @Test
    void aplicacionVacunaValida() {
        Vacuna vacuna = Vacuna.crear("Nobivac Rabia", "Rabia", 12);
        AplicacionVacuna aplicacion = AplicacionVacuna.crear("Vacuna antirrabica", new BigDecimal("20000.00"), 15, vacuna);
        assertEquals(vacuna, aplicacion.getVacuna());
        assertEquals("Aplicacion de vacuna", aplicacion.getTipo());
    }
}
