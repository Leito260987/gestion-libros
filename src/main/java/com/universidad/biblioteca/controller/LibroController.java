package com.universidad.biblioteca.controller;

import com.universidad.biblioteca.dto.request.LibroRequest;
import com.universidad.biblioteca.dto.response.LibroResponse;
import com.universidad.biblioteca.dto.response.PageResponse;
import com.universidad.biblioteca.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Libros", description = "Gestion del catalogo de libros")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/books")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @Operation(summary = "Buscar el catalogo con filtros, paginacion y ordenamiento")
    @GetMapping
    public ResponseEntity<PageResponse<LibroResponse>> buscar(
            @Parameter(description = "Filtro por titulo (contiene)") @RequestParam(required = false) String titulo,
            @Parameter(description = "Filtro por autor (contiene)") @RequestParam(required = false) String autor,
            @Parameter(description = "Filtro por categoria (contiene)") @RequestParam(required = false) String categoria,
            @Parameter(description = "Solo libros con ejemplares disponibles") @RequestParam(required = false) Boolean disponibles,
            @ParameterObject @PageableDefault(size = 10, sort = "titulo") Pageable pageable) {
        return ResponseEntity.ok(libroService.buscarCatalogo(titulo, autor, categoria, disponibles, pageable));
    }

    @Operation(summary = "Obtener un libro por id")
    @GetMapping("/{id}")
    public ResponseEntity<LibroResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(libroService.obtenerPorId(id));
    }

    @Operation(summary = "Obtener un libro por ISBN")
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<LibroResponse> obtenerPorIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(libroService.obtenerPorIsbn(isbn));
    }

    @Operation(summary = "Crear un libro (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<LibroResponse> crear(@Valid @RequestBody LibroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libroService.crear(request));
    }

    @Operation(summary = "Actualizar un libro (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<LibroResponse> actualizar(@PathVariable Long id, @Valid @RequestBody LibroRequest request) {
        return ResponseEntity.ok(libroService.actualizar(id, request));
    }

    @Operation(summary = "Eliminar un libro (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
