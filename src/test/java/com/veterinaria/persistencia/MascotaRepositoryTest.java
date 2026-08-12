package com.veterinaria.persistencia;

import com.veterinaria.entity.Cliente;
import com.veterinaria.entity.Mascota;
import com.veterinaria.repository.ClienteRepository;
import com.veterinaria.repository.MascotaRepository;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MascotaRepositoryTest extends PersistenciaBaseTest {

    @Override
    protected String nombreBase() {
        return "mascotas_test";
    }

    private final ClienteRepository repositorioClientes = new ClienteRepository();
    private final MascotaRepository repositorio = new MascotaRepository();

    private Cliente guardarCliente(int n) {
        return enTransaccion(em -> repositorioClientes.guardar(DatosPrueba.cliente(n), em));
    }

    @Test
    void guardaYRecuperaMascotaPorFicha() {
        Cliente cliente = guardarCliente(1);
        Mascota mascota = DatosPrueba.mascota(cliente, 1);
        cliente.agregarMascota(mascota);
        enTransaccion(em -> {
            repositorioClientes.guardar(cliente, em);
            return null;
        });

        Mascota recuperada = enTransaccion(em -> repositorio.buscarPorFicha(em, "M-2026-0001").orElseThrow());
        assertNotNull(recuperada);
        assertEquals("Rex", recuperada.getNombre());
        assertEquals(cliente.getId(), recuperada.getCliente().getId());
    }

    @Test
    void fichaDuplicadaLanzaErrorDePersistencia() {
        Cliente cliente1 = guardarCliente(1);
        Cliente cliente2 = guardarCliente(2);
        Mascota mascota1 = DatosPrueba.mascota(cliente1, 1);
        Mascota mascota2 = DatosPrueba.mascota(cliente2, 1);
        cliente1.agregarMascota(mascota1);
        cliente2.agregarMascota(mascota2);
        enTransaccion(em -> {
            repositorioClientes.guardar(cliente1, em);
            return null;
        });

        assertThrows(PersistenceException.class,
                () -> enTransaccion(em -> {
                    repositorioClientes.guardar(cliente2, em);
                    return null;
                }));
    }

    @Test
    void listaMascotasPorCliente() {
        Cliente cliente = guardarCliente(1);
        Mascota m1 = DatosPrueba.mascota(cliente, 1);
        Mascota m2 = DatosPrueba.mascota(cliente, 2);
        cliente.agregarMascota(m1);
        cliente.agregarMascota(m2);
        enTransaccion(em -> {
            repositorioClientes.guardar(cliente, em);
            return null;
        });

        int cantidad = enTransaccion(em -> repositorio.listarPorCliente(em, cliente.getId()).size());
        assertEquals(2, cantidad);
    }

    @Test
    void ultimaFichaPorAnio() {
        Cliente cliente = guardarCliente(1);
        Mascota m1 = DatosPrueba.mascota(cliente, 1);
        Mascota m2 = DatosPrueba.mascota(cliente, 5);
        cliente.agregarMascota(m1);
        cliente.agregarMascota(m2);
        enTransaccion(em -> {
            repositorioClientes.guardar(cliente, em);
            return null;
        });

        String ultima = enTransaccion(em -> repositorio.buscarUltimaFicha(em, "M-2026-"));
        assertEquals("M-2026-0005", ultima);
    }

    @Test
    void mascotaInactivaNoApareceEnListadoActivas() {
        Cliente cliente = guardarCliente(1);
        Mascota mascota = DatosPrueba.mascota(cliente, 1);
        mascota.marcarInactiva();
        cliente.agregarMascota(mascota);
        enTransaccion(em -> {
            repositorioClientes.guardar(cliente, em);
            return null;
        });

        boolean enActivas = enTransaccion(em -> repositorio.listarActivas(em).contains(mascota));
        assertFalse(enActivas);

        Mascota recuperada = enTransaccion(em -> repositorio.buscarPorFicha(em, "M-2026-0001").orElseThrow());
        assertFalse(recuperada.isActiva());
    }
}
