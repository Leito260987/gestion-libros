package com.universidad.biblioteca.service;

import com.universidad.biblioteca.dto.request.LoginRequest;
import com.universidad.biblioteca.dto.request.RefreshTokenRequest;
import com.universidad.biblioteca.dto.request.RegisterRequest;
import com.universidad.biblioteca.dto.response.AuthResponse;

/** Casos de uso de autenticacion. */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);
}
