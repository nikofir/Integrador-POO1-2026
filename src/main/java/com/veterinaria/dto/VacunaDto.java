package com.veterinaria.dto;

import com.veterinaria.entity.Vacuna;

/**
 * DTO de datos de una vacuna, inmutable, con mapeo desde la entidad.
 */
public record VacunaDto(Long id, String nombreComercial, String enfermedadPrevenida, int periodicidadMeses) {

    public static VacunaDto desde(Vacuna vacuna) {
        return new VacunaDto(vacuna.getId(), vacuna.getNombreComercial(), vacuna.getEnfermedadPrevenida(),
                vacuna.getPeriodicidadMeses());
    }
}
