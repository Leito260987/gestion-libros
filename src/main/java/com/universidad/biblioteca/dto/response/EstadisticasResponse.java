package com.universidad.biblioteca.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Schema(description = "Estadisticas basicas de prestamos")
@Builder
public record EstadisticasResponse(

        @Schema(description = "Cantidad de prestamos por estado")
        Map<String, Long> prestamosPorEstado,

        @Schema(description = "Libros mas prestados (titulo -> numero de prestamos)")
        List<LibroPrestamos> librosMasPrestados
) {
    @Builder
    public record LibroPrestamos(String titulo, long prestamos) {
    }
}
