package com.veterinaria.repository;

import com.veterinaria.entity.EstadoTurno;
import com.veterinaria.entity.Turno;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de turnos, incluidas las consultas necesarias para las
 * reglas de negocio (solapamientos, cupo de guarderia y vigencia de vacunas).
 */
public class TurnoRepository extends BaseRepository<Turno, Long> {

    private static final long MARGEN_DIAS_SOLAPAMIENTO = 2;

    public TurnoRepository() {
        super(Turno.class);
    }

    /** Busca un turno con todas sus asociaciones precargadas. */
    public Optional<Turno> buscarConDetalles(EntityManager em, Long id) {
        try {
            return Optional.ofNullable(em.createQuery(
                    "SELECT t FROM Turno t"
                            + " LEFT JOIN FETCH t.mascota m LEFT JOIN FETCH m.cliente"
                            + " LEFT JOIN FETCH t.veterinario"
                            + " LEFT JOIN FETCH t.turnoServicios ts LEFT JOIN FETCH ts.servicio"
                            + " LEFT JOIN FETCH ts.registroMedico"
                            + " WHERE t.id = :id", Turno.class)
                    .setParameter("id", id)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /** Lista todos los turnos con detalles, del mas reciente al mas antiguo. */
    public List<Turno> listarTodosConDetalles(EntityManager em) {
        return em.createQuery(
                "SELECT DISTINCT t FROM Turno t"
                        + " LEFT JOIN FETCH t.mascota m LEFT JOIN FETCH m.cliente"
                        + " LEFT JOIN FETCH t.veterinario"
                        + " LEFT JOIN FETCH t.turnoServicios ts LEFT JOIN FETCH ts.servicio"
                        + " ORDER BY t.fechaHora DESC", Turno.class)
                .getResultList();
    }

    /** Lista los turnos de un dia (agenda), ordenados por hora. */
    public List<Turno> listarPorDia(EntityManager em, LocalDate dia) {
        return em.createQuery(
                "SELECT DISTINCT t FROM Turno t"
                        + " LEFT JOIN FETCH t.mascota m LEFT JOIN FETCH m.cliente"
                        + " LEFT JOIN FETCH t.veterinario"
                        + " LEFT JOIN FETCH t.turnoServicios ts LEFT JOIN FETCH ts.servicio"
                        + " WHERE t.fechaHora >= :inicio AND t.fechaHora < :fin"
                        + " ORDER BY t.fechaHora", Turno.class)
                .setParameter("inicio", dia.atStartOfDay())
                .setParameter("fin", dia.plusDays(1).atStartOfDay())
                .getResultList();
    }

    /**
     * Lista turnos candidatos a superponerse con un horario dado, para el
     * propietario indicado ({@code "mascota"} o {@code "veterinario"}).
     * El calculo exacto de solapamiento (con duraciones) se hace en la capa
     * de servicios.
     */
    public List<Turno> listarCandidatosSolapados(EntityManager em, String propietario, Long id,
                                                 LocalDateTime inicio, LocalDateTime fin,
                                                 List<EstadoTurno> estados) {
        if (!"mascota".equals(propietario) && !"veterinario".equals(propietario)) {
            throw new IllegalArgumentException("Propietario de solapamiento invalido: " + propietario);
        }
        return em.createQuery(
                "SELECT DISTINCT t FROM Turno t"
                        + " LEFT JOIN FETCH t.turnoServicios ts LEFT JOIN FETCH ts.servicio"
                        + " WHERE t." + propietario + ".id = :id"
                        + " AND t.estado IN :estados"
                        + " AND t.fechaHora < :fin"
                        + " AND t.fechaHora >= :limiteInferior", Turno.class)
                .setParameter("id", id)
                .setParameter("estados", estados)
                .setParameter("fin", fin)
                .setParameter("limiteInferior", inicio.minus(MARGEN_DIAS_SOLAPAMIENTO, ChronoUnit.DAYS))
                .getResultList();
    }

    /**
     * Cuenta los turnos de guarderia no cancelados para una fecha (cupo diario).
     */
    public long contarGuarderiaEnDia(EntityManager em, LocalDate dia) {
        return em.createQuery(
                "SELECT COUNT(DISTINCT t) FROM Turno t"
                        + " JOIN t.turnoServicios ts JOIN ts.servicio s"
                        + " WHERE t.estado <> :cancelado"
                        + " AND TYPE(s) = GuarderiaDia"
                        + " AND t.fechaHora >= :inicio AND t.fechaHora < :fin", Long.class)
                .setParameter("cancelado", EstadoTurno.CANCELADO)
                .setParameter("inicio", dia.atStartOfDay())
                .setParameter("fin", dia.plusDays(1).atStartOfDay())
                .getSingleResult();
    }

    /**
     * Devuelve la fecha de la ultima aplicacion de una vacuna a una mascota,
     * considerando solo turnos ya atendidos. {@code null} si nunca se aplico.
     */
    public LocalDateTime ultimaAplicacionVacuna(EntityManager em, Long mascotaId, Long vacunaId) {
        return resultadoUnico(em.createQuery(
                "SELECT MAX(t.fechaHora) FROM Turno t"
                        + " JOIN t.turnoServicios ts JOIN ts.servicio s"
                        + " WHERE t.mascota.id = :mascota"
                        + " AND t.estado = :estado"
                        + " AND TYPE(s) = AplicacionVacuna"
                        + " AND TREAT(s AS AplicacionVacuna).vacuna.id = :vacuna", LocalDateTime.class)
                .setParameter("mascota", mascotaId)
                .setParameter("estado", EstadoTurno.ATENDIDO)
                .setParameter("vacuna", vacunaId));
    }

    /** Suma de los precios historicos de los turnos atendidos en el periodo. */
    public BigDecimal sumarIngresos(EntityManager em, LocalDateTime desde, LocalDateTime hasta) {
        return em.createQuery(
                "SELECT COALESCE(SUM(ts.precioHistorico), 0) FROM TurnoServicio ts JOIN ts.turno t"
                        + " WHERE t.estado = :estado"
                        + " AND t.fechaHora >= :desde AND t.fechaHora < :hasta", BigDecimal.class)
                .setParameter("estado", EstadoTurno.ATENDIDO)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getSingleResult();
    }
}
