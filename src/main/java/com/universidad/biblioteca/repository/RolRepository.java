package com.universidad.biblioteca.repository;

import com.universidad.biblioteca.entity.Rol;
import com.universidad.biblioteca.entity.enums.RolNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(RolNombre nombre);

    boolean existsByNombre(RolNombre nombre);
}
