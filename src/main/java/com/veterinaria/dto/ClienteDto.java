package com.veterinaria.dto;

import com.veterinaria.entity.Cliente;

/**
 * DTO de datos de un cliente, inmutable, con mapeo desde la entidad.
 */
public record ClienteDto(Long id, String dni, String nombre, String apellido,
                         String telefono, String email, String domicilio, int cantidadMascotas) {

    /**
     * Constructor de conveniencia para el alta, cuando el id y la cantidad
     * de mascotas aun no existen.
     */
    public ClienteDto(Long id, String dni, String nombre, String apellido,
                      String telefono, String email, String domicilio) {
        this(id, dni, nombre, apellido, telefono, email, domicilio, 0);
    }

    public static ClienteDto desde(Cliente cliente) {
        return new ClienteDto(cliente.getId(), cliente.getDni(), cliente.getNombre(), cliente.getApellido(),
                cliente.getTelefono(), cliente.getEmail(), cliente.getDomicilio(),
                cliente.getMascotas().size());
    }
}
