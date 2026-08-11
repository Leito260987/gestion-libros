package com.universidad.biblioteca.security;

import com.universidad.biblioteca.config.JwtProperties;
import com.universidad.biblioteca.entity.Rol;
import com.universidad.biblioteca.entity.Usuario;
import com.universidad.biblioteca.entity.enums.RolNombre;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(
                "dGVzdC1zZWNyZXQtY2xhdmUtZGUtcHJ1ZWJhcy1kZS0zMi1ieXRlcy1taW5pbW8tcGFyYS1oczI1Ng==",
                "dGVzdC1yZWZyZXNoLXNlY3JldC1jbGF2ZS1kZS1wcnVlYmFzLTMyLWJ5dGVzLW1pbmltby1oczI1Ng==",
                900000, 604800000, "biblioteca-test");
        jwtService = new JwtService(props);

        Usuario usuario = Usuario.builder()
                .id(7L).email("ana@biblioteca.edu")
                .roles(Set.of(new Rol(RolNombre.ROLE_USER)))
                .build();
        principal = UserPrincipal.from(usuario);
    }

    @Test
    @DisplayName("El access token emitido se valida y contiene subject, uid y roles")
    void accessTokenRoundTrip() {
        String token = jwtService.generateAccessToken(principal);
        Claims claims = jwtService.parseAccessToken(token);

        assertThat(claims.getSubject()).isEqualTo("ana@biblioteca.edu");
        assertThat(claims.get("uid", Number.class).longValue()).isEqualTo(7L);
        @SuppressWarnings("unchecked")
        java.util.List<String> roles = claims.get("roles", java.util.List.class);
        assertThat(roles).contains("ROLE_USER");
    }

    @Test
    @DisplayName("Un refresh token no puede usarse como access token (claim type)")
    void refreshNoSirveComoAccess() {
        String refresh = jwtService.generateRefreshToken(principal);
        assertThatThrownBy(() -> jwtService.parseAccessToken(refresh))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Un token con firma manipulada es rechazado")
    void tokenManipuladoRechazado() {
        String token = jwtService.generateAccessToken(principal);
        String manipulado = token.substring(0, token.length() - 3) + "abc";
        assertThatThrownBy(() -> jwtService.parseAccessToken(manipulado))
                .isInstanceOf(JwtException.class);
    }
}
