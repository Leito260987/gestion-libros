package com.universidad.biblioteca.controller;

import com.universidad.biblioteca.dto.response.PageResponse;
import com.universidad.biblioteca.dto.response.PrestamoResponse;
import com.universidad.biblioteca.dto.response.UsuarioResponse;
import com.universidad.biblioteca.security.SecurityUtils;
import com.universidad.biblioteca.service.PrestamoService;
import com.universidad.biblioteca.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Usuarios", description = "Perfil propio y administracion de usuarios")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PrestamoService prestamoService;

    public UsuarioController(UsuarioService usuarioService, PrestamoService prestamoService) {
        this.usuarioService = usuarioService;
        this.prestamoService = prestamoService;
    }

    // ---- Perfil propio (cualquier usuario autenticado) ----

    @Operation(summary = "Obtener el perfil del usuario autenticado")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> perfil() {
        return ResponseEntity.ok(usuarioService.obtenerPorId(SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "Historial de prestamos del usuario autenticado")
    @GetMapping("/me/historial")
    public ResponseEntity<PageResponse<PrestamoResponse>> miHistorial(
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(prestamoService.historialUsuario(SecurityUtils.getCurrentUserId(), pageable));
    }

    // ---- Administracion (solo ADMIN, reforzado por SecurityConfig y @PreAuthorize) ----

    @Operation(summary = "Listar todos los usuarios (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<PageResponse<UsuarioResponse>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listar(pageable));
    }

    @Operation(summary = "Obtener un usuario por id (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @Operation(summary = "Historial de prestamos de cualquier usuario (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{id}/historial")
    public ResponseEntity<PageResponse<PrestamoResponse>> historial(
            @PathVariable Long id,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(prestamoService.historialUsuario(id, pageable));
    }

    @Operation(summary = "Activar/desactivar un usuario (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, activo));
    }
}
