package com.universidad.biblioteca.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Arranca la aplicacion completa con Tomcat embebido en un puerto real para
 * verificar que el contexto levanta y que la documentacion OpenAPI se genera
 * y es accesible sin autenticacion.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationSmokeTest {

    @Autowired TestRestTemplate restTemplate;

    @Test
    @DisplayName("El documento OpenAPI se genera y expone los endpoints y el esquema Bearer")
    void openApiDocDisponible() {
        ResponseEntity<String> res = restTemplate.getForEntity("/v3/api-docs", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody())
                .contains("/api/auth/login")
                .contains("/api/books")
                .contains("/api/loans")
                .contains("bearerAuth");
    }

    @Test
    @DisplayName("Un endpoint protegido responde 401 sin token (cadena de seguridad activa)")
    void endpointProtegidoSinToken() {
        ResponseEntity<String> res = restTemplate.getForEntity("/api/books", String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
