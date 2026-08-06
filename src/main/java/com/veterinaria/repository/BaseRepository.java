package com.veterinaria.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio base generico. Los metodos reciben el {@link EntityManager}
 * activo de la transaccion abierta por la capa de servicios.
 *
 * @param <T>  tipo de entidad
 * @param <ID> tipo del identificador
 */
public abstract class BaseRepository<T, ID> {

    private final Class<T> tipo;

    protected BaseRepository(Class<T> tipo) {
        this.tipo = tipo;
    }

    /**
     * Persiste una entidad nueva o actualiza (merge) una existente.
     *
     * @return la entidad gestionada (usar este resultado)
     */
    public T guardar(T entidad, EntityManager em) {
        Object identificador = em.getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .getIdentifier(entidad);
        if (identificador == null) {
            em.persist(entidad);
            return entidad;
        }
        return em.merge(entidad);
    }

    public Optional<T> buscarPorId(ID id, EntityManager em) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(em.find(tipo, id));
    }

    public List<T> listarTodos(EntityManager em) {
        return em.createQuery("SELECT e FROM " + tipo.getSimpleName() + " e", tipo).getResultList();
    }

    public boolean existePorId(ID id, EntityManager em) {
        return id != null && em.find(tipo, id) != null;
    }

    public void eliminar(T entidad, EntityManager em) {
        T gestionada = em.contains(entidad) ? entidad : em.merge(entidad);
        em.remove(gestionada);
    }

    /** Devuelve el unico resultado de una consulta o {@code null} si no hay ninguno. */
    protected static <T> T resultadoUnico(TypedQuery<T> consulta) {
        try {
            return consulta.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
