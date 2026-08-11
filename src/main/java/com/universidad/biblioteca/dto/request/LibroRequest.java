package com.universidad.biblioteca.dto.request;

import com.universidad.biblioteca.validation.ValidIsbn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Datos para crear o actualizar un libro")
public record LibroRequest(

        @Schema(example = "978-3-16-148410-0")
        @NotBlank(message = "El ISBN es obligatorio")
        @ValidIsbn
        String isbn,

        @Schema(example = "Clean Architecture")
        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 200)
        String titulo,

        @Schema(example = "Robert C. Martin")
        @NotBlank(message = "El autor es obligatorio")
        @Size(max = 150)
        String autor,

        @Schema(example = "Prentice Hall")
        @Size(max = 150)
        String editorial,

        @Schema(example = "Ingenieria de Software")
        @Size(max = 80)
        String categoria,

        @Schema(example = "2017")
        @Min(value = 1450, message = "El anio de publicacion no es valido")
        @Max(value = 2100, message = "El anio de publicacion no es valido")
        Integer anioPublicacion,

        @Schema(example = "5")
        @NotNull(message = "La cantidad total es obligatoria")
        @Min(value = 0, message = "La cantidad total no puede ser negativa")
        Integer cantidadTotal
) {
}
