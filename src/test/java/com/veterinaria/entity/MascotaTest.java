package com.veterinaria.entity;

import com.veterinaria.exception.ReglaNegocioException;
import com.veterinaria.exception.ValidacionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MascotaTest {

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = Cliente.crear("30123456", "Juan", "Perez", "1144556677", "juan@mail.com", null);
    }

    private Mascota mascotaValida() {
        return Mascota.crear(cliente, Especie.PERRO, "Rex", "Labrador",
                LocalDate.now().minusYears(3), "M-2026-0001");
    }

    @Test
    void creaMascotaValidaYActivaPorDefecto() {
        Mascota mascota = mascotaValida();
        assertEquals("M-2026-0001", mascota.getFicha());
        assertEquals(Especie.PERRO, mascota.getEspecie());
        assertTrue(mascota.isActiva());
        assertEquals(cliente, mascota.getCliente());
    }

    @Test
    void especieNullLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Mascota.crear(cliente, null, "Rex", "Labrador", LocalDate.now().minusYears(3), "M-2026-0001"));
    }

    @Test
    void nombreVacioLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Mascota.crear(cliente, Especie.PERRO, "", "Labrador", LocalDate.now().minusYears(3), "M-2026-0001"));
    }

    @Test
    void razaVaciaLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Mascota.crear(cliente, Especie.PERRO, "Rex", "", LocalDate.now().minusYears(3), "M-2026-0001"));
    }

    @Test
    void fechaNacimientoFuturaLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Mascota.crear(cliente, Especie.PERRO, "Rex", "Labrador",
                        LocalDate.now().plusDays(1), "M-2026-0001"));
    }

    @Test
    void fichaConFormatoIncorrectoLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Mascota.crear(cliente, Especie.PERRO, "Rex", "Labrador",
                        LocalDate.now().minusYears(3), "FICHA-001"));
    }

    @Test
    void marcarInactivaYReactivar() {
        Mascota mascota = mascotaValida();
        mascota.marcarInactiva();
        assertFalse(mascota.isActiva());
        mascota.reactivar();
        assertTrue(mascota.isActiva());
    }

    @Test
    void marcarInactivaDosVecesLanzaExcepcion() {
        Mascota mascota = mascotaValida();
        mascota.marcarInactiva();
        assertThrows(ReglaNegocioException.class, mascota::marcarInactiva);
    }

    @Test
    void reactivarMascotaActivaLanzaExcepcion() {
        Mascota mascota = mascotaValida();
        assertThrows(ReglaNegocioException.class, mascota::reactivar);
    }
}
