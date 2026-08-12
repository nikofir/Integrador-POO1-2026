package com.veterinaria.entity;

import com.veterinaria.exception.EntidadInvalidaException;
import com.veterinaria.exception.ReglaNegocioException;
import com.veterinaria.validator.ValidadorMascota;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

/**
 * Mascota de un cliente. Posee una ficha unica generada por el sistema
 * con formato {@code M-AAAA-NNNN}.
 */
@Entity
@Table(name = "mascotas", uniqueConstraints = @UniqueConstraint(name = "uk_mascotas_ficha", columnNames = "ficha"))
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String ficha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Especie especie;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String raza;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private boolean activa = true;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    protected Mascota() {
        // requerido por JPA
    }

    private Mascota(Cliente cliente, Especie especie, String nombre, String raza,
                    LocalDate fechaNacimiento, String ficha) {
        this.cliente = cliente;
        this.especie = especie;
        this.nombre = nombre;
        this.raza = raza;
        this.fechaNacimiento = fechaNacimiento;
        this.ficha = ficha;
        this.activa = true;
    }

    /**
     * Metodo de fabrica. Crea una mascota valida y activa o lanza excepcion.
     */
    public static Mascota crear(Cliente cliente, Especie especie, String nombre, String raza,
                                LocalDate fechaNacimiento, String ficha) {
        ValidadorMascota.validar(cliente, especie, nombre, raza, fechaNacimiento, ficha);
        return new Mascota(cliente, especie, nombre.trim(), raza.trim(), fechaNacimiento, ficha);
    }

    /**
     * Marca la mascota como inactiva. Una mascota inactiva no puede tomar turnos.
     *
     * @throws ReglaNegocioException si ya estaba inactiva
     */
    public void marcarInactiva() {
        if (!activa) {
            throw new ReglaNegocioException("La mascota ya se encuentra inactiva.");
        }
        activa = false;
    }

    /**
     * Reactiva la mascota.
     *
     * @throws ReglaNegocioException si ya estaba activa
     */
    public void reactivar() {
        if (activa) {
            throw new ReglaNegocioException("La mascota ya se encuentra activa.");
        }
        activa = true;
    }

    /**
     * Actualiza los datos editables de la mascota (la ficha es inmutable).
     *
     * @throws EntidadInvalidaException si algun dato no es valido
     */
    public void actualizarDatos(Especie especie, String nombre, String raza, LocalDate fechaNacimiento) {
        ValidadorMascota.validarDatos(especie, nombre, raza, fechaNacimiento);
        this.especie = especie;
        this.nombre = nombre.trim();
        this.raza = raza.trim();
        this.fechaNacimiento = fechaNacimiento;
    }

    void asignarCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Long getId() {
        return id;
    }

    public String getFicha() {
        return ficha;
    }

    public Especie getEspecie() {
        return especie;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRaza() {
        return raza;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public boolean isActiva() {
        return activa;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public long getEdadAnios() {
        return java.time.Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        return nombre + " (" + especie + ", " + raza + ")";
    }
}
