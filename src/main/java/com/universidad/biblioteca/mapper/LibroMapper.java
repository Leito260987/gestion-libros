package com.universidad.biblioteca.mapper;

import com.universidad.biblioteca.dto.request.LibroRequest;
import com.universidad.biblioteca.dto.response.LibroResponse;
import com.universidad.biblioteca.entity.Libro;
import org.springframework.stereotype.Component;

@Component
public class LibroMapper {

    /** Construye una nueva entidad a partir del request (alta). */
    public Libro toEntity(LibroRequest request) {
        Libro libro = Libro.builder()
                .isbn(normalizarIsbn(request.isbn()))
                .titulo(request.titulo())
                .autor(request.autor())
                .editorial(request.editorial())
                .categoria(request.categoria())
                .anioPublicacion(request.anioPublicacion())
                .cantidadTotal(request.cantidadTotal())
                .cantidadDisponible(request.cantidadTotal())
                .build();
        libro.recalcularEstado();
        return libro;
    }

    /**
     * Aplica los cambios del request sobre una entidad existente preservando
     * el numero de prestamos en curso (unidades no disponibles).
     */
    public void updateEntity(Libro libro, LibroRequest request) {
        int prestados = libro.getCantidadTotal() - libro.getCantidadDisponible();
        libro.setIsbn(normalizarIsbn(request.isbn()));
        libro.setTitulo(request.titulo());
        libro.setAutor(request.autor());
        libro.setEditorial(request.editorial());
        libro.setCategoria(request.categoria());
        libro.setAnioPublicacion(request.anioPublicacion());
        libro.setCantidadTotal(request.cantidadTotal());
        // La nueva disponibilidad = total nuevo - prestamos vigentes (nunca negativa)
        libro.setCantidadDisponible(Math.max(0, request.cantidadTotal() - prestados));
        libro.recalcularEstado();
    }

    public LibroResponse toResponse(Libro libro) {
        return LibroResponse.builder()
                .id(libro.getId())
                .isbn(libro.getIsbn())
                .titulo(libro.getTitulo())
                .autor(libro.getAutor())
                .editorial(libro.getEditorial())
                .categoria(libro.getCategoria())
                .anioPublicacion(libro.getAnioPublicacion())
                .cantidadTotal(libro.getCantidadTotal())
                .cantidadDisponible(libro.getCantidadDisponible())
                .estado(libro.getEstado().name())
                .build();
    }

    private String normalizarIsbn(String isbn) {
        return isbn == null ? null : isbn.replaceAll("[\\s-]", "");
    }
}
