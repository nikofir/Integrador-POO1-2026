package com.veterinaria.exception;

/**
 * Indica que se intento construir una entidad con un estado invalido.
 * Las entidades del modelo de dominio se crean mediante metodos de
 * fabrica que validan todos sus invariantes antes de instanciarse.
 */
public class EntidadInvalidaException extends ValidacionException {

    public EntidadInvalidaException(String mensaje) {
        super(mensaje);
    }

    public EntidadInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
