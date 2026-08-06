package com.veterinaria.persistencia;

import com.veterinaria.entity.Cliente;
import com.veterinaria.entity.ConsultaMedica;
import com.veterinaria.entity.Especie;
import com.veterinaria.entity.Especialidad;
import com.veterinaria.entity.Mascota;
import com.veterinaria.entity.Vacuna;
import com.veterinaria.entity.Veterinario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;

/**
 * Fabrica de entidades para pruebas (datos siempre validos).
 */
public final class DatosPrueba {

    private DatosPrueba() {
    }

    public static Cliente cliente(int n) {
        return Cliente.crear(String.format("%08d", 30000000 + n), "Juan", "Perez",
                "1144556677", "juan" + n + "@mail.com", null);
    }

    public static Mascota mascota(Cliente cliente, int n) {
        return Mascota.crear(cliente, Especie.PERRO, "Rex", "Labrador",
                LocalDate.now().minusYears(3), String.format("M-2026-%04d", n));
    }

    public static Veterinario veterinario(int n) {
        return Veterinario.crear(String.valueOf(10000 + n), "Ana", "Gomez",
                EnumSet.of(Especialidad.CLINICA_GENERAL));
    }

    public static ConsultaMedica consultaMedica() {
        return ConsultaMedica.crear("Consulta general", new BigDecimal("15000.00"), 30);
    }

    public static Vacuna vacuna(int n) {
        return Vacuna.crear("Vacuna " + n, "Enfermedad " + n, 12);
    }
}
