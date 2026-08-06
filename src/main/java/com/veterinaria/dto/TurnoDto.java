package com.veterinaria.dto;

import com.veterinaria.entity.Turno;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de un turno completo con sus servicios, inmutable, con mapeo desde la entidad.
 */
public record TurnoDto(Long id, Long mascotaId, String mascotaFicha, String mascotaNombre,
                       Long clienteId, String clienteNombreCompleto,
                       Long veterinarioId, String veterinarioNombreCompleto,
                       LocalDateTime fechaHora, LocalDateTime fechaCreacion, String estado,
                       BigDecimal precioTotal, int duracionTotal,
                       List<TurnoServicioDto> turnoServicios) {

    public static TurnoDto desde(Turno turno) {
        return new TurnoDto(turno.getId(),
                turno.getMascota().getId(), turno.getMascota().getFicha(), turno.getMascota().getNombre(),
                turno.getMascota().getCliente().getId(),
                turno.getMascota().getCliente().getNombreCompleto(),
                turno.getVeterinario().getId(), turno.getVeterinario().getNombreCompleto(),
                turno.getFechaHora(), turno.getFechaCreacion(), turno.getEstado().getEtiqueta(),
                turno.calcularPrecioTotal(), turno.calcularDuracionTotal(),
                turno.getTurnoServicios().stream().map(TurnoServicioDto::desde).toList());
    }
}
