package com.veterinaria.repository;

import com.veterinaria.entity.Vacuna;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Acceso a datos de vacunas (catalogo).
 */
public class VacunaRepository extends BaseRepository<Vacuna, Long> {

    public VacunaRepository() {
        super(Vacuna.class);
    }

    /** Busca una vacuna por su nombre comercial (unico). */
    public Vacuna buscarPorNombreComercial(EntityManager em, String nombreComercial) {
        return resultadoUnico(em.createQuery(
                "SELECT v FROM Vacuna v WHERE v.nombreComercial = :nombre", Vacuna.class)
                .setParameter("nombre", nombreComercial));
    }

    /** Lista las vacunas ordenadas por nombre comercial. */
    public List<Vacuna> listarTodas(EntityManager em) {
        return em.createQuery(
                "SELECT v FROM Vacuna v ORDER BY v.nombreComercial", Vacuna.class)
                .getResultList();
    }
}
