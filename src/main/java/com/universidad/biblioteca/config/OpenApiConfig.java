package com.universidad.biblioteca.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de OpenAPI. Declara el esquema de seguridad Bearer JWT para
 * que Swagger UI permita autorizar con el access token.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Biblioteca Universitaria",
                version = "1.0.0",
                description = "API RESTful para la gestion de libros, usuarios y prestamos de una biblioteca universitaria.",
                contact = @Contact(name = "Equipo de Desarrollo", email = "biblioteca@universidad.edu")
        ),
        servers = @Server(url = "/", description = "Servidor por defecto")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Introduce el access token JWT obtenido en /api/auth/login"
)
public class OpenApiConfig {
}
