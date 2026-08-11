package com.universidad.biblioteca.repository;

import com.universidad.biblioteca.entity.Libro;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    Optional<Libro> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    /**
     * Bloqueo pesimista de escritura sobre la fila del libro. Garantiza que
     * el ajuste de cantidadDisponible en prestamos/devoluciones concurrentes
     * sea consistente (evita condiciones de carrera / lost update).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Libro l where l.id = :id")
    Optional<Libro> findByIdForUpdate(@Param("id") Long id);

    /**
     * Busqueda por catalogo con filtros opcionales combinables y paginacion.
     * Los parametros nulos se ignoran, permitiendo cualquier combinacion.
     */
    @Query("""
            select l from Libro l
            where (:titulo    is null or lower(l.titulo)    like lower(concat('%', :titulo, '%')))
              and (:autor     is null or lower(l.autor)     like lower(concat('%', :autor, '%')))
              and (:categoria is null or lower(l.categoria) like lower(concat('%', :categoria, '%')))
              and (:soloDisponibles = false or l.cantidadDisponible > 0)
            """)
    Page<Libro> buscarCatalogo(@Param("titulo") String titulo,
                               @Param("autor") String autor,
                               @Param("categoria") String categoria,
                               @Param("soloDisponibles") boolean soloDisponibles,
                               Pageable pageable);
}
