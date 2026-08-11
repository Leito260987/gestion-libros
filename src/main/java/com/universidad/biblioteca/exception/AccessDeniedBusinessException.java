package com.universidad.biblioteca.exception;

/**
 * Se lanza cuando un usuario intenta acceder a un recurso ajeno mediante reglas
 * de negocio (no solo por rol). Mapea a HTTP 403.
 */
public class AccessDeniedBusinessException extends RuntimeException {

    public AccessDeniedBusinessException(String message) {
        super(message);
    }
}
