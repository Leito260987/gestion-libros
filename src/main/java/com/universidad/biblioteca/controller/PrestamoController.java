package com.universidad.biblioteca.controller;

import com.universidad.biblioteca.dto.request.PrestamoRequest;
import com.universidad.biblioteca.dto.response.EstadisticasResponse;
import com.universidad.biblioteca.dto.response.PageResponse;
import com.universidad.biblioteca.dto.response.PrestamoResponse;
import com.universidad.biblioteca.entity.enums.EstadoPrestamo;
import com.universidad.biblioteca.security.SecurityUtils;
import com.universidad.biblioteca.service.PrestamoService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;

@Tag(name = "Prestamos", description = "Registro de prestamos, devoluciones y consultas")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/loans")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @Operation(summary = "Registrar un prestamo. Un USER se lo asigna a si mismo; " +
            "un ADMIN puede indicar el usuarioId.")
    @PostMapping
    public ResponseEntity<PrestamoResponse> crear(@Valid @RequestBody PrestamoRequest request) {
        PrestamoResponse response = prestamoService.crear(
                request, SecurityUtils.getCurrentUserId(), SecurityUtils.isAdmin());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Registrar la devolucion de un prestamo (propietario o ADMIN)")
    @PostMapping("/{id}/devolucion")
    public ResponseEntity<PrestamoResponse> devolver(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.devolver(
                id, SecurityUtils.getCurrentUserId(), SecurityUtils.isAdmin()));
    }

    @Operation(summary = "Obtener un prestamo por id (propietario o ADMIN)")
    @GetMapping("/{id}")
    public ResponseEntity<PrestamoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.obtener(
                id, SecurityUtils.getCurrentUserId(), SecurityUtils.isAdmin()));
    }

    @Operation(summary = "Listar prestamos por estado (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<PrestamoResponse>> listarPorEstado(
            @RequestParam(defaultValue = "ACTIVO") EstadoPrestamo estado,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(prestamoService.listarPorEstado(estado, pageable));
    }

    @Operation(summary = "Listar prestamos vencidos y marcarlos como VENCIDO (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/vencidos")
    public ResponseEntity<List<PrestamoResponse>> vencidos() {
        return ResponseEntity.ok(prestamoService.listarVencidos());
    }

    @Operation(summary = "Estadisticas basicas de prestamos (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasResponse> estadisticas() {
        return ResponseEntity.ok(prestamoService.estadisticas());
    }
}
