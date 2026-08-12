package com.veterinaria.dto;

import com.veterinaria.entity.EstadoVacuna;

import java.time.LocalDate;

/**
 * Alerta de vacunacion: una mascota con una vacuna vencida o proxima a
 * vencer, calculada desde su ultima aplicacion en un turno atendido.
 */
public record AlertaVacunaDto(Long mascotaId, String mascotaFicha, String mascotaNombre,
                              String clienteNombre, Long vacunaId, String vacunaNombre,
                              String enfermedadPrevenida, LocalDate ultimaAplicacion,
                              LocalDate proximaAplicacion, EstadoVacuna estado, long diasRestantes) {

    public boolean esVencida() {
        return estado == EstadoVacuna.VENCIDA;
    }
}
