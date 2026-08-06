package com.veterinaria.persistencia;

import com.veterinaria.entity.Especialidad;
import com.veterinaria.entity.Veterinario;
import com.veterinaria.repository.VeterinarioRepository;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VeterinarioRepositoryTest extends PersistenciaBaseTest {

    @Override
    protected String nombreBase() {
        return "veterinarios_test";
    }

    private final VeterinarioRepository repositorio = new VeterinarioRepository();

    @Test
    void guardaYRecuperaVeterinarioConEspecialidades() {
        Veterinario veterinario = Veterinario.crear("10001", "Ana", "Gomez",
                EnumSet.of(Especialidad.CLINICA_GENERAL, Especialidad.CARDIOLOGIA));
        Veterinario guardado = enTransaccion(em -> repositorio.guardar(veterinario, em));

        Veterinario recuperado = enTransaccion(em -> repositorio.buscarPorMatricula(em, "10001"));
        assertNotNull(recuperado);
        assertEquals(guardado.getId(), recuperado.getId());
        assertEquals(2, recuperado.getEspecialidades().size());
    }

    @Test
    void matriculaDuplicadaLanzaErrorDePersistencia() {
        enTransaccion(em -> repositorio.guardar(DatosPrueba.veterinario(1), em));

        assertThrows(PersistenceException.class,
                () -> enTransaccion(em -> repositorio.guardar(DatosPrueba.veterinario(1), em)));
    }
}
