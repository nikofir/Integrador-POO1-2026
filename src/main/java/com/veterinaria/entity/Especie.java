package com.veterinaria.entity;

/**
 * Especies soportadas por la veterinaria.
 */
public enum Especie {

    PERRO("Perro"),
    GATO("Gato"),
    AVE("Ave"),
    ROEDOR("Roedor"),
    REPTIL("Reptil"),
    OTRO("Otro");

    private final String etiqueta;

    Especie(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Busca una especie por su etiqueta descriptiva (sin distinguir mayusculas).
     *
     * @return la especie o {@code null} si no existe
     */
    public static Especie porEtiqueta(String etiqueta) {
        if (etiqueta == null) {
            return null;
        }
        for (Especie especie : values()) {
            if (especie.etiqueta.equalsIgnoreCase(etiqueta.trim())) {
                return especie;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
