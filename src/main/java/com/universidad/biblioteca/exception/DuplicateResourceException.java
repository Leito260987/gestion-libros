package com.universidad.biblioteca.exception;

/** Se lanza al violar una restriccion de unicidad (email/ISBN). Mapea a HTTP 409. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
