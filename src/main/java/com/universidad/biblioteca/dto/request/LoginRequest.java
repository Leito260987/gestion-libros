package com.universidad.biblioteca.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales de acceso")
public record LoginRequest(

        @Schema(example = "admin@biblioteca.edu")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        @Schema(example = "Admin123!")
        @NotBlank(message = "La contrasena es obligatoria")
        String password
) {
}
