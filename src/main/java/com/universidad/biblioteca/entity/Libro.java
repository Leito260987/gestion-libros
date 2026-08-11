package com.universidad.biblioteca.entity;

import com.universidad.biblioteca.entity.enums.EstadoLibro;
import jakarta.persistence.*;
import lombok.*;

/**
 * Libro del catalogo. Mantiene el control de inventario mediante
 * cantidadTotal y cantidadDisponible. El ISBN es unico.
 */
@Entity
@Table(name = "libro",
        uniqueConstraints = @UniqueConstraint(name = "uk_libro_isbn", columnNames = "isbn"),
        indexes = {
                @Index(name = "idx_libro_isbn", columnList = "isbn"),
                @Index(name = "idx_libro_titulo", columnList = "titulo"),
                @Index(name = "idx_libro_autor", columnList = "autor"),
                @Index(name = "idx_libro_categoria", columnList = "categoria")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(length = 150)
    private String editorial;

    @Column(length = 80)
    private String categoria;

    @Column(name = "anio_publicacion")
    private Integer anioPublicacion;

    @Column(name = "cantidad_total", nullable = false)
    private Integer cantidadTotal;

    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible;

    /** Version para bloqueo optimista adicional (defensa en profundidad). */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoLibro estado = EstadoLibro.DISPONIBLE;

    public boolean hayDisponibilidad() {
        return cantidadDisponible != null && cantidadDisponible > 0;
    }

    /** Ajusta el estado derivado a partir del inventario disponible. */
    public void recalcularEstado() {
        if (estado == EstadoLibro.DESCATALOGADO) {
            return;
        }
        this.estado = hayDisponibilidad() ? EstadoLibro.DISPONIBLE : EstadoLibro.AGOTADO;
    }
}
