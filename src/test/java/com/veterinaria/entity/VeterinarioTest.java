package com.veterinaria.entity;

import com.veterinaria.exception.EntidadInvalidaException;
import com.veterinaria.exception.ReglaNegocioException;
import com.veterinaria.exception.ValidacionException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VeterinarioTest {

    private Veterinario veterinarioValido() {
        return Veterinario.crear("12345", "Ana", "Gomez", EnumSet.of(Especialidad.CLINICA_GENERAL));
    }

    @Test
    void creaVeterinarioValido() {
        Veterinario veterinario = veterinarioValido();
        assertEquals("12345", veterinario.getMatricula());
        assertEquals("Ana Gomez", veterinario.getNombreCompleto());
        assertEquals(1, veterinario.getEspecialidades().size());
    }

    @Test
    void sinEspecialidadesLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Veterinario.crear("12345", "Ana", "Gomez", Collections.emptySet()));
    }

    @Test
    void especialidadesNullLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Veterinario.crear("12345", "Ana", "Gomez", null));
    }

    @Test
    void matriculaConLetrasLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Veterinario.crear("12A45", "Ana", "Gomez", EnumSet.of(Especialidad.CLINICA_GENERAL)));
    }

    @Test
    void nombreConNumerosLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Veterinario.crear("12345", "Ana2", "Gomez", EnumSet.of(Especialidad.CLINICA_GENERAL)));
    }

    @Test
    void agregarEspecialidadDuplicadaLanzaExcepcion() {
        Veterinario veterinario = veterinarioValido();
        assertThrows(EntidadInvalidaException.class,
                () -> veterinario.agregarEspecialidad(Especialidad.CLINICA_GENERAL));
    }

    @Test
    void removerEspecialidadHastaDejarUnaLanzaExcepcion() {
        Veterinario veterinario = Veterinario.crear("12345", "Ana", "Gomez",
                EnumSet.of(Especialidad.CLINICA_GENERAL, Especialidad.CARDIOLOGIA));

        veterinario.removerEspecialidad(Especialidad.CARDIOLOGIA);
        assertThrows(ReglaNegocioException.class,
                () -> veterinario.removerEspecialidad(Especialidad.CLINICA_GENERAL));
    }

    @Test
    void removerEspecialidadNoRegistradaLanzaExcepcion() {
        Veterinario veterinario = veterinarioValido();
        assertThrows(EntidadInvalidaException.class,
                () -> veterinario.removerEspecialidad(Especialidad.CIRUGIA));
    }
}
