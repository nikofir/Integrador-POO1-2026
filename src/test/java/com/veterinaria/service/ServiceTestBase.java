package com.veterinaria.service;

import com.veterinaria.util.JpaUtil;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base para pruebas de servicios. Reinicia la fabrica de persistencia en cada
 * prueba para que cada una parta de un esquema H2 en memoria limpio.
 */
abstract class ServiceTestBase {

    @BeforeEach
    void reiniciarPersistencia() {
        JpaUtil.cerrar();
    }
}
