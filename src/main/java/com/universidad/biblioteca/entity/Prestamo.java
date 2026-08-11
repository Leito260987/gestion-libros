package com.universidad.biblioteca.entity;

import com.universidad.biblioteca.entity.enums.EstadoPrestamo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Prestamo de un libro a un usuario. Ambas relaciones son LAZY para evitar
 * cargas innecesarias y problemas N+1; las consultas que necesitan los datos
 * asociados usan JOIN FETCH explicito.
 */
@Entity
@Table(name = "prestamo",
        indexes = {
                @Index(name = "idx_prestamo_usuario", columnList = "usuario_id"),
                @Index(name = "idx_prestamo_libro", columnList = "libro_id"),
                @Index(name = "idx_prestamo_estado", columnList = "estado"),
                @Index(name = "idx_prestamo_vencimiento", columnList = "fecha_vencimiento")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_prestamo_usuario"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "libro_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_prestamo_libro"))
    private Libro libro;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;

    public boolean estaDevuelto() {
        return estado == EstadoPrestamo.DEVUELTO;
    }

    /** Un prestamo activo esta vencido si su fecha de vencimiento ya paso. */
    public boolean estaVencido(LocalDate referencia) {
        return !estaDevuelto() && fechaVencimiento.isBefore(referencia);
    }
}
