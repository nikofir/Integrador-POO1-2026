package com.veterinaria.repository;

import com.veterinaria.entity.Mascota;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Acceso a datos de mascotas.
 */
public class MascotaRepository extends BaseRepository<Mascota, Long> {

    public MascotaRepository() {
        super(Mascota.class);
    }

    /** Busca una mascota por su ficha unica. */
    public Mascota buscarPorFicha(EntityManager em, String ficha) {
        return resultadoUnico(em.createQuery(
                "SELECT m FROM Mascota m WHERE m.ficha = :ficha", Mascota.class)
                .setParameter("ficha", ficha));
    }

    /** Lista mascotas de un cliente. */
    public List<Mascota> listarPorCliente(EntityManager em, Long clienteId) {
        return em.createQuery(
                "SELECT m FROM Mascota m JOIN FETCH m.cliente WHERE m.cliente.id = :id ORDER BY m.nombre",
                Mascota.class)
                .setParameter("id", clienteId)
                .getResultList();
    }

    /** Lista mascotas activas (pueden tomar turnos). */
    public List<Mascota> listarActivas(EntityManager em) {
        return em.createQuery(
                "SELECT m FROM Mascota m JOIN FETCH m.cliente WHERE m.activa = true ORDER BY m.nombre",
                Mascota.class)
                .getResultList();
    }

    /**
     * Devuelve la ficha mas alta registrada para el prefijo dado (por anio),
     * util para generar la siguiente ficha unica. Ej.: {@code M-2026-%}.
     */
    public String buscarUltimaFicha(EntityManager em, String prefijo) {
        return resultadoUnico(em.createQuery(
                "SELECT MAX(m.ficha) FROM Mascota m WHERE m.ficha LIKE :prefijo", String.class)
                .setParameter("prefijo", prefijo + "%"));
    }
}
