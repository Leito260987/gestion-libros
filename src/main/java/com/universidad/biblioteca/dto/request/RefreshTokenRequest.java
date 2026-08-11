package com.universidad.biblioteca.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud de renovacion de access token")
public record RefreshTokenRequest(

        @Schema(description = "Refresh token emitido en el login")
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken
) {
}
