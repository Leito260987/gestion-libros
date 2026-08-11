package com.universidad.biblioteca.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universidad.biblioteca.dto.request.LibroRequest;
import com.universidad.biblioteca.dto.request.LoginRequest;
import com.universidad.biblioteca.dto.request.PrestamoRequest;
import com.universidad.biblioteca.dto.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integracion end-to-end con contexto Spring real y H2 en memoria.
 * Ejercitan seguridad JWT, autorizacion por rol y por propietario, validaciones,
 * manejo de errores y el flujo completo de prestamo/devolucion.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // Admin sembrado por DataInitializer (ver src/test/resources/application.yml)
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = login("admin@biblioteca.edu", "Admin123!");
        // Registro de un usuario normal unico por ejecucion
        String email = "user" + System.nanoTime() + "@biblioteca.edu";
        JsonNode reg = registrar("Leo", "Parati", email, "Passw0rd!");
        userToken = reg.get("accessToken").asText();
    }

    // ------------------- Autenticacion -------------------

    @Test
    @DisplayName("Login con credenciales validas devuelve access y refresh token")
    void login_ok() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@biblioteca.edu", "Admin123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyString())))
                .andExpect(jsonPath("$.refreshToken", not(emptyString())))
                .andExpect(jsonPath("$.roles", hasItem("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Login con password incorrecta devuelve 401")
    void login_credencialesInvalidas() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@biblioteca.edu", "mala"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Registro con email invalido devuelve 400 con detalles de validacion")
    void registro_validacion() throws Exception {
        var body = new RegisterRequest("A", "B", "no-es-email", "123");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details", not(empty())));
    }

    // ------------------- Seguridad y roles -------------------

    @Test
    @DisplayName("Acceder a endpoint protegido sin token devuelve 401")
    void sinToken_401() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un USER no puede crear libros (403)")
    void user_noCreaLibros() throws Exception {
        mockMvc.perform(post("/api/books").header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(libroValido("9780134494166"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un ADMIN crea un libro (201) y luego se consulta (200)")
    void admin_creaLibro() throws Exception {
        Long id = crearLibro(libroValido("9780132350884"));
        mockMvc.perform(get("/api/books/" + id).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Clean Code"));
    }

    @Test
    @DisplayName("Crear libro con ISBN invalido devuelve 400")
    void crearLibro_isbnInvalido() throws Exception {
        // ISBN-10 con digito de control incorrecto
        var invalido = new LibroRequest("0134494163", "T", "A", null, null, 2020, 1);
        mockMvc.perform(post("/api/books").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Obtener un libro inexistente devuelve 404")
    void libro_noEncontrado() throws Exception {
        mockMvc.perform(get("/api/books/999999").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ------------------- Flujo de prestamo -------------------

    @Test
    @DisplayName("Flujo completo: prestamo, devolucion y prevencion de doble devolucion")
    void flujoPrestamoDevolucion() throws Exception {
        Long libroId = crearLibro(libroValido("9781617294945"));

        // USER crea su prestamo (201)
        MvcResult res = mockMvc.perform(post("/api/loans").header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PrestamoRequest(libroId, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andReturn();
        Long prestamoId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        // La disponibilidad bajo a 5
        mockMvc.perform(get("/api/books/" + libroId).header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$.cantidadDisponible").value(5));

        // Devolucion (200)
        mockMvc.perform(post("/api/loans/" + prestamoId + "/devolucion")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTO"));

        // Doble devolucion (409)
        mockMvc.perform(post("/api/loans/" + prestamoId + "/devolucion")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Un USER no puede ver el prestamo de otro usuario (403)")
    void user_noVePrestamoAjeno() throws Exception {
        Long libroId = crearLibro(libroValido("9780596009205"));
        // ADMIN presta a un tercero (otro usuario)
        String otroEmail = "otro" + System.nanoTime() + "@biblioteca.edu";
        JsonNode otro = registrar("Otro", "User", otroEmail, "Passw0rd!");
        Long otroId = obtenerIdDesdeMe(otro.get("accessToken").asText());

        MvcResult res = mockMvc.perform(post("/api/loans").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PrestamoRequest(libroId, otroId))))
                .andExpect(status().isCreated()).andReturn();
        Long prestamoId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        // El primer USER intenta acceder al prestamo ajeno
        mockMvc.perform(get("/api/loans/" + prestamoId).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ------------------- Helpers -------------------

    private String login(String email, String password) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private JsonNode registrar(String nombre, String apellido, String email, String pass) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(nombre, apellido, email, pass))))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString());
    }

    private Long obtenerIdDesdeMe(String token) throws Exception {
        MvcResult me = mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(me.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long crearLibro(LibroRequest req) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/books").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();
    }

    private LibroRequest libroValido(String isbn) {
        String titulo = switch (isbn) {
            case "9780132350884" -> "Clean Code";
            default -> "Libro de Prueba";
        };
        return new LibroRequest(isbn, titulo, "Autor Prueba", "Editorial", "Categoria", 2020, 6);
    }
}
