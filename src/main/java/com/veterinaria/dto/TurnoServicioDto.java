package com.veterinaria.dto;

import com.veterinaria.entity.TurnoServicio;

import java.math.BigDecimal;

/**
 * DTO de un servicio dentro de un turno, con el precio historico congelado
 * al momento de la creacion del turno.
 */
public record TurnoServicioDto(Long id, Long servicioId, String servicioNombre, String servicioTipo,
                               BigDecimal precioHistorico, boolean tieneRegistroMedico) {

    public static TurnoServicioDto desde(TurnoServicio detalle) {
        return new TurnoServicioDto(detalle.getId(), detalle.getServicio().getId(),
                detalle.getServicio().getNombre(), detalle.getServicio().getTipo(),
                detalle.getPrecioHistorico(), detalle.tieneRegistroMedico());
    }
}
