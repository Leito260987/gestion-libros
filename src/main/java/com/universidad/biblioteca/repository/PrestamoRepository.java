package com.universidad.biblioteca.repository;

import com.universidad.biblioteca.entity.Prestamo;
import com.universidad.biblioteca.entity.enums.EstadoPrestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    /**
     * Carga un prestamo con usuario y libro en una sola consulta (JOIN FETCH)
     * para poder mapearlo a DTO sin disparar consultas adicionales (N+1).
     */
    @Query("""
            select p from Prestamo p
            join fetch p.usuario u
            join fetch p.libro l
            where p.id = :id
            """)
    Optional<Prestamo> findDetalleById(@Param("id") Long id);

    /** Historial paginado de un usuario, con el libro cargado por JOIN FETCH. */
    @Query(value = """
            select p from Prestamo p
            join fetch p.libro l
            where p.usuario.id = :usuarioId
            """,
            countQuery = "select count(p) from Prestamo p where p.usuario.id = :usuarioId")
    Page<Prestamo> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query(value = """
            select p from Prestamo p
            join fetch p.usuario u
            join fetch p.libro l
            where p.estado = :estado
            """,
            countQuery = "select count(p) from Prestamo p where p.estado = :estado")
    Page<Prestamo> findByEstado(@Param("estado") EstadoPrestamo estado, Pageable pageable);

    /** Prestamos activos cuya fecha de vencimiento ya expiro. */
    @Query("""
            select p from Prestamo p
            join fetch p.usuario u
            join fetch p.libro l
            where p.estado = com.universidad.biblioteca.entity.enums.EstadoPrestamo.ACTIVO
              and p.fechaVencimiento < :hoy
            order by p.fechaVencimiento asc
            """)
    List<Prestamo> findVencidos(@Param("hoy") LocalDate hoy);

    long countByUsuarioIdAndEstado(Long usuarioId, EstadoPrestamo estado);

    boolean existsByLibroIdAndEstado(Long libroId, EstadoPrestamo estado);

    // ---- Estadisticas basicas ----

    @Query("select p.estado, count(p) from Prestamo p group by p.estado")
    List<Object[]> contarPorEstado();

    @Query("""
            select l.titulo, count(p)
            from Prestamo p join p.libro l
            group by l.id, l.titulo
            order by count(p) desc
            """)
    List<Object[]> librosMasPrestados(Pageable pageable);
}
