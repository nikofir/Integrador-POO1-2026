package com.veterinaria.service;

import com.veterinaria.dto.ClienteDto;
import com.veterinaria.dto.VeterinarioDto;
import com.veterinaria.entity.Especie;
import com.veterinaria.exception.EntidadInvalidaException;
import com.veterinaria.exception.EntidadNoEncontradaException;
import com.veterinaria.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VeterinarioServiceTest extends ServiceTestBase {

    private final VeterinarioService servicio = new VeterinarioService();

    private VeterinarioDto veterinario(String matricula) {
        return new VeterinarioDto(null, matricula, "Ana", "Gomez", Set.of("Clinica general"));
    }

    @Test
    void registrarCreaVeterinario() {
        VeterinarioDto creado = servicio.registrar(veterinario("10001"));
        assertNotNull(creado.id());
        assertEquals("10001", creado.matricula());
        assertTrue(creado.especialidades().contains("Clinica general"));
    }

    @Test
    void registrarRechazaMatriculaDuplicada() {
        servicio.registrar(veterinario("10001"));
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(veterinario("10001")));
    }

    @Test
    void especialidadInvalidaLanzaEntidadInvalida() {
        assertThrows(EntidadInvalidaException.class, () -> servicio.registrar(
                new VeterinarioDto(null, "10001", "Ana", "Gomez", Set.of("No existe"))));
    }

    @Test
    void actualizarModificaEspecialidades() {
        VeterinarioDto creado = servicio.registrar(veterinario("10001"));

        VeterinarioDto ampliado = servicio.actualizar(creado.id(),
                new VeterinarioDto(creado.id(), "10001", "Ana", "Gomez",
                        Set.of("Clinica general", "Cardiologia")));
        assertEquals(2, ampliado.especialidades().size());

        VeterinarioDto reducido = servicio.actualizar(creado.id(),
                new VeterinarioDto(creado.id(), "10001", "Ana", "Gomez", Set.of("Clinica general")));
        assertEquals(1, reducido.especialidades().size());
    }

    @Test
    void buscarPorMatriculaDevuelveElVeterinario() {
        VeterinarioDto creado = servicio.registrar(veterinario("10001"));
        VeterinarioDto encontrado = servicio.buscarPorMatricula("10001");
        assertNotNull(encontrado);
        assertEquals(creado.id(), encontrado.id());
        assertNull(servicio.buscarPorMatricula("99999"));
    }

    @Test
    void eliminarVeterinarioSinTurnos() {
        VeterinarioDto creado = servicio.registrar(veterinario("10001"));
        servicio.eliminar(creado.id());
        assertThrows(EntidadNoEncontradaException.class, () -> servicio.buscar(creado.id()));
    }

    @Test
    void eliminarVeterinarioConTurnosActivosLanzaRegla() {
        VeterinarioDto vet = servicio.registrar(veterinario("10001"));
        ClienteService clientes = new ClienteService();
        MascotaService mascotas = new MascotaService();
        TurnoService turnos = new TurnoService();
        Long clienteId = clientes.registrar(new ClienteDto(null, "30000001", "Juan", "Perez",
                "1144556677", "juan@mail.com", null)).id();
        Long mascotaId = mascotas.registrar(clienteId, Especie.PERRO, "Rex", "Labrador",
                LocalDate.now().minusYears(3)).id();
        Long consultaId = new ServicioService()
                .crearConsultaMedica("Consulta general", new java.math.BigDecimal("15000"), 30).id();

        Long turnoId = turnos.crearTurno(mascotaId, vet.id(),
                LocalDate.now().plusDays(3).atTime(10, 0), List.of(consultaId)).id();
        turnos.confirmar(turnoId);

        assertThrows(ReglaNegocioException.class, () -> servicio.eliminar(vet.id()));
    }
}
