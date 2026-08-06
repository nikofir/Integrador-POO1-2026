package com.veterinaria.service;

import com.veterinaria.dto.ServicioDto;
import com.veterinaria.entity.AplicacionVacuna;
import com.veterinaria.entity.ConsultaMedica;
import com.veterinaria.entity.GuarderiaDia;
import com.veterinaria.entity.Peluqueria;
import com.veterinaria.entity.Servicio;
import com.veterinaria.entity.Vacuna;
import com.veterinaria.exception.EntidadNoEncontradaException;
import com.veterinaria.repository.ServicioRepository;
import com.veterinaria.repository.VacunaRepository;
import com.veterinaria.util.JpaUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * Casos de uso sobre el catalogo de servicios: alta de cada tipo, consulta,
 * modificacion de precio y listado.
 */
public class ServicioService {

    private final ServicioRepository repositorio = new ServicioRepository();
    private final VacunaRepository repositorioVacunas = new VacunaRepository();

    public ServicioDto crearConsultaMedica(String nombre, BigDecimal precioBase, int duracionMinutos) {
        return guardar(ConsultaMedica.crear(nombre, precioBase, duracionMinutos));
    }

    public ServicioDto crearPeluqueria(String nombre, BigDecimal precioBase, int duracionMinutos) {
        return guardar(Peluqueria.crear(nombre, precioBase, duracionMinutos));
    }

    public ServicioDto crearGuarderiaDia(String nombre, BigDecimal precioBase, int duracionMinutos,
                                         int cupoMaximo) {
        return guardar(GuarderiaDia.crear(nombre, precioBase, duracionMinutos, cupoMaximo));
    }

    /**
     * Crea un servicio de aplicacion de vacuna asociado a una vacuna existente.
     *
     * @throws EntidadNoEncontradaException si la vacuna no existe
     */
    public ServicioDto crearAplicacionVacuna(String nombre, BigDecimal precioBase, int duracionMinutos,
                                             Long vacunaId) {
        return JpaUtil.enTransaccion(em -> {
            Vacuna vacuna = repositorioVacunas.buscarPorId(vacunaId, em)
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "No existe la vacuna con id " + vacunaId + "."));
            return ServicioDto.desde(repositorio.guardar(
                    AplicacionVacuna.crear(nombre, precioBase, duracionMinutos, vacuna), em));
        });
    }

    /**
     * Actualiza los datos editables de un servicio (nombre, precio y duracion).
     */
    public ServicioDto actualizar(Long id, String nombre, BigDecimal precioBase, int duracionMinutos) {
        return JpaUtil.enTransaccion(em -> {
            Servicio servicio = obtener(id, em);
            servicio.actualizarDatos(nombre, precioBase, duracionMinutos);
            return ServicioDto.desde(repositorio.guardar(servicio, em));
        });
    }

    /**
     * Actualiza el cupo diario de una guarderia.
     */
    public ServicioDto actualizarCupoGuarderia(Long id, int cupoMaximo) {
        return JpaUtil.enTransaccion(em -> {
            Servicio servicio = obtener(id, em);
            if (!(servicio instanceof GuarderiaDia guarderia)) {
                throw new com.veterinaria.exception.ReglaNegocioException(
                        "El servicio no es una guarderia.");
            }
            guarderia.actualizarCupo(cupoMaximo);
            return ServicioDto.desde(repositorio.guardar(servicio, em));
        });
    }

    public ServicioDto buscar(Long id) {
        return JpaUtil.enTransaccion(em -> ServicioDto.desde(obtener(id, em)));
    }

    public List<ServicioDto> listarCatalogo() {
        return JpaUtil.enTransaccion(em ->
                repositorio.listarCatalogo(em).stream().map(ServicioDto::desde).toList());
    }

    private ServicioDto guardar(Servicio servicio) {
        return JpaUtil.enTransaccion(em -> ServicioDto.desde(repositorio.guardar(servicio, em)));
    }

    private Servicio obtener(Long id, jakarta.persistence.EntityManager em) {
        return repositorio.buscarPorId(id, em)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el servicio con id " + id + "."));
    }
}
