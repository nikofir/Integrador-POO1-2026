package com.veterinaria.entity;

/**
 * Estados por los que transita un turno.
 * <p>
 * Maquina de estados:
 * PENDIENTE -&gt; CONFIRMADO / CANCELADO
 * CONFIRMADO -&gt; ATENDIDO / CANCELADO (este ultimo solo con mas de 24 hs
 * de anticipacion)
 * ATENDIDO y CANCELADO son estados terminales.
 */
public enum EstadoTurno {

    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    ATENDIDO("Atendido"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoTurno(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
