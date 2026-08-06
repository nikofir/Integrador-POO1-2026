package com.veterinaria.service;

import com.veterinaria.dto.ClienteDto;
import com.veterinaria.entity.Cliente;
import com.veterinaria.exception.EntidadNoEncontradaException;
import com.veterinaria.exception.ReglaNegocioException;
import com.veterinaria.repository.ClienteRepository;
import com.veterinaria.util.JpaUtil;

import java.util.List;

/**
 * Casos de uso sobre clientes: alta, consulta, modificacion y baja,
 * coordinando las reglas de negocio y la persistencia.
 */
public class ClienteService {

    private final ClienteRepository repositorio = new ClienteRepository();

    /**
     * Da de alta un cliente validando que el DNI sea unico.
     *
     * @throws ReglaNegocioException si ya existe un cliente con el mismo DNI
     */
    public ClienteDto registrar(ClienteDto datos) {
        return JpaUtil.enTransaccion(em -> {
            if (repositorio.buscarPorDni(em, datos.dni()) != null) {
                throw new ReglaNegocioException("Ya existe un cliente con el DNI " + datos.dni() + ".");
            }
            Cliente cliente = Cliente.crear(datos.dni(), datos.nombre(), datos.apellido(),
                    datos.telefono(), datos.email(), datos.domicilio());
            return ClienteDto.desde(repositorio.guardar(cliente, em));
        });
    }

    /**
     * Actualiza los datos editables de un cliente. El DNI es inmutable.
     *
     * @throws ReglaNegocioException si se intenta modificar el DNI
     */
    public ClienteDto actualizar(Long id, ClienteDto datos) {
        return JpaUtil.enTransaccion(em -> {
            Cliente cliente = obtener(id, em);
            if (!cliente.getDni().equals(datos.dni())) {
                throw new ReglaNegocioException("El DNI de un cliente no se puede modificar.");
            }
            cliente.actualizarDatos(datos.nombre(), datos.apellido(), datos.telefono(),
                    datos.email(), datos.domicilio());
            return ClienteDto.desde(repositorio.guardar(cliente, em));
        });
    }

    public ClienteDto buscar(Long id) {
        return JpaUtil.enTransaccion(em -> ClienteDto.desde(obtener(id, em)));
    }

    /**
     * Busca por DNI. Devuelve {@code null} si no existe.
     */
    public ClienteDto buscarPorDni(String dni) {
        return JpaUtil.enTransaccion(em -> {
            Cliente cliente = repositorio.buscarPorDni(em, dni);
            return cliente == null ? null : ClienteDto.desde(cliente);
        });
    }

    public List<ClienteDto> listar() {
        return JpaUtil.enTransaccion(em ->
                repositorio.listarTodosConMascotas(em).stream().map(ClienteDto::desde).toList());
    }

    /**
     * Elimina un cliente. No se permite eliminar clientes con mascotas.
     *
     * @throws ReglaNegocioException si el cliente tiene mascotas registradas
     */
    public void eliminar(Long id) {
        JpaUtil.enTransaccion(em -> {
            Cliente cliente = obtener(id, em);
            if (!cliente.getMascotas().isEmpty()) {
                throw new ReglaNegocioException(
                        "No se puede eliminar un cliente que tiene mascotas registradas.");
            }
            repositorio.eliminar(cliente, em);
            return null;
        });
    }

    private Cliente obtener(Long id, jakarta.persistence.EntityManager em) {
        return repositorio.buscarPorId(id, em)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el cliente con id " + id + "."));
    }
}
