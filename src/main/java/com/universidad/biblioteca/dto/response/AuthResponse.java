package com.universidad.biblioteca.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;

@Schema(description = "Respuesta de autenticacion con los tokens emitidos")
@Builder
public record AuthResponse(

        @Schema(description = "JWT de acceso (corta duracion)")
        String accessToken,

        @Schema(description = "JWT de refresco (larga duracion)")
        String refreshToken,

        @Schema(example = "Bearer")
        String tokenType,

        @Schema(description = "Segundos hasta la expiracion del access token")
        long expiresIn,

        @Schema(example = "leonardo@biblioteca.edu")
        String email,

        @Schema(description = "Roles del usuario", example = "[\"ROLE_USER\"]")
        Set<String> roles
) {
}
