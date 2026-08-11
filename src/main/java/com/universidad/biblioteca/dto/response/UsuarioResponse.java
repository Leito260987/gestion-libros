package com.universidad.biblioteca.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/** Representacion publica de un usuario. Nunca incluye la contrasena. */
@Schema(description = "Datos publicos de un usuario")
@Builder
public record UsuarioResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        String estado,
        LocalDateTime fechaRegistro,
        Set<String> roles
) {
}
