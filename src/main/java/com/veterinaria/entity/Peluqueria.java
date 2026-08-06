package com.veterinaria.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

/**
 * Servicio de peluqueria y estetica de mascotas.
 */
@Entity
@DiscriminatorValue("PELUQUERIA")
public class Peluqueria extends Servicio {

    protected Peluqueria() {
        // requerido por JPA
    }

    /**
     * Metodo de fabrica.
     */
    public static Peluqueria crear(String nombre, BigDecimal precioBase, int duracionMinutos) {
        return new Peluqueria(nombre, precioBase, duracionMinutos);
    }

    private Peluqueria(String nombre, BigDecimal precioBase, int duracionMinutos) {
        super(nombre, precioBase, duracionMinutos);
    }

    @Override
    public String getTipo() {
        return "Peluqueria";
    }
}
