package com.universidad.biblioteca.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Solicitud de prestamo. El usuarioId solo lo puede fijar un ADMIN; " +
        "para un USER se ignora y se usa su propia identidad.")
public record PrestamoRequest(

        @Schema(example = "1", description = "ID del libro a prestar")
        @NotNull(message = "El libroId es obligatorio")
        @Positive(message = "El libroId debe ser positivo")
        Long libroId,

        @Schema(example = "2", description = "ID del usuario (solo ADMIN). Opcional para USER.")
        @Positive(message = "El usuarioId debe ser positivo")
        Long usuarioId
) {
}
