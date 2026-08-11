package com.universidad.biblioteca.entity;

import com.universidad.biblioteca.entity.enums.RolNombre;
import jakarta.persistence.*;
import lombok.*;

/**
 * Rol de autorizacion. Relacion muchos-a-muchos con Usuario a traves de
 * la tabla intermedia usuario_rol.
 */
@Entity
@Table(name = "rol",
        uniqueConstraints = @UniqueConstraint(name = "uk_rol_nombre", columnNames = "nombre"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolNombre nombre;

    public Rol(RolNombre nombre) {
        this.nombre = nombre;
    }
}
