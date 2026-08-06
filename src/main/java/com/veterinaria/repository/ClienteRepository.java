package com.veterinaria.repository;

import com.veterinaria.entity.Cliente;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Acceso a datos de clientes.
 */
public class ClienteRepository extends BaseRepository<Cliente, Long> {

    public ClienteRepository() {
        super(Cliente.class);
    }

    /** Busca un cliente por DNI (unico). */
    public Cliente buscarPorDni(EntityManager em, String dni) {
        return resultadoUnico(em.createQuery(
                "SELECT c FROM Cliente c WHERE c.dni = :dni", Cliente.class)
                .setParameter("dni", dni));
    }

    /** Lista clientes con sus mascotas precargadas, ordenados por apellido. */
    public List<Cliente> listarTodosConMascotas(EntityManager em) {
        return em.createQuery(
                "SELECT DISTINCT c FROM Cliente c LEFT JOIN FETCH c.mascotas ORDER BY c.apellido, c.nombre",
                Cliente.class)
                .getResultList();
    }
}
