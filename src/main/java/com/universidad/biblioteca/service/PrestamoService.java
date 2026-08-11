package com.universidad.biblioteca.service;

import com.universidad.biblioteca.dto.request.PrestamoRequest;
import com.universidad.biblioteca.dto.response.EstadisticasResponse;
import com.universidad.biblioteca.dto.response.PageResponse;
import com.universidad.biblioteca.dto.response.PrestamoResponse;
import com.universidad.biblioteca.entity.enums.EstadoPrestamo;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PrestamoService {

    /**
     * Registra un prestamo. El solicitante es el usuario autenticado; un ADMIN
     * puede prestar en nombre de otro usuario indicando usuarioId en el request.
     */
    PrestamoResponse crear(PrestamoRequest request, Long solicitanteId, boolean esAdmin);

    /** Registra la devolucion, verificando propiedad si el solicitante no es ADMIN. */
    PrestamoResponse devolver(Long prestamoId, Long solicitanteId, boolean esAdmin);

    PrestamoResponse obtener(Long prestamoId, Long solicitanteId, boolean esAdmin);

    PageResponse<PrestamoResponse> historialUsuario(Long usuarioId, Pageable pageable);

    PageResponse<PrestamoResponse> listarPorEstado(EstadoPrestamo estado, Pageable pageable);

    List<PrestamoResponse> listarVencidos();

    EstadisticasResponse estadisticas();
}
