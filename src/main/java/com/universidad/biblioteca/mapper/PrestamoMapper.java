package com.universidad.biblioteca.mapper;

import com.universidad.biblioteca.dto.response.PrestamoResponse;
import com.universidad.biblioteca.entity.Prestamo;
import org.springframework.stereotype.Component;

@Component
public class PrestamoMapper {

    /**
     * Requiere que usuario y libro esten inicializados (las consultas del
     * repositorio usan JOIN FETCH para garantizarlo y evitar N+1).
     */
    public PrestamoResponse toResponse(Prestamo prestamo) {
        var usuario = prestamo.getUsuario();
        var libro = prestamo.getLibro();
        return PrestamoResponse.builder()
                .id(prestamo.getId())
                .usuarioId(usuario.getId())
                .usuarioNombre(usuario.getNombre() + " " + usuario.getApellido())
                .libroId(libro.getId())
                .libroTitulo(libro.getTitulo())
                .libroIsbn(libro.getIsbn())
                .fechaPrestamo(prestamo.getFechaPrestamo())
                .fechaVencimiento(prestamo.getFechaVencimiento())
                .fechaDevolucion(prestamo.getFechaDevolucion())
                .estado(prestamo.getEstado().name())
                .build();
    }
}
