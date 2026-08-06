package com.veterinaria.exception;

/**
 * Se lanza cuando una entidad buscada por identificador u otro
 * atributo unico no existe en la base de datos.
 */
public class EntidadNoEncontradaException extends RuntimeException {

    public EntidadNoEncontradaException(String mensaje) {
        super(mensaje);
    }

    public EntidadNoEncontradaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
