package com.veterinaria.exception;

/**
 * Violacion de una regla de negocio (transicion de estado ilegal,
 * turnos solapados, cancelacion fuera de termino, cupo completo,
 * vacuna vigente, etc.).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
