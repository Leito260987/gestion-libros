package com.universidad.biblioteca.exception;

/**
 * Se lanza cuando una operacion viola una regla de negocio (p. ej. prestar un
 * libro sin disponibilidad o devolver un prestamo ya devuelto). Mapea a HTTP 409.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
