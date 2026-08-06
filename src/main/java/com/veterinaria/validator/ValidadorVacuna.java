package com.veterinaria.validator;

/**
 * Reglas de validacion de una vacuna.
 */
public final class ValidadorVacuna {

    private static final int MAX_TEXTO = 100;

    private ValidadorVacuna() {
    }

    /**
     * Valida los datos de una vacuna.
     *
     * @return el nombre comercial normalizado
     */
    public static String validar(String nombreComercial, String enfermedadPrevenida, int periodicidadMeses) {
        String nombre = Validadores.longitud(nombreComercial, MAX_TEXTO, "Nombre comercial");
        Validadores.longitud(enfermedadPrevenida, MAX_TEXTO, "Enfermedad que previene");
        if (periodicidadMeses <= 0) {
            throw new com.veterinaria.exception.ValidacionException(
                    "La periodicidad debe ser mayor a cero meses.");
        }
        return nombre;
    }
}
