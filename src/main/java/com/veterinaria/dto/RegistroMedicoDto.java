package com.veterinaria.dto;

import com.veterinaria.entity.RegistroMedico;

import java.time.LocalDateTime;

/**
 * DTO de un registro medico asociado a un servicio de un turno atendido.
 */
public record RegistroMedicoDto(Long id, String diagnostico, String tratamiento, LocalDateTime fecha) {

    public static RegistroMedicoDto desde(RegistroMedico registro) {
        return new RegistroMedicoDto(registro.getId(), registro.getDiagnostico(), registro.getTratamiento(),
                registro.getFecha());
    }
}
