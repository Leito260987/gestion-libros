package com.universidad.biblioteca.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/** Estructura de error uniforme para todas las respuestas de la API. */
@Schema(description = "Respuesta de error estandar")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> details
) {
    @Schema(description = "Detalle de error de validacion por campo")
    @Builder
    public record FieldErrorDetail(String field, String message) {
    }
}
