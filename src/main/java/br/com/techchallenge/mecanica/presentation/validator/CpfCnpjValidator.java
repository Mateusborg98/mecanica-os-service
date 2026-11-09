package br.com.techchallenge.mecanica.presentation.validator;

import br.com.techchallenge.mecanica.presentation.annotation.CpfCnpjAnnotation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfCnpjValidator implements ConstraintValidator<CpfCnpjAnnotation, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // deixe @NotBlank cuidar disso
        }

        String documento = value.replaceAll("\\D", "");

        if (documento.length() == 11) {
            return isCpfValido(documento);
        }

        if (documento.length() == 14) {
            return isCnpjValido(documento);
        }

        return false;
    }

    private boolean isCpfValido(String cpf) {
        if (cpf.matches("(\\d)\\1{10}"))
            return false;

        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (cpf.charAt(i) - '0') * (10 - i);
        }

        int dig1 = 11 - (soma % 11);
        dig1 = (dig1 >= 10) ? 0 : dig1;

        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (cpf.charAt(i) - '0') * (11 - i);
        }

        int dig2 = 11 - (soma % 11);
        dig2 = (dig2 >= 10) ? 0 : dig2;

        return dig1 == (cpf.charAt(9) - '0')
                && dig2 == (cpf.charAt(10) - '0');

    }

    private boolean isCnpjValido(String cnpj) {
        if (cnpj.matches("(\\d)\\1{13}"))
            return false;

        int[] peso1 = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
        int[] peso2 = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += (cnpj.charAt(i) - '0') * peso1[i];
        }

        int dig1 = soma % 11;
        dig1 = (dig1 < 2) ? 0 : 11 - dig1;

        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += (cnpj.charAt(i) - '0') * peso2[i];
        }

        int dig2 = soma % 11;
        dig2 = (dig2 < 2) ? 0 : 11 - dig2;

        return dig1 == (cnpj.charAt(12) - '0')
                && dig2 == (cnpj.charAt(13) - '0');

    }
}
