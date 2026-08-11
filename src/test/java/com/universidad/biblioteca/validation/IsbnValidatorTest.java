package com.universidad.biblioteca.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class IsbnValidatorTest {

    private final IsbnValidator validator = new IsbnValidator();

    @DisplayName("Acepta ISBN-10 e ISBN-13 validos (con y sin separadores)")
    @ParameterizedTest
    @ValueSource(strings = {
            "0134494164",        // ISBN-10 valido
            "0-201-63361-2",     // ISBN-10 con guiones
            "9780134494166",     // ISBN-13 valido
            "978-0-13-235088-4"  // ISBN-13 con guiones
    })
    void aceptaIsbnValidos(String isbn) {
        assertThat(validator.isValid(isbn, null)).isTrue();
    }

    @DisplayName("Rechaza ISBN con digito de control incorrecto o longitud invalida")
    @ParameterizedTest
    @ValueSource(strings = {
            "0134494163",      // checksum ISBN-10 incorrecto
            "9780134494167",   // checksum ISBN-13 incorrecto
            "123",             // longitud invalida
            "ABCDEFGHIJ"       // no numerico
    })
    void rechazaIsbnInvalidos(String isbn) {
        assertThat(validator.isValid(isbn, null)).isFalse();
    }

    @DisplayName("Valores nulos/vacios se delegan a @NotBlank (se consideran validos aqui)")
    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void permiteVaciosParaDelegar(String isbn) {
        assertThat(validator.isValid(isbn, null)).isTrue();
    }
}
