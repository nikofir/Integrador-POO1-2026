package com.veterinaria.entity;

import com.veterinaria.exception.EntidadInvalidaException;
import com.veterinaria.exception.ValidacionException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteTest {

    private Cliente clienteValido() {
        return Cliente.crear("30123456", "Juan", "Perez",
                "1144556677", "juan@mail.com", "Av. Siempre Viva 123");
    }

    private Mascota mascotaDe(Cliente cliente) {
        Mascota mascota = Mascota.crear(cliente, Especie.PERRO, "Rex", "Labrador",
                LocalDate.now().minusYears(3), "M-2026-0001");
        cliente.agregarMascota(mascota);
        return mascota;
    }

    @Test
    void creaClienteConDatosValidos() {
        Cliente cliente = clienteValido();
        assertEquals("30123456", cliente.getDni());
        assertEquals("Juan", cliente.getNombre());
        assertEquals("Perez", cliente.getApellido());
        assertEquals("Juan Perez", cliente.getNombreCompleto());
        assertTrue(cliente.getMascotas().isEmpty());
    }

    @Test
    void dniConLetrasLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Cliente.crear("30A23456", "Juan", "Perez", "1144556677", "juan@mail.com", null));
    }

    @Test
    void dniConLongitudIncorrectaLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Cliente.crear("12345", "Juan", "Perez", "1144556677", "juan@mail.com", null));
    }

    @Test
    void nombreVacioLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Cliente.crear("30123456", "", "Perez", "1144556677", "juan@mail.com", null));
    }

    @Test
    void nombreConNumerosLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Cliente.crear("30123456", "Juan123", "Perez", "1144556677", "juan@mail.com", null));
    }

    @Test
    void apellidoNullLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Cliente.crear("30123456", "Juan", null, "1144556677", "juan@mail.com", null));
    }

    @Test
    void telefonoConLetrasLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Cliente.crear("30123456", "Juan", "Perez", "11AB556677", "juan@mail.com", null));
    }

    @Test
    void emailInvalidoLanzaExcepcion() {
        assertThrows(ValidacionException.class,
                () -> Cliente.crear("30123456", "Juan", "Perez", "1144556677", "juan@", null));
    }

    @Test
    void agregarMascotaNullLanzaExcepcion() {
        Cliente cliente = clienteValido();
        assertThrows(EntidadInvalidaException.class, () -> cliente.agregarMascota(null));
    }

    @Test
    void agregarMascotaDeOtroClienteLanzaExcepcion() {
        Cliente cliente1 = clienteValido();
        Cliente cliente2 = Cliente.crear("27123456", "Maria", "Lopez", "1155667788", "maria@mail.com", null);
        Mascota mascota = Mascota.crear(cliente2, Especie.GATO, "Michi", "Siames",
                LocalDate.now().minusYears(1), "M-2026-0002");

        assertThrows(EntidadInvalidaException.class, () -> cliente1.agregarMascota(mascota));
    }

    @Test
    void agregarMascotaRepetidaLanzaExcepcion() {
        Cliente cliente = clienteValido();
        Mascota mascota = mascotaDe(cliente);

        assertThrows(EntidadInvalidaException.class, () -> cliente.agregarMascota(mascota));
    }

    @Test
    void removerMascotaQueNoPerteneceLanzaExcepcion() {
        Cliente cliente1 = clienteValido();
        Cliente cliente2 = Cliente.crear("27123456", "Maria", "Lopez", "1155667788", "maria@mail.com", null);
        Mascota mascota = mascotaDe(cliente2);

        assertThrows(EntidadInvalidaException.class, () -> cliente1.removerMascota(mascota));
    }

    @Test
    void agregarYRemoverMascotaActualizaComposicion() {
        Cliente cliente = clienteValido();
        mascotaDe(cliente);
        assertEquals(1, cliente.getMascotas().size());

        Mascota aRemover = cliente.getMascotas().get(0);
        cliente.removerMascota(aRemover);
        assertTrue(cliente.getMascotas().isEmpty());
    }
}
