package com.universidad.biblioteca.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Schema(description = "Datos de un prestamo")
@Builder
public record PrestamoResponse(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        Long libroId,
        String libroTitulo,
        String libroIsbn,
        LocalDate fechaPrestamo,
        LocalDate fechaVencimiento,
        LocalDate fechaDevolucion,
        String estado
) {
}
