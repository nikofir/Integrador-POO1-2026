package com.veterinaria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

/**
 * Vacuna del catalogo. Se identifica por su nombre comercial y previene
 * una enfermedad con una periodicidad definida en meses.
 * <p>
 * No debe aplicarse a una mascota mientras la dosis anterior siga vigente
 * (ver {@link #estaVigente(LocalDate, LocalDate)}).
 */
@Entity
@Table(name = "vacunas",
        uniqueConstraints = @UniqueConstraint(name = "uk_vacunas_nombre", columnNames = "nombre_comercial"))
public class Vacuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_comercial", nullable = false, length = 100, unique = true)
    private String nombreComercial;

    @Column(name = "enfermedad_prevenida", nullable = false, length = 100)
    private String enfermedadPrevenida;

    @Column(name = "periodicidad_meses", nullable = false)
    private int periodicidadMeses;

    protected Vacuna() {
        // requerido por JPA
    }

    private Vacuna(String nombreComercial, String enfermedadPrevenida, int periodicidadMeses) {
        this.nombreComercial = nombreComercial;
        this.enfermedadPrevenida = enfermedadPrevenida;
        this.periodicidadMeses = periodicidadMeses;
    }

    /**
     * Metodo de fabrica.
     */
    public static Vacuna crear(String nombreComercial, String enfermedadPrevenida, int periodicidadMeses) {
        String nombre = com.veterinaria.validator.ValidadorVacuna
                .validar(nombreComercial, enfermedadPrevenida, periodicidadMeses);
        return new Vacuna(nombre, enfermedadPrevenida.trim(), periodicidadMeses);
    }

    /**
     * Determina si la dosis aplicada en {@code fechaUltimaAplicacion} sigue
     * vigente para la fecha {@code fechaTurno}. Si lo esta, no corresponde
     * reaplicar la vacuna.
     */
    public boolean estaVigente(LocalDate fechaUltimaAplicacion, LocalDate fechaTurno) {
        if (fechaUltimaAplicacion == null) {
            return false;
        }
        return !fechaTurno.isAfter(fechaUltimaAplicacion.plusMonths(periodicidadMeses));
    }

    /**
     * Fecha en que debe reaplicarse la dosis aplicada en {@code ultimaAplicacion}
     * (la aplicacion anterior mas la periodicidad en meses).
     */
    public LocalDate proximaAplicacion(LocalDate ultimaAplicacion) {
        if (ultimaAplicacion == null) {
            return null;
        }
        return ultimaAplicacion.plusMonths(periodicidadMeses);
    }

    /**
     * Clasifica el estado de la dosis aplicada en {@code ultimaAplicacion}
     * respecto de la fecha de referencia: VENCIDA, PROXIMA_A_VENCER (si vence
     * dentro de los proximos {@code diasAlerta}) o AL_DIA.
     */
    public EstadoVacuna estadoAlerta(LocalDate ultimaAplicacion, LocalDate referencia, long diasAlerta) {
        LocalDate proxima = proximaAplicacion(ultimaAplicacion);
        if (proxima.isBefore(referencia)) {
            return EstadoVacuna.VENCIDA;
        }
        if (!proxima.isAfter(referencia.plusDays(diasAlerta))) {
            return EstadoVacuna.PROXIMA_A_VENCER;
        }
        return EstadoVacuna.AL_DIA;
    }

    public Long getId() {
        return id;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public String getEnfermedadPrevenida() {
        return enfermedadPrevenida;
    }

    public int getPeriodicidadMeses() {
        return periodicidadMeses;
    }

    @Override
    public String toString() {
        return nombreComercial + " (" + enfermedadPrevenida + ")";
    }
}
