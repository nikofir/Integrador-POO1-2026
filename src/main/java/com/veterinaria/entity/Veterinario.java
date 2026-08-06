package com.veterinaria.entity;

import com.veterinaria.exception.EntidadInvalidaException;
import com.veterinaria.exception.ReglaNegocioException;
import com.veterinaria.validator.ValidadorVeterinario;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Veterinario de la clinica. Posee una matricula unica y al menos una
 * especialidad (invariante 1..*).
 */
@Entity
@Table(name = "veterinarios",
        uniqueConstraints = @UniqueConstraint(name = "uk_veterinarios_matricula", columnNames = "matricula"))
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10, unique = true)
    private String matricula;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String apellido;

    @ElementCollection
    @CollectionTable(name = "veterinario_especialidades", joinColumns = @JoinColumn(name = "veterinario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "especialidad", nullable = false, length = 30)
    private final Set<Especialidad> especialidades = EnumSet.noneOf(Especialidad.class);

    protected Veterinario() {
        // requerido por JPA
    }

    private Veterinario(String matricula, String nombre, String apellido, Set<Especialidad> especialidades) {
        this.matricula = matricula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.especialidades.addAll(especialidades);
    }

    /**
     * Metodo de fabrica. Crea un veterinario valido (con al menos una
     * especialidad) o lanza excepcion.
     */
    public static Veterinario crear(String matricula, String nombre, String apellido,
                                    Set<Especialidad> especialidades) {
        String matriculaLimpia = ValidadorVeterinario.validar(matricula, nombre, apellido, especialidades);
        return new Veterinario(matriculaLimpia, nombre.trim(), apellido.trim(), especialidades);
    }

    /**
     * Actualiza los datos editables del veterinario (la matricula es inmutable).
     *
     * @throws EntidadInvalidaException si algun dato no es valido
     */
    public void actualizarDatos(String nombre, String apellido) {
        ValidadorVeterinario.validarNombreApellido(nombre, apellido);
        this.nombre = nombre.trim();
        this.apellido = apellido.trim();
    }

    /**
     * Agrega una especialidad.
     *
     * @throws EntidadInvalidaException si la especialidad es nula o ya estaba
     */
    public void agregarEspecialidad(Especialidad especialidad) {
        if (especialidad == null) {
            throw new EntidadInvalidaException("La especialidad es obligatoria.");
        }
        if (!especialidades.add(especialidad)) {
            throw new EntidadInvalidaException("La especialidad " + especialidad + " ya esta registrada.");
        }
    }

    /**
     * Quita una especialidad respetando el invariante de minimo una.
     *
     * @throws ReglaNegocioException si quedaria sin especialidades
     * @throws EntidadInvalidaException si la especialidad no estaba registrada
     */
    public void removerEspecialidad(Especialidad especialidad) {
        if (!especialidades.contains(especialidad)) {
            throw new EntidadInvalidaException("La especialidad " + especialidad + " no esta registrada.");
        }
        if (especialidades.size() == 1) {
            throw new ReglaNegocioException("El veterinario debe tener al menos una especialidad.");
        }
        especialidades.remove(especialidad);
    }

    public Long getId() {
        return id;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    /** Vista inmutable de las especialidades. */
    public Set<Especialidad> getEspecialidades() {
        return Collections.unmodifiableSet(especialidades);
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return nombre + " " + apellido + " (Mat. " + matricula + ")";
    }
}
