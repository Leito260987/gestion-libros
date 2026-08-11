package com.universidad.biblioteca.mapper;

import com.universidad.biblioteca.dto.response.UsuarioResponse;
import com.universidad.biblioteca.entity.Rol;
import com.universidad.biblioteca.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .estado(usuario.getEstado().name())
                .fechaRegistro(usuario.getFechaRegistro())
                .roles(mapRoles(usuario.getRoles()))
                .build();
    }

    public Set<String> mapRoles(Set<Rol> roles) {
        return roles.stream()
                .map(rol -> rol.getNombre().name())
                .collect(Collectors.toSet());
    }
}
