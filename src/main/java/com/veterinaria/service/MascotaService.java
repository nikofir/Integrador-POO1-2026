package com.veterinaria.service;

import com.veterinaria.dto.MascotaDto;
import com.veterinaria.entity.Cliente;
import com.veterinaria.entity.Especie;
import com.veterinaria.entity.Mascota;
import com.veterinaria.exception.EntidadNoEncontradaException;
import com.veterinaria.repository.ClienteRepository;
import com.veterinaria.repository.MascotaRepository;
import com.veterinaria.util.JpaUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Casos de uso sobre mascotas: alta con ficha generada por el sistema,
 * consulta, modificacion y baja logica (activa/inactiva).
 */
public class MascotaService {

    private final MascotaRepository repositorio = new MascotaRepository();
    private final ClienteRepository repositorioClientes = new ClienteRepository();

    /**
     * Da de alta una mascota para un cliente, generando la ficha unica
     * con formato {@code M-AAAA-NNNN}.
     *
     * @throws EntidadNoEncontradaException si el cliente no existe
     */
    public MascotaDto registrar(Long clienteId, Especie especie, String nombre, String raza,
                                LocalDate fechaNacimiento) {
        return JpaUtil.enTransaccion(em -> {
            Cliente cliente = repositorioClientes.buscarPorId(clienteId, em)
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "No existe el cliente con id " + clienteId + "."));
            Mascota mascota = Mascota.crear(cliente, especie, nombre, raza, fechaNacimiento,
                    generarFicha(em));
            cliente.agregarMascota(mascota);
            return MascotaDto.desde(repositorio.guardar(mascota, em));
        });
    }

    /**
     * Actualiza los datos editables de una mascota (la ficha es inmutable).
     */
    public MascotaDto actualizar(Long id, Especie especie, String nombre, String raza,
                                 LocalDate fechaNacimiento) {
        return JpaUtil.enTransaccion(em -> {
            Mascota mascota = obtener(id, em);
            mascota.actualizarDatos(especie, nombre, raza, fechaNacimiento);
            return MascotaDto.desde(repositorio.guardar(mascota, em));
        });
    }

    public MascotaDto buscar(Long id) {
        return JpaUtil.enTransaccion(em -> MascotaDto.desde(obtener(id, em)));
    }

    /**
     * Busca por ficha.
     *
     * @return {@code Optional} con la mascota si existe, vacio en caso contrario
     */
    public Optional<MascotaDto> buscarPorFicha(String ficha) {
        return JpaUtil.enTransaccion(em ->
                repositorio.buscarPorFicha(em, ficha).map(MascotaDto::desde));
    }

    public List<MascotaDto> listarActivas() {
        return JpaUtil.enTransaccion(em ->
                repositorio.listarActivas(em).stream().map(MascotaDto::desde).toList());
    }

    public List<MascotaDto> listarPorCliente(Long clienteId) {
        return JpaUtil.enTransaccion(em ->
                repositorio.listarPorCliente(em, clienteId).stream().map(MascotaDto::desde).toList());
    }

    /** Lista todas las mascotas (activas e inactivas), para el historial medico. */
    public List<MascotaDto> listarTodas() {
        return JpaUtil.enTransaccion(em ->
                repositorio.listarTodas(em).stream().map(MascotaDto::desde).toList());
    }

    /** Baja logica: la mascota deja de poder tomar turnos. */
    public void marcarInactiva(Long id) {
        JpaUtil.enTransaccion(em -> {
            obtener(id, em).marcarInactiva();
            return null;
        });
    }

    /** Reactiva la mascota. */
    public void reactivar(Long id) {
        JpaUtil.enTransaccion(em -> {
            obtener(id, em).reactivar();
            return null;
        });
    }

    private Mascota obtener(Long id, jakarta.persistence.EntityManager em) {
        return repositorio.buscarPorId(id, em)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe la mascota con id " + id + "."));
    }

    private String generarFicha(jakarta.persistence.EntityManager em) {
        String prefijo = "M-" + LocalDate.now().getYear() + "-";
        String ultima = repositorio.buscarUltimaFicha(em, prefijo);
        int numero = ultima == null ? 1 : Integer.parseInt(ultima.substring(prefijo.length())) + 1;
        return prefijo + String.format("%04d", numero);
    }
}
