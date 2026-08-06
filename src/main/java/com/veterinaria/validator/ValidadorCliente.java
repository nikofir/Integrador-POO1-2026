package com.veterinaria.validator;

import com.veterinaria.exception.ValidacionException;

/**
 * Reglas de validacion de un cliente.
 */
public final class ValidadorCliente {

    private static final int MIN_DNI = 6;
    private static final int MAX_DNI = 8;
    private static final int MIN_TELEFONO = 6;
    private static final int MAX_TELEFONO = 15;

    private ValidadorCliente() {
    }

    /**
     * Valida los datos obligatorios de un cliente.
     *
     * @return el email normalizado
     */
    public static String validar(String dni, String nombre, String apellido,
                                 String telefono, String email) {
        Validadores.rango(dni, MIN_DNI, MAX_DNI, "DNI");
        Validadores.nombre(nombre, "Nombre");
        Validadores.nombre(apellido, "Apellido");
        Validadores.rango(telefono, MIN_TELEFONO, MAX_TELEFONO, "Telefono");
        return Validadores.email(email, "Email");
    }

    /** Valida un domicilio opcional (si se informa, solo longitud). */
    public static void validarDomicilioOpcional(String domicilio) {
        if (domicilio != null && !domicilio.isBlank()) {
            Validadores.longitud(domicilio, 120, "Domicilio");
        }
    }
}
