package com.veterinaria.entity;

import com.veterinaria.exception.EntidadInvalidaException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Linea de un turno: relaciona el turno con un {@link Servicio} y guarda
 * el precio historico (instantanea del precio base al momento de reservar).
 * Puede contener, opcionalmente, un {@link RegistroMedico}.
 */
@Entity
@Table(name = "turno_servicios")
public class TurnoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Column(name = "precio_historico", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioHistorico;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "registro_medico_id")
    private RegistroMedico registroMedico;

    protected TurnoServicio() {
        // requerido por JPA
    }

    private TurnoServicio(Turno turno, Servicio servicio) {
        this.turno = turno;
        this.servicio = servicio;
        this.precioHistorico = servicio.getPrecioBase();
    }

    /**
     * Metodo de fabrica. Toma instantanea del precio base del servicio.
     */
    public static TurnoServicio crear(Turno turno, Servicio servicio) {
        if (turno == null) {
            throw new EntidadInvalidaException("El turno es obligatorio.");
        }
        if (servicio == null) {
            throw new EntidadInvalidaException("El servicio es obligatorio.");
        }
        return new TurnoServicio(turno, servicio);
    }

    /**
     * Asocia un registro medico a este turno-servicio.
     *
     * @throws EntidadInvalidaException si el registro es nulo o ya existe uno
     */
    public void asignarRegistroMedico(RegistroMedico registroMedico) {
        if (registroMedico == null) {
            throw new EntidadInvalidaException("El registro medico es obligatorio.");
        }
        if (this.registroMedico != null) {
            throw new EntidadInvalidaException("Este turno-servicio ya tiene un registro medico.");
        }
        this.registroMedico = registroMedico;
    }

    public Long getId() {
        return id;
    }

    public Turno getTurno() {
        return turno;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public BigDecimal getPrecioHistorico() {
        return precioHistorico;
    }

    public RegistroMedico getRegistroMedico() {
        return registroMedico;
    }

    public boolean tieneRegistroMedico() {
        return registroMedico != null;
    }

    @Override
    public String toString() {
        return servicio.getNombre() + " ($" + precioHistorico + ")";
    }
}
