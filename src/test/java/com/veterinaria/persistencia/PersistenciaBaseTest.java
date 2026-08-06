package com.veterinaria.persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import java.util.Properties;
import java.util.function.Function;

import static com.veterinaria.util.JpaUtil.crearFabrica;

/**
 * Base para pruebas de persistencia con H2 en memoria (modo PostgreSQL).
 * Cada metodo de prueba usa un esquema nuevo (create-drop), lo que aísla
 * los datos entre tests.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PersistenciaBaseTest {

    private EntityManagerFactory emf;

    @BeforeEach
    void iniciarFabrica() {
        emf = crearFabrica(propiedadesBase());
    }

    @AfterEach
    void cerrarFabrica() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    private Properties propiedadesBase() {
        Properties propiedades = new Properties();
        propiedades.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        propiedades.setProperty("hibernate.connection.url",
                "jdbc:h2:mem:" + nombreBase() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        propiedades.setProperty("hibernate.connection.username", "sa");
        propiedades.setProperty("hibernate.connection.password", "");
        propiedades.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        propiedades.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        propiedades.setProperty("hibernate.show_sql", "false");
        return propiedades;
    }

    /** Nombre de la base en memoria; sobrescribir por clase para aislar pruebas. */
    protected String nombreBase() {
        return "veterinaria_test";
    }

    protected EntityManager em() {
        return emf.createEntityManager();
    }

    /** Ejecuta una operacion en transaccion sobre la fabrica de pruebas. */
    protected <T> T enTransaccion(Function<EntityManager, T> operacion) {
        EntityManager em = em();
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
}
