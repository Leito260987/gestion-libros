package com.universidad.biblioteca.service.impl;

import com.universidad.biblioteca.dto.request.LibroRequest;
import com.universidad.biblioteca.dto.response.LibroResponse;
import com.universidad.biblioteca.dto.response.PageResponse;
import com.universidad.biblioteca.entity.Libro;
import com.universidad.biblioteca.entity.enums.EstadoPrestamo;
import com.universidad.biblioteca.exception.BusinessRuleException;
import com.universidad.biblioteca.exception.DuplicateResourceException;
import com.universidad.biblioteca.exception.ResourceNotFoundException;
import com.universidad.biblioteca.mapper.LibroMapper;
import com.universidad.biblioteca.repository.LibroRepository;
import com.universidad.biblioteca.repository.PrestamoRepository;
import com.universidad.biblioteca.service.LibroService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class LibroServiceImpl implements LibroService {

    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final LibroMapper libroMapper;

    public LibroServiceImpl(LibroRepository libroRepository,
                            PrestamoRepository prestamoRepository,
                            LibroMapper libroMapper) {
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.libroMapper = libroMapper;
    }

    @Override
    @Transactional
    public LibroResponse crear(LibroRequest request) {
        String isbn = normalizar(request.isbn());
        if (libroRepository.existsByIsbn(isbn)) {
            throw new DuplicateResourceException("Ya existe un libro con el ISBN: " + isbn);
        }
        Libro libro = libroMapper.toEntity(request);
        return libroMapper.toResponse(libroRepository.save(libro));
    }

    @Override
    @Transactional
    public LibroResponse actualizar(Long id, LibroRequest request) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Libro", id));

        String isbn = normalizar(request.isbn());
        if (libroRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw new DuplicateResourceException("Ya existe otro libro con el ISBN: " + isbn);
        }
        // No permitir reducir el total por debajo de las unidades ya prestadas.
        int prestados = libro.getCantidadTotal() - libro.getCantidadDisponible();
        if (request.cantidadTotal() < prestados) {
            throw new BusinessRuleException(
                    "La cantidad total (" + request.cantidadTotal() + ") no puede ser menor a las unidades prestadas ("
                            + prestados + ")");
        }
        libroMapper.updateEntity(libro, request);
        return libroMapper.toResponse(libroRepository.save(libro));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Libro", id));
        if (prestamoRepository.existsByLibroIdAndEstado(id, EstadoPrestamo.ACTIVO)) {
            throw new BusinessRuleException("No se puede eliminar un libro con prestamos activos");
        }
        libroRepository.delete(libro);
    }

    @Override
    @Transactional(readOnly = true)
    public LibroResponse obtenerPorId(Long id) {
        return libroRepository.findById(id)
                .map(libroMapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.of("Libro", id));
    }

    @Override
    @Transactional(readOnly = true)
    public LibroResponse obtenerPorIsbn(String isbn) {
        return libroRepository.findByIsbn(normalizar(isbn))
                .map(libroMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ISBN: " + isbn));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LibroResponse> buscarCatalogo(String titulo, String autor, String categoria,
                                                      Boolean soloDisponibles, Pageable pageable) {
        var page = libroRepository.buscarCatalogo(
                        blankToNull(titulo), blankToNull(autor), blankToNull(categoria),
                        Boolean.TRUE.equals(soloDisponibles), pageable)
                .map(libroMapper::toResponse);
        return PageResponse.from(page);
    }

    private String normalizar(String isbn) {
        return isbn == null ? null : isbn.replaceAll("[\\s-]", "");
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
