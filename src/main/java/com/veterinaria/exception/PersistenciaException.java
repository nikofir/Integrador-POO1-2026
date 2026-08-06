package com.veterinaria.exception;

/**
 * Error de la capa de persistencia (JPA / base de datos).
 * No es recuperable a nivel de dominio; suele indicar un
 * problema de configuracion o infraestructura.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
