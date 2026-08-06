package com.veterinaria.entity;

/**
 * Especialidades que puede tener un veterinario (al menos una).
 */
public enum Especialidad {

    CLINICA_GENERAL("Clinica general"),
    CARDIOLOGIA("Cardiologia"),
    DERMATOLOGIA("Dermatologia"),
    ODONTOLOGIA("Odontologia"),
    TRAUMATOLOGIA("Traumatologia"),
    OFTALMOLOGIA("Oftalmologia"),
    CIRUGIA("Cirugia"),
    NUTRICION("Nutricion"),
    COMPORTAMIENTO("Comportamiento animal");

    private final String etiqueta;

    Especialidad(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Busca una especialidad por su etiqueta descriptiva (sin distinguir mayusculas).
     *
     * @return la especialidad o {@code null} si no existe
     */
    public static Especialidad porEtiqueta(String etiqueta) {
        if (etiqueta == null) {
            return null;
        }
        for (Especialidad especialidad : values()) {
            if (especialidad.etiqueta.equalsIgnoreCase(etiqueta.trim())) {
                return especialidad;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
