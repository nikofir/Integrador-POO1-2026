package com.veterinaria.service;

import com.veterinaria.dto.ClienteDto;
import com.veterinaria.exception.EntidadNoEncontradaException;
import com.veterinaria.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClienteServiceTest extends ServiceTestBase {

    private final ClienteService servicio = new ClienteService();

    private ClienteDto cliente(String dni) {
        return new ClienteDto(null, dni, "Juan", "Perez", "1144556677", "juan@mail.com", "Av. Siempre Viva");
    }

    @Test
    void registrarCreaCliente() {
        ClienteDto creado = servicio.registrar(cliente("30000001"));
        assertNotNull(creado.id());
        assertEquals("30000001", creado.dni());
        assertEquals("Juan", creado.nombre());
        assertEquals("Perez", creado.apellido());
    }

    @Test
    void registrarRechazaDniDuplicado() {
        servicio.registrar(cliente("30000001"));
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(cliente("30000001")));
    }

    @Test
    void buscarPorDniDevuelveElCliente() {
        ClienteDto creado = servicio.registrar(cliente("30000001"));
        ClienteDto encontrado = servicio.buscarPorDni("30000001");
        assertNotNull(encontrado);
        assertEquals(creado.id(), encontrado.id());
        assertNull(servicio.buscarPorDni("99999999"));
    }

    @Test
    void actualizarModificaLosDatosEditables() {
        ClienteDto creado = servicio.registrar(cliente("30000001"));
        ClienteDto actualizado = servicio.actualizar(creado.id(),
                new ClienteDto(creado.id(), "30000001", "Juan", "Perez Lopez",
                        "1144556677", "juan@mail.com", "Otra 123"));
        assertEquals("Perez Lopez", actualizado.apellido());
        assertEquals("Otra 123", actualizado.domicilio());
    }

    @Test
    void actualizarNoPermiteCambiarElDni() {
        ClienteDto creado = servicio.registrar(cliente("30000001"));
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar(creado.id(),
                new ClienteDto(creado.id(), "30000002", "Juan", "Perez",
                        "1144556677", "juan@mail.com", null)));
    }

    @Test
    void eliminarClienteSinMascotas() {
        ClienteDto creado = servicio.registrar(cliente("30000001"));
        servicio.eliminar(creado.id());
        assertThrows(EntidadNoEncontradaException.class, () -> servicio.buscar(creado.id()));
    }

    @Test
    void eliminarClienteConMascotasLanzaRegla() {
        Long clienteId = servicio.registrar(cliente("30000001")).id();
        MascotaService mascotaService = new MascotaService();
        mascotaService.registrar(clienteId, com.veterinaria.entity.Especie.PERRO, "Rex", "Labrador",
                java.time.LocalDate.now().minusYears(3));
        assertThrows(ReglaNegocioException.class, () -> servicio.eliminar(clienteId));
    }
}
