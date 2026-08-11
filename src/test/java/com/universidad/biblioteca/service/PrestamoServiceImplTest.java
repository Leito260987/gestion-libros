package com.universidad.biblioteca.service;

import com.universidad.biblioteca.config.AppProperties;
import com.universidad.biblioteca.dto.request.PrestamoRequest;
import com.universidad.biblioteca.entity.Libro;
import com.universidad.biblioteca.entity.Prestamo;
import com.universidad.biblioteca.entity.Usuario;
import com.universidad.biblioteca.entity.enums.EstadoPrestamo;
import com.universidad.biblioteca.entity.enums.EstadoUsuario;
import com.universidad.biblioteca.exception.AccessDeniedBusinessException;
import com.universidad.biblioteca.exception.BusinessRuleException;
import com.universidad.biblioteca.mapper.PrestamoMapper;
import com.universidad.biblioteca.repository.LibroRepository;
import com.universidad.biblioteca.repository.PrestamoRepository;
import com.universidad.biblioteca.repository.UsuarioRepository;
import com.universidad.biblioteca.service.impl.PrestamoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de las reglas de negocio de prestamos, aisladas con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class PrestamoServiceImplTest {

    @Mock PrestamoRepository prestamoRepository;
    @Mock LibroRepository libroRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PrestamoMapper prestamoMapper;

    PrestamoServiceImpl service;

    Usuario usuarioActivo;
    Libro libroDisponible;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties(
                new AppProperties.Admin("admin@biblioteca.edu", "x"),
                new AppProperties.Prestamo(14));
        service = new PrestamoServiceImpl(prestamoRepository, libroRepository,
                usuarioRepository, prestamoMapper, props);

        usuarioActivo = Usuario.builder().id(1L).nombre("Ana").apellido("Diaz")
                .estado(EstadoUsuario.ACTIVO).build();
        libroDisponible = Libro.builder().id(10L).titulo("Clean Code")
                .cantidadTotal(3).cantidadDisponible(2).build();
    }

    @Test
    @DisplayName("Prestar decrementa la disponibilidad y persiste un prestamo ACTIVO")
    void crear_exitoso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioActivo));
        when(libroRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(libroDisponible));
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        service.crear(new PrestamoRequest(10L, null), 1L, false);

        assertThat(libroDisponible.getCantidadDisponible()).isEqualTo(1);
        verify(prestamoRepository).save(argThat(p ->
                p.getEstado() == EstadoPrestamo.ACTIVO
                        && p.getFechaVencimiento().equals(p.getFechaPrestamo().plusDays(14))));
    }

    @Test
    @DisplayName("No permite prestar un libro sin ejemplares disponibles")
    void crear_sinDisponibilidad() {
        libroDisponible.setCantidadDisponible(0);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioActivo));
        when(libroRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(libroDisponible));

        assertThatThrownBy(() -> service.crear(new PrestamoRequest(10L, null), 1L, false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no tiene ejemplares disponibles");

        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("No permite prestar a un usuario inactivo")
    void crear_usuarioInactivo() {
        usuarioActivo.setEstado(EstadoUsuario.INACTIVO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioActivo));

        assertThatThrownBy(() -> service.crear(new PrestamoRequest(10L, null), 1L, false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("usuario inactivo");
    }

    @Test
    @DisplayName("No permite devolver dos veces el mismo prestamo")
    void devolver_yaDevuelto() {
        Prestamo prestamo = Prestamo.builder().id(5L).usuario(usuarioActivo).libro(libroDisponible)
                .estado(EstadoPrestamo.DEVUELTO).fechaDevolucion(LocalDate.now()).build();
        when(prestamoRepository.findDetalleById(5L)).thenReturn(Optional.of(prestamo));

        assertThatThrownBy(() -> service.devolver(5L, 1L, false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya fue devuelto");
    }

    @Test
    @DisplayName("Un USER no puede devolver el prestamo de otro usuario")
    void devolver_deOtroUsuario() {
        Usuario otro = Usuario.builder().id(99L).build();
        Prestamo prestamo = Prestamo.builder().id(5L).usuario(otro).libro(libroDisponible)
                .estado(EstadoPrestamo.ACTIVO).build();
        when(prestamoRepository.findDetalleById(5L)).thenReturn(Optional.of(prestamo));

        assertThatThrownBy(() -> service.devolver(5L, 1L, false))
                .isInstanceOf(AccessDeniedBusinessException.class);
    }

    @Test
    @DisplayName("Devolver un prestamo activo lo marca DEVUELTO y reincorpora el ejemplar")
    void devolver_exitoso() {
        Prestamo prestamo = Prestamo.builder().id(5L).usuario(usuarioActivo).libro(libroDisponible)
                .estado(EstadoPrestamo.ACTIVO).build();
        when(prestamoRepository.findDetalleById(5L)).thenReturn(Optional.of(prestamo));
        when(libroRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(libroDisponible));
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        service.devolver(5L, 1L, false);

        assertThat(prestamo.getEstado()).isEqualTo(EstadoPrestamo.DEVUELTO);
        assertThat(prestamo.getFechaDevolucion()).isEqualTo(LocalDate.now());
        assertThat(libroDisponible.getCantidadDisponible()).isEqualTo(3);
    }
}
