package com.veterinaria.validator;

import java.math.BigDecimal;

/**
 * Reglas de validacion de un servicio veterinario (abstracto y subclases).
 */
public final class ValidadorServicio {

    private static final int MAX_NOMBRE = 100;
    private static final int MAX_CUPO = 100;

    private ValidadorServicio() {
    }

    /**
     * Valida los datos comunes a todo servicio.
     *
     * @return el nombre normalizado
     */
    public static String validar(String nombre, BigDecimal precioBase, int duracionMinutos) {
        String nombreLimpio = Validadores.longitud(nombre, MAX_NOMBRE, "Nombre del servicio");
        Validadores.noNulo(precioBase, "Precio base");
        if (precioBase.signum() <= 0) {
            throw new com.veterinaria.exception.ValidacionException("El precio base debe ser mayor a cero.");
        }
        if (duracionMinutos <= 0) {
            throw new com.veterinaria.exception.ValidacionException("La duracion debe ser mayor a cero minutos.");
        }
        return nombreLimpio;
    }

    /** Valida el cupo diario de la guarderia (maximo {@value #MAX_CUPO}). */
    public static void validarCupo(int cupoMaximo) {
        if (cupoMaximo <= 0 || cupoMaximo > MAX_CUPO) {
            throw new com.veterinaria.exception.ValidacionException(
                    "El cupo diario de la guarderia debe estar entre 1 y " + MAX_CUPO + ".");
        }
    }
}
