package com.universidad.biblioteca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de JWT enlazadas desde app.jwt.* (con soporte de variables de
 * entorno). Centraliza secretos y expiraciones fuera del codigo.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        String refreshSecret,
        long expirationMs,
        long refreshExpirationMs,
        String issuer
) {
}
