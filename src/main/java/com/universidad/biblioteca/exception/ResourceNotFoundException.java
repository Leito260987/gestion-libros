package com.universidad.biblioteca.exception;

/** Se lanza cuando un recurso solicitado no existe. Mapea a HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String recurso, Object id) {
        return new ResourceNotFoundException(recurso + " no encontrado con id: " + id);
    }
}
