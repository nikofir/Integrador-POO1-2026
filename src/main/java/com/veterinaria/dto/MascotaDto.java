package com.veterinaria.dto;

import com.veterinaria.entity.Cliente;
import com.veterinaria.entity.Mascota;

import java.time.LocalDate;

/**
 * DTO de datos de una mascota, inmutable, con mapeo desde la entidad.
 */
public record MascotaDto(Long id, String ficha, String especie, String nombre, String raza,
                         LocalDate fechaNacimiento, boolean activa, long edadAnios,
                         Long clienteId, String clienteNombreCompleto) {

    public static MascotaDto desde(Mascota mascota) {
        Cliente cliente = mascota.getCliente();
        return new MascotaDto(mascota.getId(), mascota.getFicha(), mascota.getEspecie().getEtiqueta(),
                mascota.getNombre(), mascota.getRaza(), mascota.getFechaNacimiento(), mascota.isActiva(),
                mascota.getEdadAnios(), cliente.getId(), cliente.getNombreCompleto());
    }
}
