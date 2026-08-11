package com.universidad.biblioteca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Propiedades de negocio enlazadas desde app.* (admin seed y politica de prestamos). */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Admin admin,
        Prestamo prestamo
) {
    public record Admin(String email, String password) {
    }

    public record Prestamo(int diasVencimiento) {
    }
}
