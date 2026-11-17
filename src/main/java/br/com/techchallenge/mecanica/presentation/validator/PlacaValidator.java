package br.com.techchallenge.mecanica.presentation.validator;

import br.com.techchallenge.mecanica.presentation.annotation.PlacaValida;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PlacaValidator implements ConstraintValidator<PlacaValida, String> {

    private static final String PLACA_ANTIGA = "^[A-Z]{3}[0-9]{4}$";
    private static final String PLACA_MERCOSUL = "^[A-Z]{3}[0-9][A-Z][0-9]{2}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // @NotBlank cuida disso
        }

        String placa = value.toUpperCase().replaceAll("[^A-Z0-9]", "");

        return placa.matches(PLACA_ANTIGA) || placa.matches(PLACA_MERCOSUL);
    }
}