package com.veterinaria.entity;

import com.veterinaria.exception.ReglaNegocioException;
import com.veterinaria.validator.ValidadorServicio;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

/**
 * Servicio de guarderia por dia. Controla un cupo diario maximo de
 * mascotas admitidas.
 */
@Entity
@DiscriminatorValue("GUARDERIA_DIA")
public class GuarderiaDia extends Servicio {

    // Nulable en la tabla: con herencia SINGLE_TABLE las otras subclases
    // dejan esta columna en NULL.
    @Column(name = "cupo_maximo")
    private int cupoMaximo;

    protected GuarderiaDia() {
        // requerido por JPA
    }

    /**
     * Metodo de fabrica.
     *
     * @param cupoMaximo cupo diario de mascotas admitidas
     */
    public static GuarderiaDia crear(String nombre, BigDecimal precioBase, int duracionMinutos, int cupoMaximo) {
        ValidadorServicio.validarCupo(cupoMaximo);
        return new GuarderiaDia(nombre, precioBase, duracionMinutos, cupoMaximo);
    }

    private GuarderiaDia(String nombre, BigDecimal precioBase, int duracionMinutos, int cupoMaximo) {
        super(nombre, precioBase, duracionMinutos);
        this.cupoMaximo = cupoMaximo;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    /**
     * Actualiza el cupo diario.
     *
     * @throws com.veterinaria.exception.ValidacionException si el cupo no es valido
     */
    public void actualizarCupo(int cupoMaximo) {
        ValidadorServicio.validarCupo(cupoMaximo);
        this.cupoMaximo = cupoMaximo;
    }

    /**
     * Valida que quede cupo disponible para el dia.
     *
     * @param turnosOcupados cantidad de turnos de guarderia ya tomados para la fecha
     * @throws ReglaNegocioException si el cupo diario esta completo
     */
    public void validarCupoDisponible(int turnosOcupados) {
        if (turnosOcupados >= cupoMaximo) {
            throw new ReglaNegocioException(
                    "Cupo diario de guarderia completo (maximo " + cupoMaximo + " mascotas).");
        }
    }

    @Override
    public String getTipo() {
        return "Guarderia por dia";
    }
}
