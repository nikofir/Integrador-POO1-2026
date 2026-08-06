package com.veterinaria.persistencia;

import com.veterinaria.entity.Cliente;
import com.veterinaria.entity.Mascota;
import com.veterinaria.repository.ClienteRepository;
import com.veterinaria.repository.MascotaRepository;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteRepositoryTest extends PersistenciaBaseTest {

    @Override
    protected String nombreBase() {
        return "clientes_test";
    }

    private final ClienteRepository repositorio = new ClienteRepository();
    private final MascotaRepository repositorioMascotas = new MascotaRepository();

    @Test
    void guardaYRecuperaClientePorDni() {
        Cliente guardado = enTransaccion(em -> repositorio.guardar(DatosPrueba.cliente(1), em));

        assertNotNull(guardado.getId());
        Cliente recuperado = enTransaccion(em -> repositorio.buscarPorDni(em, "30000001"));
        assertNotNull(recuperado);
        assertEquals(guardado.getId(), recuperado.getId());
        assertEquals("Juan", recuperado.getNombre());
    }

    @Test
    void dniDuplicadoLanzaErrorDePersistencia() {
        enTransaccion(em -> repositorio.guardar(DatosPrueba.cliente(1), em));

        assertThrows(PersistenceException.class,
                () -> enTransaccion(em -> repositorio.guardar(DatosPrueba.cliente(1), em)));
    }

    @Test
    void listaClientesConMascotas() {
        Cliente cliente = DatosPrueba.cliente(1);
        Mascota mascota1 = DatosPrueba.mascota(cliente, 1);
        Mascota mascota2 = DatosPrueba.mascota(cliente, 2);
        cliente.agregarMascota(mascota1);
        cliente.agregarMascota(mascota2);
        enTransaccion(em -> {
            repositorio.guardar(cliente, em);
            return null;
        });

        Cliente recuperado = enTransaccion(em -> repositorio.listarTodosConMascotas(em).get(0));
        assertEquals(2, recuperado.getMascotas().size());
    }

    @Test
    void eliminarClienteEliminaSusMascotasEnComposicion() {
        Cliente cliente = DatosPrueba.cliente(1);
        cliente.agregarMascota(DatosPrueba.mascota(cliente, 1));
        enTransaccion(em -> {
            repositorio.guardar(cliente, em);
            return null;
        });

        enTransaccion(em -> {
            Cliente persistido = repositorio.buscarPorDni(em, "30000001");
            repositorio.eliminar(persistido, em);
            return null;
        });

        boolean sinMascotas = enTransaccion(em -> repositorioMascotas.listarTodos(em).isEmpty());
        assertTrue(sinMascotas);
    }
}
