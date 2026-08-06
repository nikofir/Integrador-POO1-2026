package com.veterinaria.service;

import com.veterinaria.dto.VeterinarioDto;
import com.veterinaria.entity.Especialidad;
import com.veterinaria.entity.EstadoTurno;
import com.veterinaria.entity.Veterinario;
import com.veterinaria.exception.EntidadInvalidaException;
import com.veterinaria.exception.EntidadNoEncontradaException;
import com.veterinaria.exception.ReglaNegocioException;
import com.veterinaria.repository.TurnoRepository;
import com.veterinaria.repository.VeterinarioRepository;
import com.veterinaria.util.JpaUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Casos de uso sobre veterinarios: alta (matricula unica), consulta,
 * modificacion de datos y especialidades, y baja condicionada.
 */
public class VeterinarioService {

    private final VeterinarioRepository repositorio = new VeterinarioRepository();
    private final TurnoRepository repositorioTurnos = new TurnoRepository();

    /**
     * Da de alta un veterinario validando que la matricula sea unica.
     *
     * @throws ReglaNegocioException si ya existe un veterinario con la misma matricula
     */
    public VeterinarioDto registrar(VeterinarioDto datos) {
        return JpaUtil.enTransaccion(em -> {
            if (repositorio.buscarPorMatricula(em, datos.matricula()) != null) {
                throw new ReglaNegocioException(
                        "Ya existe un veterinario con la matricula " + datos.matricula() + ".");
            }
            Veterinario veterinario = Veterinario.crear(datos.matricula(), datos.nombre(), datos.apellido(),
                    convertirEspecialidades(datos.especialidades()));
            return VeterinarioDto.desde(repositorio.guardar(veterinario, em));
        });
    }

    /**
     * Actualiza datos y especialidades de un veterinario. La matricula es inmutable.
     */
    public VeterinarioDto actualizar(Long id, VeterinarioDto datos) {
        return JpaUtil.enTransaccion(em -> {
            Veterinario veterinario = obtener(id, em);
            if (!veterinario.getMatricula().equals(datos.matricula())) {
                throw new ReglaNegocioException("La matricula de un veterinario no se puede modificar.");
            }
            veterinario.actualizarDatos(datos.nombre(), datos.apellido());
            reconciliarEspecialidades(veterinario, convertirEspecialidades(datos.especialidades()));
            return VeterinarioDto.desde(repositorio.guardar(veterinario, em));
        });
    }

    public VeterinarioDto buscar(Long id) {
        return JpaUtil.enTransaccion(em -> VeterinarioDto.desde(obtener(id, em)));
    }

    /**
     * Busca por matricula. Devuelve {@code null} si no existe.
     */
    public VeterinarioDto buscarPorMatricula(String matricula) {
        return JpaUtil.enTransaccion(em -> {
            Veterinario veterinario = repositorio.buscarPorMatricula(em, matricula);
            return veterinario == null ? null : VeterinarioDto.desde(veterinario);
        });
    }

    public List<VeterinarioDto> listar() {
        return JpaUtil.enTransaccion(em ->
                repositorio.listarTodosConEspecialidades(em).stream().map(VeterinarioDto::desde).toList());
    }

    public void agregarEspecialidad(Long id, Especialidad especialidad) {
        JpaUtil.enTransaccion(em -> {
            obtener(id, em).agregarEspecialidad(especialidad);
            return null;
        });
    }

    public void removerEspecialidad(Long id, Especialidad especialidad) {
        JpaUtil.enTransaccion(em -> {
            obtener(id, em).removerEspecialidad(especialidad);
            return null;
        });
    }

    /**
     * Elimina un veterinario. No se permite si tiene turnos pendientes o confirmados.
     *
     * @throws ReglaNegocioException si el veterinario tiene turnos activos
     */
    public void eliminar(Long id) {
        JpaUtil.enTransaccion(em -> {
            Veterinario veterinario = obtener(id, em);
            boolean conTurnosActivos = repositorioTurnos.listarTodosConDetalles(em).stream()
                    .anyMatch(t -> t.getVeterinario().getId().equals(id)
                            && (t.getEstado() == EstadoTurno.PENDIENTE
                            || t.getEstado() == EstadoTurno.CONFIRMADO));
            if (conTurnosActivos) {
                throw new ReglaNegocioException(
                        "No se puede eliminar un veterinario con turnos pendientes o confirmados.");
            }
            repositorio.eliminar(veterinario, em);
            return null;
        });
    }

    private Veterinario obtener(Long id, jakarta.persistence.EntityManager em) {
        return repositorio.buscarPorId(id, em)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No existe el veterinario con id " + id + "."));
    }

    private void reconciliarEspecialidades(Veterinario veterinario, Set<Especialidad> objetivo) {
        Set<Especialidad> actuales = new HashSet<>(veterinario.getEspecialidades());
        for (Especialidad especialidad : actuales) {
            if (!objetivo.contains(especialidad)) {
                veterinario.removerEspecialidad(especialidad);
            }
        }
        for (Especialidad especialidad : objetivo) {
            if (!actuales.contains(especialidad)) {
                veterinario.agregarEspecialidad(especialidad);
            }
        }
    }

    private Set<Especialidad> convertirEspecialidades(Set<String> nombres) {
        if (nombres == null || nombres.isEmpty()) {
            throw new ReglaNegocioException("El veterinario debe tener al menos una especialidad.");
        }
        Set<Especialidad> resultado = new HashSet<>();
        for (String nombre : nombres) {
            Especialidad especialidad = Especialidad.porEtiqueta(nombre);
            if (especialidad == null) {
                throw new EntidadInvalidaException("Especialidad invalida: " + nombre + ".");
            }
            resultado.add(especialidad);
        }
        return resultado;
    }
}
