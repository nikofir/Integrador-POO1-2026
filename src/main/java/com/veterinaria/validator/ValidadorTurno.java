package com.veterinaria.validator;

import com.veterinaria.entity.Mascota;
import com.veterinaria.entity.Servicio;
import com.veterinaria.entity.Veterinario;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reglas de validacion de un turno.
 */
public final class ValidadorTurno {

    private ValidadorTurno() {
    }

    /**
     * Valida los datos de creacion de un turno.
     */
    public static void validarCreacion(Mascota mascota, Veterinario veterinario,
                                       LocalDateTime fechaHora, List<Servicio> servicios) {
        Validadores.noNulo(mascota, "Mascota");
        Validadores.noNulo(veterinario, "Veterinario");
        Validadores.futura(fechaHora, "Fecha y hora del turno");
        Validadores.coleccionValida(servicios, "Servicios del turno");

        if (!mascota.isActiva()) {
            throw new com.veterinaria.exception.ReglaNegocioException(
                    "No se pueden tomar turnos para una mascota inactiva.");
        }
        validarServiciosSinDuplicados(servicios);
    }

    private static void validarServiciosSinDuplicados(List<Servicio> servicios) {
        Set<Object> claves = new HashSet<>();
        for (Servicio servicio : servicios) {
            Object clave = servicio.getId() != null ? servicio.getId() : servicio;
            if (!claves.add(clave)) {
                throw new com.veterinaria.exception.ValidacionException(
                        "El servicio '" + servicio.getNombre() + "' esta duplicado en el turno.");
            }
        }
    }
}
