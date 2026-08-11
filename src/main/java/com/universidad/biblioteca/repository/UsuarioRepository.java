package com.universidad.biblioteca.repository;

import com.universidad.biblioteca.entity.Usuario;
import com.universidad.biblioteca.entity.enums.EstadoUsuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Carga el usuario con sus roles en una sola consulta (EntityGraph) para
     * la autenticacion, evitando N+1 al construir las authorities.
     */
    @EntityGraph(attributePaths = "roles")
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRoles_Nombre(com.universidad.biblioteca.entity.enums.RolNombre nombre);

    @Query("select u.estado from Usuario u where u.id = :id")
    Optional<EstadoUsuario> findEstadoById(@Param("id") Long id);
}
