package com.veterinaria.service;

import com.veterinaria.dto.RegistroMedicoDto;
import com.veterinaria.dto.TurnoDto;
import com.veterinaria.entity.AplicacionVacuna;
import com.veterinaria.entity.EstadoTurno;
import com.veterinaria.entity.GuarderiaDia;
import com.veterinaria.entity.Mascota;
import com.veterinaria.entity.RegistroMedico;
import com.veterinaria.entity.Servicio;
import com.veterinaria.entity.Turno;
import com.veterinaria.entity.TurnoServicio;
import com.veterinaria.entity.Vacuna;
import com.veterinaria.entity.Veterinario;
import com.veterinaria.exception.EntidadNoEncontradaException;
import com.veterinaria.exception.ReglaNegocioException;
import com.veterinaria.repository.MascotaRepository;
import com.veterinaria.repository.ServicioRepository;
import com.veterinaria.repository.TurnoRepository;
import com.veterinaria.repository.VeterinarioRepository;
import com.veterinaria.util.JpaUtil;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Casos de uso sobre turnos: creacion con las reglas de negocio de agenda
 * (sin solapamientos, cupo de guarderia, vigencia de vacunas), ciclo de vida
 * (confirmar, atender, cancelar), registro de consultas e informes.
 */
public class TurnoService {

    private static final List<EstadoTurno> ESTADOS_ACTIVOS =
            List.of(EstadoTurno.PENDIENTE, EstadoTurno.CONFIRMADO);

    private final TurnoRepository repositorio = new TurnoRepository();
    private final MascotaRepository repositorioMascotas = new MascotaRepository();
    private final VeterinarioRepository repositorioVeterinarios = new VeterinarioRepository();
    private final ServicioRepository repositorioServicios = new ServicioRepository();

