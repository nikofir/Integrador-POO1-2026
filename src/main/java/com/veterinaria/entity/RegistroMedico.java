package com.veterinaria.entity;

import com.veterinaria.validator.ValidadorRegistroMedico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Registro medico asociado opcionalmente a un turno-servicio. Contiene
 * el diagnostico y el tratamiento indicado al atender al paciente.
 */
@Entity
@Table(name = "registros_medicos")
public class RegistroMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String diagnostico;

    @Column(nullable = false, length = 500)
    private String tratamiento;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected RegistroMedico() {
        // requerido por JPA
    }

    private RegistroMedico(String diagnostico, String tratamiento) {
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.fecha = LocalDateTime.now();
    }

    /**
     * Metodo de fabrica. Crea un registro medico valido o lanza excepcion.
     */
    public static RegistroMedico crear(String diagnostico, String tratamiento) {
        String diagnosticoLimpio = ValidadorRegistroMedico.validar(diagnostico, tratamiento);
        return new RegistroMedico(diagnosticoLimpio, tratamiento.trim());
    }

    public Long getId() {
        return id;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    @Override
    public String toString() {
        return "Diagnostico: " + diagnostico;
    }
}
