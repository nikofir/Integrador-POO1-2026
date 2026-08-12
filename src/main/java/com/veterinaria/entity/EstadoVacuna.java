package com.veterinaria.entity;

/**
 * Estado de vigencia de una dosis de vacuna aplicada a una mascota,
 * usado por el sistema de alertas de vacunacion.
 */
public enum EstadoVacuna {

    VENCIDA("Vencida"),
    PROXIMA_A_VENCER("Vence en breve"),
    AL_DIA("Al dia");

    private final String etiqueta;

    EstadoVacuna(String etiqueta) {
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
