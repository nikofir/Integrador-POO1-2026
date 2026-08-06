package com.veterinaria.service;

import com.veterinaria.dto.ClienteDto;
import com.veterinaria.dto.MascotaDto;
import com.veterinaria.entity.Especie;
import com.veterinaria.exception.EntidadNoEncontradaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MascotaServiceTest extends ServiceTestBase {

    private final ClienteService clientes = new ClienteService();
    private final MascotaService servicio = new MascotaService();

    private Long crearCliente() {
        return clientes.registrar(new ClienteDto(null, "30000001", "Juan", "Perez",
                "1144556677", "juan@mail.com", null)).id();
    }

    private MascotaDto registrarMascota(Long clienteId) {
        return servicio.registrar(clienteId, Especie.PERRO, "Rex", "Labrador",
                LocalDate.now().minusYears(3));
    }

    @Test
    void registrarGeneraFichasSecuenciales() {
        Long clienteId = crearCliente();
        MascotaDto primera = registrarMascota(clienteId);
        MascotaDto segunda = registrarMascota(clienteId);

        assertEquals("M-" + LocalDate.now().getYear() + "-0001", primera.ficha());
        assertEquals("M-" + LocalDate.now().getYear() + "-0002", segunda.ficha());
        assertEquals("Rex", primera.nombre());
        assertEquals("Perro", primera.especie());
    }

    @Test
    void registrarConClienteInexistenteLanzaNoEncontrada() {
        assertThrows(EntidadNoEncontradaException.class, () ->
                servicio.registrar(999L, Especie.PERRO, "Rex", "Labrador", LocalDate.now().minusYears(3)));
    }

    @Test
    void actualizarModificaLosDatosEditables() {
        Long clienteId = crearCliente();
        MascotaDto creada = registrarMascota(clienteId);
        MascotaDto actualizada = servicio.actualizar(creada.id(), Especie.GATO, "Misi", "Siames",
                LocalDate.now().minusYears(2));

        assertEquals("Misi", actualizada.nombre());
        assertEquals("Siames", actualizada.raza());
        assertEquals("Gato", actualizada.especie());
        assertEquals(creada.ficha(), actualizada.ficha());
    }

    @Test
    void marcarInactivaExcluyeDeLasActivas() {
        Long clienteId = crearCliente();
        MascotaDto creada = registrarMascota(clienteId);

        servicio.marcarInactiva(creada.id());

        MascotaDto inactiva = servicio.buscar(creada.id());
        assertFalse(inactiva.activa());
        assertTrue(servicio.listarActivas().stream()
                .noneMatch(m -> m.id().equals(creada.id())));

        servicio.reactivar(creada.id());
        assertTrue(servicio.buscar(creada.id()).activa());
    }

    @Test
    void listarPorClienteAgrupaSusMascotas() {
        Long clienteId = crearCliente();
        Long otroClienteId = clientes.registrar(new ClienteDto(null, "30000002", "Ana", "Gomez",
                "1144556678", "ana@mail.com", null)).id();
        registrarMascota(clienteId);
        registrarMascota(clienteId);
        servicio.registrar(otroClienteId, Especie.GATO, "Misi", "Siames", LocalDate.now().minusYears(2));

        assertEquals(2, servicio.listarPorCliente(clienteId).size());
        assertEquals(1, servicio.listarPorCliente(otroClienteId).size());
    }

    @Test
    void buscarPorFichaDevuelveLaMascota() {
        Long clienteId = crearCliente();
        MascotaDto creada = registrarMascota(clienteId);

        MascotaDto encontrada = servicio.buscarPorFicha(creada.ficha());
        assertNotNull(encontrada);
        assertEquals(creada.id(), encontrada.id());
        assertEquals(clienteId, encontrada.clienteId());
    }
}