    /**
     * Crea un turno pendiente validando:
     * <ul>
     *   <li>la fecha debe ser futura;</li>
     *   <li>la mascota debe estar activa;</li>
     *   <li>no debe solaparse con turnos activos del mismo veterinario o mascota;</li>
     *   <li>no debe superarse el cupo diario de guarderia;</li>
     *   <li>las vacunas a aplicar no deben estar ya vigentes.</li>
     * </ul>
     */
    public TurnoDto crearTurno(Long mascotaId, Long veterinarioId, LocalDateTime fechaHora,
                               List<Long> servicioIds) {
        return JpaUtil.enTransaccion(em -> {
            if (fechaHora.isBefore(LocalDateTime.now())) {
                throw new ReglaNegocioException("No se pueden crear turnos en el pasado.");
            }
            Mascota mascota = repositorioMascotas.buscarPorId(mascotaId, em)
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "No existe la mascota con id " + mascotaId + "."));
            if (!mascota.isActiva()) {
                throw new ReglaNegocioException("La mascota se encuentra inactiva y no puede tomar turnos.");
            }
            Veterinario veterinario = repositorioVeterinarios.buscarPorId(veterinarioId, em)
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "No existe el veterinario con id " + veterinarioId + "."));
            List<Servicio> servicios = cargarServicios(servicioIds, em);

            validarSolapamiento(em, mascota.getId(), veterinario.getId(), fechaHora, servicios, null);
            validarCupoGuarderia(em, fechaHora, servicios);
            validarVigenciaVacunas(em, mascota.getId(), fechaHora, servicios);

            Turno turno = Turno.crear(mascota, veterinario, fechaHora, servicios);
            return TurnoDto.desde(repositorio.guardar(turno, em));
        });
    }

    /** Confirma un turno pendiente. */
    public TurnoDto confirmar(Long id) {
        return JpaUtil.enTransaccion(em -> {
            Turno turno = obtenerTurno(id, em);
            turno.confirmar();
            return TurnoDto.desde(repositorio.guardar(turno, em));
        });
    }

    /** Marca como atendido un turno confirmado. */
    public TurnoDto atender(Long id) {
        return JpaUtil.enTransaccion(em -> {
            Turno turno = obtenerTurno(id, em);
            turno.atender();
            return TurnoDto.desde(repositorio.guardar(turno, em));
        });
    }

    /**
     * Cancela un turno respetando el plazo de 24 horas para confirmados.
     */
    public TurnoDto cancelar(Long id, LocalDateTime momento) {
        return JpaUtil.enTransaccion(em -> {
            Turno turno = obtenerTurno(id, em);
            turno.cancelar(momento);
            return TurnoDto.desde(repositorio.guardar(turno, em));
        });
    }

    public TurnoDto buscar(Long id) {
        return JpaUtil.enTransaccion(em -> TurnoDto.desde(obtenerTurno(id, em)));
    }

    public List<TurnoDto> listarTodos() {
        return JpaUtil.enTransaccion(em ->
                repositorio.listarTodosConDetalles(em).stream().map(TurnoDto::desde).toList());
    }

    /** Agenda del dia: turnos no cancelados ordenados por hora. */
    public List<TurnoDto> listarPorDia(LocalDate dia) {
        return JpaUtil.enTransaccion(em ->
                repositorio.listarPorDia(em, dia).stream().map(TurnoDto::desde).toList());
    }

    /**
     * Agrega un servicio a un turno existente, revalidando las reglas de agenda.
     */
    public TurnoDto agregarServicio(Long turnoId, Long servicioId) {
        return JpaUtil.enTransaccion(em -> {
            Turno turno = obtenerTurno(turnoId, em);
            if (turno.getTurnoServicios().stream()
                    .anyMatch(detalle -> detalle.getServicio().getId().equals(servicioId))) {
                throw new ReglaNegocioException("El turno ya incluye ese servicio.");
            }
            Servicio servicio = repositorioServicios.buscarPorId(servicioId, em)
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "No existe el servicio con id " + servicioId + "."));

            List<Servicio> nuevos = new ArrayList<>(turno.getTurnoServicios().stream()
                    .map(TurnoServicio::getServicio).toList());
            nuevos.add(servicio);

            validarSolapamiento(em, turno.getMascota().getId(), turno.getVeterinario().getId(),
                    turno.getFechaHora(), nuevos, turno);
            validarCupoGuarderia(em, turno.getFechaHora(), List.of(servicio));
            validarVigenciaVacunas(em, turno.getMascota().getId(), turno.getFechaHora(), List.of(servicio));

            turno.agregarServicio(servicio);
            return TurnoDto.desde(repositorio.guardar(turno, em));
        });
    }

    /** Quita un servicio de un turno no atendido. */
    public TurnoDto quitarServicio(Long turnoId, Long turnoServicioId) {
        return JpaUtil.enTransaccion(em -> {
            Turno turno = obtenerTurno(turnoId, em);
            TurnoServicio detalle = turno.getTurnoServicios().stream()
                    .filter(ts -> ts.getId().equals(turnoServicioId))
                    .findFirst()
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "El turno no incluye ese servicio."));
            turno.quitarServicio(detalle);
            return TurnoDto.desde(repositorio.guardar(turno, em));
        });
    }

    /**
     * Registra el diagnostico y tratamiento de un servicio de un turno atendido.
     */
    public RegistroMedicoDto registrarConsulta(Long turnoId, Long turnoServicioId,
                                               String diagnostico, String tratamiento) {
        return JpaUtil.enTransaccion(em -> {
            Turno turno = obtenerTurno(turnoId, em);
            TurnoServicio detalle = turno.getTurnoServicios().stream()
                    .filter(ts -> ts.getId().equals(turnoServicioId))
                    .findFirst()
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "El turno no incluye ese servicio."));
            RegistroMedico registro = RegistroMedico.crear(diagnostico, tratamiento);
            turno.registrarRegistroMedico(detalle, registro);
            Turno guardado = repositorio.guardar(turno, em);
            em.flush();
            TurnoServicio persistido = guardado.getTurnoServicios().stream()
                    .filter(ts -> ts.getId().equals(turnoServicioId))
                    .findFirst()
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "El turno no incluye ese servicio."));
            return RegistroMedicoDto.desde(persistido.getRegistroMedico());
        });
    }

    /** Suma de ingresos de turnos atendidos dentro de un rango de fechas. */
    public BigDecimal ingresosEntre(LocalDateTime desde, LocalDateTime hasta) {
        return JpaUtil.enTransaccion(em -> repositorio.sumarIngresos(em, desde, hasta));
    }

    private Turno obtenerTurno(Long id, EntityManager em) {
        return repositorio.buscarConDetalles(em, id)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el turno con id " + id + "."));
    }

    private List<Servicio> cargarServicios(List<Long> ids, EntityManager em) {
        if (ids == null || ids.isEmpty()) {
            throw new ReglaNegocioException("El turno debe incluir al menos un servicio.");
        }
        Set<Long> unicos = new HashSet<>();
        List<Servicio> servicios = new ArrayList<>();
        for (Long id : ids) {
            if (!unicos.add(id)) {
                throw new ReglaNegocioException("Un servicio no se puede repetir en el mismo turno.");
            }
            servicios.add(repositorioServicios.buscarPorId(id, em)
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "No existe el servicio con id " + id + ".")));
        }
        return servicios;
    }

    private void validarSolapamiento(EntityManager em, Long mascotaId, Long veterinarioId,
                                     LocalDateTime inicio, List<Servicio> servicios, Turno turnoActual) {
        LocalDateTime fin = inicio.plusMinutes(
                servicios.stream().mapToInt(Servicio::getDuracionMinutos).sum());

        List<Turno> candidatosVeterinario =
                repositorio.listarCandidatosSolapados(em, "veterinario", veterinarioId, inicio, fin,
                        ESTADOS_ACTIVOS);
        for (Turno candidato : candidatosVeterinario) {
            if (!esElMismoTurno(candidato, turnoActual) && seSolapan(inicio, fin, candidato)) {
                throw new ReglaNegocioException("El veterinario ya posee un turno en ese horario.");
            }
        }

        List<Turno> candidatosMascota =
                repositorio.listarCandidatosSolapados(em, "mascota", mascotaId, inicio, fin,
                        ESTADOS_ACTIVOS);
        for (Turno candidato : candidatosMascota) {
            if (!esElMismoTurno(candidato, turnoActual) && seSolapan(inicio, fin, candidato)) {
                throw new ReglaNegocioException("La mascota ya posee un turno en ese horario.");
            }
        }
    }

    private boolean esElMismoTurno(Turno candidato, Turno turnoActual) {
        return turnoActual != null && turnoActual.getId() != null
                && turnoActual.getId().equals(candidato.getId());
    }

    private boolean seSolapan(LocalDateTime inicio, LocalDateTime fin, Turno existente) {
        LocalDateTime inicioExistente = existente.getFechaHora();
        LocalDateTime finExistente = inicioExistente.plusMinutes(existente.calcularDuracionTotal());
        return inicio.isBefore(finExistente) && fin.isAfter(inicioExistente);
    }

    private void validarCupoGuarderia(EntityManager em, LocalDateTime fechaHora, List<Servicio> servicios) {
        LocalDate dia = fechaHora.toLocalDate();
        for (Servicio servicio : servicios) {
            if (servicio instanceof GuarderiaDia guarderia) {
                long ocupados = repositorio.contarGuarderiaEnDia(em, dia);
                guarderia.validarCupoDisponible((int) ocupados);
            }
        }
    }

    private void validarVigenciaVacunas(EntityManager em, Long mascotaId, LocalDateTime fechaTurno,
                                        List<Servicio> servicios) {
        LocalDate fechaTurnoDia = fechaTurno.toLocalDate();
        for (Servicio servicio : servicios) {
            if (servicio instanceof AplicacionVacuna aplicacion) {
                Vacuna vacuna = aplicacion.getVacuna();
                LocalDateTime ultima = repositorio.ultimaAplicacionVacuna(em, mascotaId, vacuna.getId());
                if (ultima != null && vacuna.estaVigente(ultima.toLocalDate(), fechaTurnoDia)) {
                    throw new ReglaNegocioException("La vacuna " + vacuna.getNombreComercial()
                            + " ya se encuentra vigente para esta mascota.");
                }
            }
        }
    }
}
