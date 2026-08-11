package com.universidad.biblioteca.security;

/**
 * Principal ligero almacenado en el SecurityContext tras validar el JWT.
 * Expone el id para permitir reglas de autorizacion por propietario en SpEL,
 * p. ej. @PreAuthorize("hasRole('ADMIN') or #id == principal.id").
 */
public record AuthenticatedUser(Long id, String email) {

    // Getter explicito para acceso via SpEL (principal.id / principal.email).
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
