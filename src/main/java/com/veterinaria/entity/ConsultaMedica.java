package com.veterinaria.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

/**
 * Consulta medica veterinaria. Sin atributos adicionales.
 */
@Entity
@DiscriminatorValue("CONSULTA_MEDICA")
public class ConsultaMedica extends Servicio {

    protected ConsultaMedica() {
        // requerido por JPA
    }

    /**
     * Metodo de fabrica.
     */
    public static ConsultaMedica crear(String nombre, BigDecimal precioBase, int duracionMinutos) {
        return new ConsultaMedica(nombre, precioBase, duracionMinutos);
    }

    private ConsultaMedica(String nombre, BigDecimal precioBase, int duracionMinutos) {
        super(nombre, precioBase, duracionMinutos);
    }

    @Override
    public String getTipo() {
        return "Consulta medica";
    }
}
