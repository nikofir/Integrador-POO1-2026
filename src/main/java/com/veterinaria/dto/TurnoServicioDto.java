package com.veterinaria.dto;

import com.veterinaria.entity.RegistroMedico;
import com.veterinaria.entity.TurnoServicio;

import java.math.BigDecimal;

/**
 * DTO de un servicio dentro de un turno, con el precio historico congelado
 * al momento de la creacion del turno. Si el servicio genero un registro
 * medico (por ejemplo, una consulta), este se incluye de forma opcional.
 */
public record TurnoServicioDto(Long id, Long servicioId, String servicioNombre, String servicioTipo,
                               BigDecimal precioHistorico, boolean tieneRegistroMedico,
                               RegistroMedicoDto registroMedico) {

    public static TurnoServicioDto desde(TurnoServicio detalle) {
        RegistroMedico registro = detalle.getRegistroMedico();
        return new TurnoServicioDto(detalle.getId(), detalle.getServicio().getId(),
                detalle.getServicio().getNombre(), detalle.getServicio().getTipo(),
                detalle.getPrecioHistorico(), detalle.tieneRegistroMedico(),
                registro == null ? null : RegistroMedicoDto.desde(registro));
    }
}
