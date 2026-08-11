package com.universidad.biblioteca.service;

import com.universidad.biblioteca.dto.response.PageResponse;
import com.universidad.biblioteca.dto.response.UsuarioResponse;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {

    UsuarioResponse obtenerPorId(Long id);

    UsuarioResponse obtenerPorEmail(String email);

    PageResponse<UsuarioResponse> listar(Pageable pageable);

    UsuarioResponse cambiarEstado(Long id, boolean activo);
}
