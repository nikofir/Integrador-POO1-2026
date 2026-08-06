package com.veterinaria.exception;

/**
 * Error de validacion de entrada de datos (formato, campos vacios,
 * letras donde se esperan numeros, etc.). Es la base de las
 * excepciones que originan los validadores.
 */
public class ValidacionException extends RuntimeException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }

    public ValidacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
