package com.universidad.biblioteca.service.impl;

import com.universidad.biblioteca.config.AppProperties;
import com.universidad.biblioteca.dto.request.PrestamoRequest;
import com.universidad.biblioteca.dto.response.EstadisticasResponse;
import com.universidad.biblioteca.dto.response.PageResponse;
import com.universidad.biblioteca.dto.response.PrestamoResponse;
import com.universidad.biblioteca.entity.Libro;
import com.universidad.biblioteca.entity.Prestamo;
import com.universidad.biblioteca.entity.Usuario;
import com.universidad.biblioteca.entity.enums.EstadoPrestamo;
import com.universidad.biblioteca.exception.AccessDeniedBusinessException;
import com.universidad.biblioteca.exception.BusinessRuleException;
import com.universidad.biblioteca.exception.ResourceNotFoundException;
import com.universidad.biblioteca.mapper.PrestamoMapper;
import com.universidad.biblioteca.repository.LibroRepository;
import com.universidad.biblioteca.repository.PrestamoRepository;
import com.universidad.biblioteca.repository.UsuarioRepository;
import com.universidad.biblioteca.service.PrestamoService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PrestamoServiceImpl implements PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrestamoMapper prestamoMapper;
    private final AppProperties appProperties;

    public PrestamoServiceImpl(PrestamoRepository prestamoRepository,
                               LibroRepository libroRepository,
                               UsuarioRepository usuarioRepository,
                               PrestamoMapper prestamoMapper,
                               AppProperties appProperties) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
        this.prestamoMapper = prestamoMapper;
        this.appProperties = appProperties;
    }

    @Override
    @Transactional
    public PrestamoResponse crear(PrestamoRequest request, Long solicitanteId, boolean esAdmin) {
        // Un USER solo puede prestarse a si mismo; un ADMIN puede indicar otro usuario.
        Long usuarioObjetivo = (esAdmin && request.usuarioId() != null)
                ? request.usuarioId() : solicitanteId;

        Usuario usuario = usuarioRepository.findById(usuarioObjetivo)
                .orElseThrow(() -> ResourceNotFoundException.of("Usuario", usuarioObjetivo));

        if (!usuario.isActivo()) {
            throw new BusinessRuleException("No se puede registrar un prestamo para un usuario inactivo");
        }

        // Bloqueo pesimista sobre el libro: serializa el ajuste de disponibilidad
        // frente a prestamos/devoluciones concurrentes del mismo ejemplar.
        Libro libro = libroRepository.findByIdForUpdate(request.libroId())
                .orElseThrow(() -> ResourceNotFoundException.of("Libro", request.libroId()));

        if (!libro.hayDisponibilidad()) {
            throw new BusinessRuleException("El libro '" + libro.getTitulo() + "' no tiene ejemplares disponibles");
        }

        libro.setCantidadDisponible(libro.getCantidadDisponible() - 1);
        libro.recalcularEstado();

        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = Prestamo.builder()
                .usuario(usuario)
                .libro(libro)
                .fechaPrestamo(hoy)
                .fechaVencimiento(hoy.plusDays(appProperties.prestamo().diasVencimiento()))
                .estado(EstadoPrestamo.ACTIVO)
                .build();

        return prestamoMapper.toResponse(prestamoRepository.save(prestamo));
    }

    @Override
    @Transactional
    public PrestamoResponse devolver(Long prestamoId, Long solicitanteId, boolean esAdmin) {
        Prestamo prestamo = prestamoRepository.findDetalleById(prestamoId)
                .orElseThrow(() -> ResourceNotFoundException.of("Prestamo", prestamoId));

        verificarPropiedad(prestamo, solicitanteId, esAdmin);

        if (prestamo.estaDevuelto()) {
            throw new BusinessRuleException("El prestamo ya fue devuelto anteriormente");
        }

        // Reincorpora el ejemplar bajo bloqueo pesimista.
        Libro libro = libroRepository.findByIdForUpdate(prestamo.getLibro().getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Libro", prestamo.getLibro().getId()));
        libro.setCantidadDisponible(libro.getCantidadDisponible() + 1);
        libro.recalcularEstado();

        prestamo.setFechaDevolucion(LocalDate.now());
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);

        return prestamoMapper.toResponse(prestamoRepository.save(prestamo));
    }

    @Override
    @Transactional(readOnly = true)
    public PrestamoResponse obtener(Long prestamoId, Long solicitanteId, boolean esAdmin) {
        Prestamo prestamo = prestamoRepository.findDetalleById(prestamoId)
                .orElseThrow(() -> ResourceNotFoundException.of("Prestamo", prestamoId));
        verificarPropiedad(prestamo, solicitanteId, esAdmin);
        return prestamoMapper.toResponse(prestamo);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PrestamoResponse> historialUsuario(Long usuarioId, Pageable pageable) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw ResourceNotFoundException.of("Usuario", usuarioId);
        }
        return PageResponse.from(
                prestamoRepository.findByUsuarioId(usuarioId, pageable).map(prestamoMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PrestamoResponse> listarPorEstado(EstadoPrestamo estado, Pageable pageable) {
        return PageResponse.from(
                prestamoRepository.findByEstado(estado, pageable).map(prestamoMapper::toResponse));
    }

    @Override
    @Transactional
    public List<PrestamoResponse> listarVencidos() {
        List<Prestamo> vencidos = prestamoRepository.findVencidos(LocalDate.now());
        // Efecto de negocio: marca como VENCIDO los prestamos activos ya expirados.
        vencidos.forEach(p -> p.setEstado(EstadoPrestamo.VENCIDO));
        return vencidos.stream().map(prestamoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasResponse estadisticas() {
        Map<String, Long> porEstado = prestamoRepository.contarPorEstado().stream()
                .collect(Collectors.toMap(
                        row -> ((EstadoPrestamo) row[0]).name(),
                        row -> (Long) row[1]));

        List<EstadisticasResponse.LibroPrestamos> top = prestamoRepository
                .librosMasPrestados(Pageable.ofSize(5)).stream()
                .map(row -> EstadisticasResponse.LibroPrestamos.builder()
                        .titulo((String) row[0])
                        .prestamos((Long) row[1])
                        .build())
                .toList();

        return EstadisticasResponse.builder()
                .prestamosPorEstado(porEstado)
                .librosMasPrestados(top)
                .build();
    }

    /**
     * Regla de acceso por propietario: un usuario no ADMIN solo puede operar
     * sobre sus propios prestamos, aunque cambie el id en la URL.
     */
    private void verificarPropiedad(Prestamo prestamo, Long solicitanteId, boolean esAdmin) {
        if (!esAdmin && !prestamo.getUsuario().getId().equals(solicitanteId)) {
            throw new AccessDeniedBusinessException("No puede acceder a prestamos de otro usuario");
        }
    }
}
