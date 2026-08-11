package com.universidad.biblioteca.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Datos de un libro del catalogo")
@Builder
public record LibroResponse(
        Long id,
        String isbn,
        String titulo,
        String autor,
        String editorial,
        String categoria,
        Integer anioPublicacion,
        Integer cantidadTotal,
        Integer cantidadDisponible,
        String estado
) {
}
