package com.veterinaria.entity;

import com.veterinaria.exception.EntidadInvalidaException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

/**
 * Servicio de aplicacion de una vacuna del catalogo. Al asignarse a un
 * turno, la regla de vigencia de la vacuna se controla en la capa de
 * servicios (ver {@link Vacuna#estaVigente}).
 */
@Entity
@DiscriminatorValue("APLICACION_VACUNA")
public class AplicacionVacuna extends Servicio {

    // Nulable en la tabla: con herencia SINGLE_TABLE las otras subclases
    // dejan esta columna en NULL.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vacuna_id")
    private Vacuna vacuna;

    protected AplicacionVacuna() {
        // requerido por JPA
    }

    /**
     * Metodo de fabrica.
     *
     * @param vacuna vacuna que se aplica con este servicio
     */
    public static AplicacionVacuna crear(String nombre, BigDecimal precioBase, int duracionMinutos, Vacuna vacuna) {
        if (vacuna == null) {
            throw new EntidadInvalidaException("La vacuna asociada al servicio es obligatoria.");
        }
        return new AplicacionVacuna(nombre, precioBase, duracionMinutos, vacuna);
    }

    private AplicacionVacuna(String nombre, BigDecimal precioBase, int duracionMinutos, Vacuna vacuna) {
        super(nombre, precioBase, duracionMinutos);
        this.vacuna = vacuna;
    }

    public Vacuna getVacuna() {
        return vacuna;
    }

    @Override
    public String getTipo() {
        return "Aplicacion de vacuna";
    }
}
