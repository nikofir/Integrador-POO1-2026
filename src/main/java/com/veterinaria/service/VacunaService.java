package com.veterinaria.service;

import com.veterinaria.dto.VacunaDto;
import com.veterinaria.entity.Vacuna;
import com.veterinaria.exception.EntidadNoEncontradaException;
import com.veterinaria.exception.ReglaNegocioException;
import com.veterinaria.repository.VacunaRepository;
import com.veterinaria.util.JpaUtil;

import java.util.List;

/**
 * Casos de uso sobre vacunas: alta (nombre comercial unico), consulta y listado.
 */
public class VacunaService {

    private final VacunaRepository repositorio = new VacunaRepository();

    /**
     * Da de alta una vacuna validando que el nombre comercial sea unico.
     *
     * @throws ReglaNegocioException si ya existe una vacuna con el mismo nombre comercial
     */
    public VacunaDto registrar(VacunaDto datos) {
        return JpaUtil.enTransaccion(em -> {
            if (repositorio.buscarPorNombreComercial(em, datos.nombreComercial()) != null) {
                throw new ReglaNegocioException(
                        "Ya existe una vacuna con el nombre comercial " + datos.nombreComercial() + ".");
            }
            Vacuna vacuna = Vacuna.crear(datos.nombreComercial(), datos.enfermedadPrevenida(),
                    datos.periodicidadMeses());
            return VacunaDto.desde(repositorio.guardar(vacuna, em));
        });
    }

    public VacunaDto buscar(Long id) {
        return JpaUtil.enTransaccion(em -> {
            Vacuna vacuna = repositorio.buscarPorId(id, em)
                    .orElseThrow(() -> new EntidadNoEncontradaException("No existe la vacuna con id " + id + "."));
            return VacunaDto.desde(vacuna);
        });
    }

    public List<VacunaDto> listar() {
        return JpaUtil.enTransaccion(em ->
                repositorio.listarTodas(em).stream().map(VacunaDto::desde).toList());
    }
}
