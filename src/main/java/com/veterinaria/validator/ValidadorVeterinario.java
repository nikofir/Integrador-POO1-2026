package com.veterinaria.validator;

import com.veterinaria.entity.Especialidad;

import java.util.Collection;

/**
 * Reglas de validacion de un veterinario.
 */
public final class ValidadorVeterinario {

    private static final int MIN_MATRICULA = 1;
    private static final int MAX_MATRICULA = 10;

    private ValidadorVeterinario() {
    }

    /**
     * Valida los datos obligatorios de un veterinario.
     *
     * @return la matricula normalizada
     */
    public static String validar(String matricula, String nombre, String apellido,
                                 Collection<Especialidad> especialidades) {
        String matriculaLimpia = Validadores.rango(matricula, MIN_MATRICULA, MAX_MATRICULA, "Matricula");
        Validadores.nombre(nombre, "Nombre");
        Validadores.nombre(apellido, "Apellido");
        Validadores.coleccionValida(especialidades, "Especialidades");
        return matriculaLimpia;
    }

    /**
     * Valida unicamente el nombre y apellido (para una actualizacion de datos).
     */
    public static void validarNombreApellido(String nombre, String apellido) {
        Validadores.nombre(nombre, "Nombre");
        Validadores.nombre(apellido, "Apellido");
    }
}
