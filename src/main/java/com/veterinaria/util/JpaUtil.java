package com.veterinaria.util;

import com.veterinaria.exception.PersistenciaException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utilidad central de persistencia.
 * <p>
 * Construye (una unica vez) la {@link EntityManagerFactory} a partir del
 * archivo {@code database.properties} (PostgreSQL en ejecucion, H2 en pruebas)
 * y ofrece ejecucion de operaciones dentro de transacciones.
 */
public final class JpaUtil {

    private static final String ARCHIVO_PROPIEDADES = "database.properties";

    private static volatile EntityManagerFactory entityManagerFactory;

    private JpaUtil() {
    }

    /**
     * Devuelve la fabrica de {@link EntityManager} (inicializacion perezosa y thread-safe).
     */
    public static EntityManagerFactory entityManagerFactory() {
        EntityManagerFactory local = entityManagerFactory;
        if (local == null) {
            synchronized (JpaUtil.class) {
                local = entityManagerFactory;
                if (local == null) {
                    local = construir();
                    entityManagerFactory = local;
                }
            }
        }
        return local;
    }

    /**
     * Crea una nueva {@link EntityManager}. Debe cerrarse siempre
     * (normalmente mediante {@link #enTransaccion(Supplier)}).
     */
    public static EntityManager entityManager() {
        return entityManagerFactory().createEntityManager();
    }

    /**
     * Ejecuta una operacion dentro de una transaccion, hace commit y cierra
     * el {@link EntityManager}. Si la operacion lanza una excepcion, hace
     * rollback y la propaga.
     *
     * @param operacion operacion cuyo resultado se persiste
     * @param <T>       tipo del resultado
     * @return el resultado de {@code operacion}
     */
    public static <T> T enTransaccion(Supplier<T> operacion) {
        return enTransaccion(em -> operacion.get());
    }

    /**
     * Variante que expone el {@link EntityManager} activo a la operacion,
     * para que los repositorios ejecuten sus consultas dentro de la misma
     * transaccion.
     */
    public static <T> T enTransaccion(Function<EntityManager, T> operacion) {
        EntityManager em = entityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            T resultado = operacion.apply(em);
            tx.commit();
            return resultado;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Variante sin valor de retorno de {@link #enTransaccion(Supplier)}.
     */
    public static void enTransaccionVoid(Runnable operacion) {
        enTransaccion(() -> {
            operacion.run();
            return null;
        });
    }

    /**
     * Cierra la fabrica. Pensada para el cierre de la aplicacion y para los tests.
     */
    public static void cerrar() {
        EntityManagerFactory local = entityManagerFactory;
        if (local != null && local.isOpen()) {
            local.close();
        }
        entityManagerFactory = null;
    }

    private static EntityManagerFactory construir() {
        try {
            return crearFabrica(cargarPropiedades());
        } catch (PersistenciaException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenciaException("No se pudo inicializar la capa de persistencia", e);
        }
    }

    /**
     * Construye una fabrica a partir de propiedades de Hibernate.
     * Se usa en ejecucion con {@code database.properties} y en las pruebas
     * con H2 en memoria.
     */
    public static EntityManagerFactory crearFabrica(Properties propiedades) {
        try {
            Configuration configuracion = new Configuration();
            configuracion.addProperties(propiedades);
            registrarEntidades(configuracion);
            return configuracion.buildSessionFactory();
        } catch (Exception e) {
            throw new PersistenciaException("No se pudo inicializar la capa de persistencia", e);
        }
    }

    private static Properties cargarPropiedades() {
        try (InputStream flujo = JpaUtil.class.getClassLoader().getResourceAsStream(ARCHIVO_PROPIEDADES)) {
            if (flujo == null) {
                throw new PersistenciaException("No se encontro el archivo " + ARCHIVO_PROPIEDADES
                        + " en el classpath");
            }
            Properties propiedades = new Properties();
            propiedades.load(flujo);
            return propiedades;
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer el archivo " + ARCHIVO_PROPIEDADES, e);
        }
    }

    /**
     * Registra las entidades del modelo de dominio en la configuracion de Hibernate.
     */
    private static void registrarEntidades(Configuration configuracion) {
        configuracion.addAnnotatedClass(com.veterinaria.entity.Cliente.class);
        configuracion.addAnnotatedClass(com.veterinaria.entity.Mascota.class);
        configuracion.addAnnotatedClass(com.veterinaria.entity.Veterinario.class);
        configuracion.addAnnotatedClass(com.veterinaria.entity.Servicio.class);
        configuracion.addAnnotatedClass(com.veterinaria.entity.ConsultaMedica.class);
        configuracion.addAnnotatedClass(com.veterinaria.entity.Peluqueria.class);
        configuracion.addAnnotatedClass(com.veterinaria.entity.GuarderiaDia.class);
        configuracion.addAnnotatedClass(com.veterinaria.entity.AplicacionVacuna.class);
        configuracion.addAnnotatedClass(com.veterinaria.entity.Vacuna.class);
        configuracion.addAnnotatedClass(com.veterinaria.entity.RegistroMedico.class);
        configuracion.addAnnotatedClass(com.veterinaria.entity.TurnoServicio.class);
        configuracion.addAnnotatedClass(com.veterinaria.entity.Turno.class);
    }
}
