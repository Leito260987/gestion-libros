package com.universidad.biblioteca.service.impl;

import com.universidad.biblioteca.dto.request.LoginRequest;
import com.universidad.biblioteca.dto.request.RefreshTokenRequest;
import com.universidad.biblioteca.dto.request.RegisterRequest;
import com.universidad.biblioteca.dto.response.AuthResponse;
import com.universidad.biblioteca.entity.Rol;
import com.universidad.biblioteca.entity.Usuario;
import com.universidad.biblioteca.entity.enums.EstadoUsuario;
import com.universidad.biblioteca.entity.enums.RolNombre;
import com.universidad.biblioteca.exception.DuplicateResourceException;
import com.universidad.biblioteca.exception.ResourceNotFoundException;
import com.universidad.biblioteca.repository.RolRepository;
import com.universidad.biblioteca.repository.UsuarioRepository;
import com.universidad.biblioteca.security.JwtService;
import com.universidad.biblioteca.security.UserPrincipal;
import com.universidad.biblioteca.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email: " + request.email());
        }

        Rol rolUser = rolRepository.findByNombre(RolNombre.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Rol ROLE_USER no configurado en el sistema"));

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // hash BCrypt
                .estado(EstadoUsuario.ACTIVO)
                .roles(Set.of(rolUser))
                .build();

        usuarioRepository.save(usuario);
        return buildTokens(UserPrincipal.from(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Delega la verificacion de credenciales al AuthenticationManager
        // (usa CustomUserDetailsService + BCrypt). Lanza BadCredentialsException
        // que el GlobalExceptionHandler traduce a 401.
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return buildTokens(principal);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        final Claims claims;
        try {
            claims = jwtService.parseRefreshToken(request.refreshToken());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Refresh token invalido o expirado");
        }

        Usuario usuario = usuarioRepository.findByEmail(claims.getSubject())
                .orElseThrow(() -> new BadCredentialsException("Refresh token invalido o expirado"));

        if (!usuario.isActivo()) {
            throw new BadCredentialsException("El usuario esta inactivo");
        }
        return buildTokens(UserPrincipal.from(usuario));
    }

    private AuthResponse buildTokens(UserPrincipal principal) {
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);
        Set<String> roles = principal.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessExpirationSeconds())
                .email(principal.getUsername())
                .roles(roles)
                .build();
    }
}
