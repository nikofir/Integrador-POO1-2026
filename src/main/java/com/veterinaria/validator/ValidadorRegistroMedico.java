package com.veterinaria.validator;

/**
 * Reglas de validacion del registro medico de un turno-servicio.
 */
public final class ValidadorRegistroMedico {

    private static final int MAX_TEXTO = 500;

    private ValidadorRegistroMedico() {
    }

    /**
     * Valida los campos del registro medico.
     *
     * @return el diagnostico normalizado
     */
    public static String validar(String diagnostico, String tratamiento) {
        String diagnosticoLimpio = Validadores.longitud(diagnostico, MAX_TEXTO, "Diagnostico");
        Validadores.longitud(tratamiento, MAX_TEXTO, "Tratamiento");
        return diagnosticoLimpio;
    }
}
