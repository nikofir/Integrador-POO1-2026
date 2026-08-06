package com.veterinaria.repository;

import com.veterinaria.entity.Servicio;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Acceso a datos de servicios (catalogo, jerarquia de herencia).
 */
public class ServicioRepository extends BaseRepository<Servicio, Long> {

    public ServicioRepository() {
        super(Servicio.class);
    }

    /** Lista el catalogo de servicios ordenado por nombre. */
    public List<Servicio> listarCatalogo(EntityManager em) {
        return em.createQuery(
                "SELECT s FROM Servicio s ORDER BY s.nombre", Servicio.class)
                .getResultList();
    }
}
