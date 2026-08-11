package com.universidad.biblioteca.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Verifica el digito de control de ISBN-10 e ISBN-13. Se permiten guiones y
 * espacios como separadores, que se ignoran antes de validar.
 */
public class IsbnValidator implements ConstraintValidator<ValidIsbn, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // la obligatoriedad se controla con @NotBlank aparte
        }
        String normalizado = value.replaceAll("[\\s-]", "");
        return switch (normalizado.length()) {
            case 10 -> esIsbn10Valido(normalizado);
            case 13 -> esIsbn13Valido(normalizado);
            default -> false;
        };
    }

    private boolean esIsbn10Valido(String isbn) {
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            char c = isbn.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            suma += (c - '0') * (10 - i);
        }
        char ultimo = isbn.charAt(9);
        int valorControl = (ultimo == 'X' || ultimo == 'x') ? 10 : (Character.isDigit(ultimo) ? ultimo - '0' : -1);
        if (valorControl < 0) {
            return false;
        }
        suma += valorControl;
        return suma % 11 == 0;
    }

    private boolean esIsbn13Valido(String isbn) {
        int suma = 0;
        for (int i = 0; i < 13; i++) {
            char c = isbn.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            int digito = c - '0';
            suma += (i % 2 == 0) ? digito : digito * 3;
        }
        return suma % 10 == 0;
    }
}
