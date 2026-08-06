package com.veterinaria.dto;

import com.veterinaria.entity.Veterinario;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO de datos de un veterinario, inmutable, con mapeo desde la entidad.
 */
public record VeterinarioDto(Long id, String matricula, String nombre, String apellido,
                             Set<String> especialidades) {

    public static VeterinarioDto desde(Veterinario veterinario) {
        Set<String> especialidades = veterinario.getEspecialidades().stream()
                .map(e -> e.getEtiqueta())
                .collect(Collectors.toSet());
        return new VeterinarioDto(veterinario.getId(), veterinario.getMatricula(), veterinario.getNombre(),
                veterinario.getApellido(), especialidades);
    }
}
