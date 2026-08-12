package com.veterinaria.service;

import com.veterinaria.dto.AlertaVacunaDto;
import com.veterinaria.entity.EstadoVacuna;
import com.veterinaria.entity.Mascota;
import com.veterinaria.entity.Vacuna;
import com.veterinaria.repository.MascotaRepository;
import com.veterinaria.repository.TurnoRepository;
import com.veterinaria.repository.VacunaRepository;
import com.veterinaria.util.JpaUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Control de vacunaciones: lista las mascotas con vacunas vencidas o proximas
 * a vencer dentro de una ventana de dias, calculadas desde la ultima aplicacion
 * registrada en un turno atendido.
 */
public class VacunaAlertaService {

    private static final long DIAS_ALERTA = 30;

    private final TurnoRepository repositorioTurnos = new TurnoRepository();
    private final MascotaRepository repositorioMascotas = new MascotaRepository();
    private final VacunaRepository repositorioVacunas = new VacunaRepository();

    /**
     * Alertas de vacunacion para hoy: solo vacunas VENCIDAS o PROXIMAS A
     * VENCER dentro de los proximos {@value #DIAS_ALERTA} dias, ordenadas de
     * mas urgente a menos urgente.
     */
    public List<AlertaVacunaDto> listarAlertas() {
        return listarAlertas(LocalDate.now(), DIAS_ALERTA);
    }

    /**
     * Idem {@link #listarAlertas()} con fecha de referencia y ventana
     * configurables (facilita los tests).
     */
    public List<AlertaVacunaDto> listarAlertas(LocalDate referencia, long diasAlerta) {
        return JpaUtil.enTransaccion(em -> {
            Map<Long, Mascota> mascotas = new HashMap<>();
            repositorioMascotas.listarTodas(em).forEach(m -> mascotas.put(m.getId(), m));
            Map<Long, Vacuna> vacunas = new HashMap<>();
            repositorioVacunas.listarTodas(em).forEach(v -> vacunas.put(v.getId(), v));

            List<AlertaVacunaDto> alertas = new ArrayList<>();
            for (Object[] fila : repositorioTurnos.ultimasAplicaciones(em)) {
                Long mascotaId = (Long) fila[0];
                Long vacunaId = (Long) fila[1];
                LocalDateTime ultimaFecha = (LocalDateTime) fila[2];

                Mascota mascota = mascotas.get(mascotaId);
                Vacuna vacuna = vacunas.get(vacunaId);
                if (mascota == null || vacuna == null || ultimaFecha == null) {
                    continue;
                }
                LocalDate ultima = ultimaFecha.toLocalDate();
                EstadoVacuna estado = vacuna.estadoAlerta(ultima, referencia, diasAlerta);
                if (estado == EstadoVacuna.AL_DIA) {
                    continue;
                }
                LocalDate proxima = vacuna.proximaAplicacion(ultima);
                alertas.add(new AlertaVacunaDto(mascotaId, mascota.getFicha(), mascota.getNombre(),
                        mascota.getCliente().getNombreCompleto(), vacunaId, vacuna.getNombreComercial(),
                        vacuna.getEnfermedadPrevenida(), ultima, proxima, estado,
                        ChronoUnit.DAYS.between(referencia, proxima)));
            }
            alertas.sort(Comparator.comparingLong(AlertaVacunaDto::diasRestantes));
            return alertas;
        });
    }
}
