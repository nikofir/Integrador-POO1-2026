package com.veterinaria.dto;

import com.veterinaria.entity.AplicacionVacuna;
import com.veterinaria.entity.GuarderiaDia;
import com.veterinaria.entity.Servicio;

import java.math.BigDecimal;

/**
 * DTO de datos de un servicio del catalogo, inmutable, con mapeo desde la entidad.
 * Los campos {@code cupoMaximo} y {@code vacunaNombre} solo aplican a
 * {@link GuarderiaDia} y {@link AplicacionVacuna} respectivamente.
 */
public record ServicioDto(Long id, String tipo, String nombre, BigDecimal precioBase,
                          int duracionMinutos, Integer cupoMaximo, String vacunaNombre) {

    public static ServicioDto desde(Servicio servicio) {
        Integer cupo = servicio instanceof GuarderiaDia guarderia ? guarderia.getCupoMaximo() : null;
        String vacuna = servicio instanceof AplicacionVacuna aplicacion
                ? aplicacion.getVacuna().getNombreComercial() : null;
        return new ServicioDto(servicio.getId(), servicio.getTipo(), servicio.getNombre(),
                servicio.getPrecioBase(), servicio.getDuracionMinutos(), cupo, vacuna);
    }
}
