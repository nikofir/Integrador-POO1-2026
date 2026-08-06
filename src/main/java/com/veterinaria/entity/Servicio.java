package com.veterinaria.entity;

import com.veterinaria.validator.ValidadorServicio;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Servicio veterinario ofrecido por la clinica (abstracto).
 * <p>
 * Jerarquia de herencia de tabla unica ({@code SINGLE_TABLE}) con subclases:
 * {@link ConsultaMedica}, {@link Peluqueria}, {@link GuarderiaDia} y
 * {@link AplicacionVacuna}.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_servicio", discriminatorType = DiscriminatorType.STRING, length = 30)
@Table(name = "servicios")
public abstract class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "duracion_minutos", nullable = false)
    private int duracionMinutos;

    protected Servicio() {
        // requerido por JPA
    }

    protected Servicio(String nombre, BigDecimal precioBase, int duracionMinutos) {
        ValidadorServicio.validar(nombre, precioBase, duracionMinutos);
        this.nombre = nombre;
        this.precioBase = precioBase;
        this.duracionMinutos = duracionMinutos;
    }

    /**
     * Actualiza los datos editables del servicio.
     *
     * @throws com.veterinaria.exception.EntidadInvalidaException si algun dato no es valido
     */
    public void actualizarDatos(String nombre, BigDecimal precioBase, int duracionMinutos) {
        String nombreLimpio = ValidadorServicio.validar(nombre, precioBase, duracionMinutos);
        this.nombre = nombreLimpio;
        this.precioBase = precioBase;
        this.duracionMinutos = duracionMinutos;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    /** Subclases pueden definir una etiqueta descriptiva del tipo de servicio. */
    public abstract String getTipo();

    @Override
    public String toString() {
        return nombre;
    }
}
