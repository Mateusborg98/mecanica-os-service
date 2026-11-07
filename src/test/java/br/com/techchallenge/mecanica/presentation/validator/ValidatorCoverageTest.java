package br.com.techchallenge.mecanica.presentation.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidatorCoverageTest {

    @Test
    void cpfCnpjValidatorDeveAceitarNuloCpfECnpjValidos() {
        var validator = new CpfCnpjValidator();

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("529.982.247-25", null));
        assertTrue(validator.isValid("04.252.011/0001-10", null));
    }

    @Test
    void cpfCnpjValidatorDeveRejeitarTamanhoRepeticaoEDigitosInvalidos() {
        var validator = new CpfCnpjValidator();

        assertFalse(validator.isValid("123", null));
        assertFalse(validator.isValid("111.111.111-11", null));
        assertFalse(validator.isValid("529.982.247-24", null));
        assertFalse(validator.isValid("11.111.111/1111-11", null));
        assertFalse(validator.isValid("04.252.011/0001-11", null));
    }

    @Test
    void placaValidatorDeveAceitarAusenciaEFormatosValidos() {
        var validator = new PlacaValidator();

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid(" ", null));
        assertTrue(validator.isValid("abc-1234", null));
        assertTrue(validator.isValid("abc1d23", null));
    }

    @Test
    void placaValidatorDeveRejeitarFormatoInvalido() {
        assertFalse(new PlacaValidator().isValid("AB-123", null));
    }
}
