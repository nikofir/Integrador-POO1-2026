package com.veterinaria.persistencia;

import com.veterinaria.entity.AplicacionVacuna;
import com.veterinaria.entity.ConsultaMedica;
import com.veterinaria.entity.GuarderiaDia;
import com.veterinaria.entity.Peluqueria;
import com.veterinaria.entity.Servicio;
import com.veterinaria.entity.Vacuna;
import com.veterinaria.repository.ServicioRepository;
import com.veterinaria.repository.VacunaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioRepositoryTest extends PersistenciaBaseTest {

    @Override
    protected String nombreBase() {
        return "servicios_test";
    }

    private final ServicioRepository repositorio = new ServicioRepository();
    private final VacunaRepository repositorioVacunas = new VacunaRepository();

    @Test
    void persisteJerarquiaDeServicios() {
        Vacuna vacuna = DatosPrueba.vacuna(1);
        ConsultaMedica consulta = DatosPrueba.consultaMedica();
        Peluqueria peluqueria = Peluqueria.crear("Bano y corte", new BigDecimal("25000.00"), 60);
        GuarderiaDia guarderia = GuarderiaDia.crear("Guarderia 8hs", new BigDecimal("30000.00"), 480, 10);
        AplicacionVacuna aplicacion = AplicacionVacuna.crear("Aplicacion vacuna 1", new BigDecimal("20000.00"), 15, vacuna);

        enTransaccion(em -> {
            repositorioVacunas.guardar(vacuna, em);
            repositorio.guardar(consulta, em);
            repositorio.guardar(peluqueria, em);
            repositorio.guardar(guarderia, em);
            repositorio.guardar(aplicacion, em);
            return null;
        });

        int cantidad = enTransaccion(em -> repositorio.listarCatalogo(em).size());
        assertEquals(4, cantidad);

        ConsultaMedica consultaPersistida = enTransaccion(em -> em.find(ConsultaMedica.class, consulta.getId()));
        assertEquals("Consulta general", consultaPersistida.getNombre());

        AplicacionVacuna aplicacionPersistida = enTransaccion(em -> em.find(AplicacionVacuna.class, aplicacion.getId()));
        assertTrue(aplicacionPersistida instanceof Servicio);
        assertEquals("Vacuna 1", aplicacionPersistida.getVacuna().getNombreComercial());
    }
}
