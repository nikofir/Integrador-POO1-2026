package com.veterinaria.validator;

import com.veterinaria.entity.Cliente;
import com.veterinaria.entity.Especie;

import java.time.LocalDate;

/**
 * Reglas de validacion de una mascota.
 */
public final class ValidadorMascota {

    private ValidadorMascota() {
    }

    /**
     * Valida todos los datos de una mascota.
     *
     * @return el nombre normalizado
     */
    public static String validar(Cliente cliente, Especie especie, String nombre,
                                 String raza, LocalDate fechaNacimiento, String ficha) {
        Validadores.noNulo(cliente, "Cliente");
        Validadores.noNulo(especie, "Especie");
        Validadores.nombre(nombre, "Nombre de la mascota");
        Validadores.longitud(raza, 60, "Raza");
        Validadores.noFutura(fechaNacimiento, "Fecha de nacimiento");
        Validadores.ficha(ficha, "Ficha");
        return nombre;
    }

    /**
     * Valida los datos editables de una mascota (sin cliente ni ficha).
     */
    public static void validarDatos(Especie especie, String nombre, String raza, LocalDate fechaNacimiento) {
        Validadores.noNulo(especie, "Especie");
        Validadores.nombre(nombre, "Nombre de la mascota");
        Validadores.longitud(raza, 60, "Raza");
        Validadores.noFutura(fechaNacimiento, "Fecha de nacimiento");
    }
}
