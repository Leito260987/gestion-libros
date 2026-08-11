package com.universidad.biblioteca.service;

import com.universidad.biblioteca.dto.request.LibroRequest;
import com.universidad.biblioteca.dto.response.LibroResponse;
import com.universidad.biblioteca.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface LibroService {

    LibroResponse crear(LibroRequest request);

    LibroResponse actualizar(Long id, LibroRequest request);

    void eliminar(Long id);

    LibroResponse obtenerPorId(Long id);

    LibroResponse obtenerPorIsbn(String isbn);

    PageResponse<LibroResponse> buscarCatalogo(String titulo, String autor, String categoria,
                                               Boolean soloDisponibles, Pageable pageable);
}
