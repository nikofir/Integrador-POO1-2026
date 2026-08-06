package com.veterinaria.repository;

import com.veterinaria.entity.Veterinario;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Acceso a datos de veterinarios.
 */
public class VeterinarioRepository extends BaseRepository<Veterinario, Long> {

    public VeterinarioRepository() {
        super(Veterinario.class);
    }

    /** Busca un veterinario por matricula (unica), con especialidades. */
    public Veterinario buscarPorMatricula(EntityManager em, String matricula) {
        return resultadoUnico(em.createQuery(
                "SELECT v FROM Veterinario v LEFT JOIN FETCH v.especialidades WHERE v.matricula = :matricula",
                Veterinario.class)
                .setParameter("matricula", matricula));
    }

    /** Lista veterinarios con sus especialidades precargadas. */
    public List<Veterinario> listarTodosConEspecialidades(EntityManager em) {
        return em.createQuery(
                "SELECT DISTINCT v FROM Veterinario v LEFT JOIN FETCH v.especialidades ORDER BY v.apellido, v.nombre",
                Veterinario.class)
                .getResultList();
    }
}
