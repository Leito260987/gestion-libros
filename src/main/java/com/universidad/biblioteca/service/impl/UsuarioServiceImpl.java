package com.universidad.biblioteca.service.impl;

import com.universidad.biblioteca.dto.response.PageResponse;
import com.universidad.biblioteca.dto.response.UsuarioResponse;
import com.universidad.biblioteca.entity.Usuario;
import com.universidad.biblioteca.entity.enums.EstadoUsuario;
import com.universidad.biblioteca.exception.ResourceNotFoundException;
import com.universidad.biblioteca.mapper.UsuarioMapper;
import com.universidad.biblioteca.repository.UsuarioRepository;
import com.universidad.biblioteca.service.UsuarioService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.of("Usuario", id));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(usuarioMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listar(Pageable pageable) {
        return PageResponse.from(usuarioRepository.findAll(pageable).map(usuarioMapper::toResponse));
    }

    @Override
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Usuario", id));
        usuario.setEstado(activo ? EstadoUsuario.ACTIVO : EstadoUsuario.INACTIVO);
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }
}
