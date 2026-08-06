package com.veterinaria.entity;

import com.veterinaria.exception.EntidadInvalidaException;
import com.veterinaria.exception.ReglaNegocioException;
import com.veterinaria.validator.ValidadorTurno;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Turno que relaciona una mascota, un veterinario y uno o mas servicios.
 * <p>
 * Maquina de estados: {@code PENDIENTE -> CONFIRMADO | CANCELADO} y
 * {@code CONFIRMADO -> ATENDIDO | CANCELADO} (la cancelacion de un turno
 * confirmado exige mas de 24 hs de anticipacion).
 */
@Entity
@Table(name = "turnos")
public class Turno {

    private static final long HORAS_CANCELACION_CONFIRMADO = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne(optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Veterinario veterinario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoTurno estado;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "turno", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private final List<TurnoServicio> turnoServicios = new ArrayList<>();

    protected Turno() {
        // requerido por JPA
    }

    private Turno(Mascota mascota, Veterinario veterinario, LocalDateTime fechaHora) {
        this.mascota = mascota;
        this.veterinario = veterinario;
        this.fechaHora = fechaHora;
        this.estado = EstadoTurno.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Metodo de fabrica. Crea un turno valido (PENDIENTE) o lanza excepcion.
     * Los turnos solapados, cupos y vigencia de vacunas se controlan en la
     * capa de servicios porque requieren consultas a la persistencia.
     */
    public static Turno crear(Mascota mascota, Veterinario veterinario,
                              LocalDateTime fechaHora, List<Servicio> servicios) {
        ValidadorTurno.validarCreacion(mascota, veterinario, fechaHora, servicios);
        Turno turno = new Turno(mascota, veterinario, fechaHora);
        for (Servicio servicio : servicios) {
            turno.agregarServicio(servicio);
        }
        return turno;
    }

    /**
     * Confirma el turno.
     *
     * @throws ReglaNegocioException si no esta PENDIENTE
     */
    public void confirmar() {
        if (estado != EstadoTurno.PENDIENTE) {
            throw new ReglaNegocioException("Solo un turno PENDIENTE puede confirmarse (estado actual: " + estado + ").");
        }
        estado = EstadoTurno.CONFIRMADO;
    }

    /**
     * Atiende el turno.
     *
     * @throws ReglaNegocioException si no esta CONFIRMADO
     */
    public void atender() {
        if (estado != EstadoTurno.CONFIRMADO) {
            throw new ReglaNegocioException("Solo un turno CONFIRMADO puede atenderse (estado actual: " + estado + ").");
        }
        estado = EstadoTurno.ATENDIDO;
    }

    /**
     * Cancela el turno. Un turno PENDIENTE se cancela en cualquier momento;
     * uno CONFIRMADO solo con mas de 24 hs de anticipacion.
     *
     * @param momento instante en que se solicita la cancelacion
     * @throws ReglaNegocioException si el turno ya esta terminal o si un
     *                               turno CONFIRMADO se cancela fuera de termino
     */
    public void cancelar(LocalDateTime momento) {
        if (estado == EstadoTurno.ATENDIDO || estado == EstadoTurno.CANCELADO) {
            throw new ReglaNegocioException("El turno ya se encuentra " + estado + ".");
        }
        if (estado == EstadoTurno.CONFIRMADO && fueraDeTermino(momento)) {
            throw new ReglaNegocioException(
                    "Un turno confirmado solo puede cancelarse con mas de " + HORAS_CANCELACION_CONFIRMADO
                            + " horas de anticipacion.");
        }
        estado = EstadoTurno.CANCELADO;
    }

    private boolean fueraDeTermino(LocalDateTime momento) {
        return momento == null || !momento.isBefore(fechaHora.minus(HORAS_CANCELACION_CONFIRMADO, ChronoUnit.HOURS));
    }

    /**
     * Agrega un servicio al turno tomando instantanea de su precio.
     *
     * @throws ReglaNegocioException si el turno esta terminal o el servicio
     *                               ya estaba incluido
     */
    public void agregarServicio(Servicio servicio) {
        if (esTerminal()) {
            throw new ReglaNegocioException("No pueden agregarse servicios a un turno " + estado + ".");
        }
        if (servicio == null) {
            throw new EntidadInvalidaException("El servicio es obligatorio.");
        }
        boolean duplicado = turnoServicios.stream().anyMatch(ts -> {
            Long idExistente = ts.getServicio().getId();
            Long idNuevo = servicio.getId();
            if (idExistente != null && idNuevo != null) {
                return idExistente.equals(idNuevo);
            }
            return ts.getServicio() == servicio;
        });
        if (duplicado) {
            throw new ReglaNegocioException("El servicio '" + servicio.getNombre() + "' ya esta incluido en el turno.");
        }
        TurnoServicio turnoServicio = TurnoServicio.crear(this, servicio);
        turnoServicios.add(turnoServicio);
    }

    /**
     * Quita un servicio del turno.
     *
     * @throws ReglaNegocioException si el turno esta terminal
     * @throws EntidadInvalidaException si el turno-servicio no pertenece al turno
     */
    public void quitarServicio(TurnoServicio turnoServicio) {
        if (esTerminal()) {
            throw new ReglaNegocioException("No pueden quitarse servicios de un turno " + estado + ".");
        }
        TurnoServicio encontrado = turnoServicios.stream()
                .filter(ts -> mismoTurnoServicio(ts, turnoServicio))
                .findFirst()
                .orElse(null);
        if (encontrado == null) {
            throw new EntidadInvalidaException("El turno-servicio no pertenece a este turno.");
        }
        turnoServicios.remove(encontrado);
    }

    private static boolean mismoTurnoServicio(TurnoServicio uno, TurnoServicio otro) {
        if (uno.getId() != null && otro.getId() != null) {
            return uno.getId().equals(otro.getId());
        }
        return uno == otro;
    }

    /**
     * Registra el registro medico de un turno-servicio. Solo se permite
     * cuando el turno ya fue atendido.
     */
    public void registrarRegistroMedico(TurnoServicio turnoServicio, RegistroMedico registroMedico) {
        if (estado != EstadoTurno.ATENDIDO) {
            throw new ReglaNegocioException(
                    "El registro medico solo puede cargarse en turnos ATENDIDOS (estado actual: " + estado + ").");
        }
        if (!turnoServicios.contains(turnoServicio)) {
            throw new EntidadInvalidaException("El turno-servicio no pertenece a este turno.");
        }
        turnoServicio.asignarRegistroMedico(registroMedico);
    }

    private boolean esTerminal() {
        return estado == EstadoTurno.ATENDIDO || estado == EstadoTurno.CANCELADO;
    }

    /** Suma de los precios historicos de todos los servicios del turno. */
    public BigDecimal calcularPrecioTotal() {
        return turnoServicios.stream()
                .map(TurnoServicio::getPrecioHistorico)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Suma de las duraciones en minutos de todos los servicios del turno. */
    public int calcularDuracionTotal() {
        return turnoServicios.stream()
                .mapToInt(ts -> ts.getServicio().getDuracionMinutos())
                .sum();
    }

    public Long getId() {
        return id;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public Veterinario getVeterinario() {
        return veterinario;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /** Vista inmutable de los servicios del turno. */
    public List<TurnoServicio> getTurnoServicios() {
        return Collections.unmodifiableList(turnoServicios);
    }

    @Override
    public String toString() {
        return "Turno de " + mascota.getNombre() + " con " + veterinario.getNombreCompleto()
                + " el " + fechaHora + " [" + estado + "]";
    }
}
