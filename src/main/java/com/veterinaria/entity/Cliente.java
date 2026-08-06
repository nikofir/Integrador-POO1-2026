package com.veterinaria.entity;

import com.veterinaria.exception.EntidadInvalidaException;
import com.veterinaria.validator.ValidadorCliente;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cliente de la veterinaria. Posee cero o mas mascotas en composicion:
 * las mascotas viven y mueren con el cliente ({@code orphanRemoval=true}).
 */
@Entity
@Table(name = "clientes", uniqueConstraints = @UniqueConstraint(name = "uk_clientes_dni", columnNames = "dni"))
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8)
    private String dni;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String apellido;

    @Column(nullable = false, length = 15)
    private String telefono;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 120)
    private String domicilio;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Mascota> mascotas = new ArrayList<>();

    protected Cliente() {
        // requerido por JPA
    }

    private Cliente(String dni, String nombre, String apellido, String telefono, String email, String domicilio) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
        this.domicilio = domicilio;
    }

    /**
     * Metodo de fabrica. Crea un cliente valido o lanza excepcion.
     */
    public static Cliente crear(String dni, String nombre, String apellido,
                                String telefono, String email, String domicilio) {
        ValidadorCliente.validarDomicilioOpcional(domicilio);
        String emailLimpio = ValidadorCliente.validar(dni, nombre, apellido, telefono, email);
        return new Cliente(dni.trim(), nombre.trim(), apellido.trim(), telefono.trim(), emailLimpio,
                domicilio == null ? null : domicilio.trim());
    }

    /**
     * Actualiza los datos editables del cliente (el DNI es inmutable).
     *
     * @throws EntidadInvalidaException si algun dato no es valido
     */
    public void actualizarDatos(String nombre, String apellido, String telefono, String email, String domicilio) {
        ValidadorCliente.validarDomicilioOpcional(domicilio);
        String emailLimpio = ValidadorCliente.validar(this.dni, nombre, apellido, telefono, email);
        this.nombre = nombre.trim();
        this.apellido = apellido.trim();
        this.telefono = telefono.trim();
        this.email = emailLimpio;
        this.domicilio = domicilio == null ? null : domicilio.trim();
    }

    /**
     * Agrega una mascota en composicion.
     *
     * @throws EntidadInvalidaException si la mascota es nula, pertenece a otro
     *                                  cliente o ya esta asociada
     */
    public void agregarMascota(Mascota mascota) {
        if (mascota == null) {
            throw new EntidadInvalidaException("La mascota es obligatoria.");
        }
        if (mascota.getCliente() != null && !mascota.getCliente().equals(this)) {
            throw new EntidadInvalidaException("La mascota ya pertenece a otro cliente.");
        }
        if (mascotas.contains(mascota)) {
            throw new EntidadInvalidaException("La mascota ya esta asociada al cliente.");
        }
        mascota.asignarCliente(this);
        mascotas.add(mascota);
    }

    /**
     * Quita una mascota de la composicion.
     *
     * @throws EntidadInvalidaException si la mascota no pertenece a este cliente
     */
    public void removerMascota(Mascota mascota) {
        Mascota encontrada = mascotas.stream()
                .filter(m -> mismaMascota(m, mascota))
                .findFirst()
                .orElse(null);
        if (encontrada == null) {
            throw new EntidadInvalidaException("La mascota no pertenece a este cliente.");
        }
        mascotas.remove(encontrada);
    }

    private static boolean mismaMascota(Mascota una, Mascota otra) {
        if (una.getId() != null && otra.getId() != null) {
            return una.getId().equals(otra.getId());
        }
        return una == otra;
    }

    public Long getId() {
        return id;
    }

    public String getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public String getDomicilio() {
        return domicilio;
    }

    /** Vista inmutable de las mascotas del cliente. */
    public List<Mascota> getMascotas() {
        return Collections.unmodifiableList(mascotas);
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return nombre + " " + apellido + " (DNI " + dni + ")";
    }
}
