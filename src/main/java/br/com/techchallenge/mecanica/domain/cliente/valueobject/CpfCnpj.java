package br.com.techchallenge.mecanica.domain.cliente.valueobject;

import br.com.techchallenge.mecanica.domain.exception.CpfInvalidoException;

import java.util.Objects;

public class CpfCnpj {

    private static final int CPF_LENGTH = 11;
    private static final int CNPJ_LENGTH = 14;

    private static final int[] CNPJ_FIRST_WEIGHTS = {
            5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2
    };

    private static final int[] CNPJ_SECOND_WEIGHTS = {
            6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2
    };

    private final String valor;

    public CpfCnpj(String valor) throws CpfInvalidoException {
        String documentoNormalizado = normalizar(valor);

        validar(documentoNormalizado);

        this.valor = documentoNormalizado;
    }

    private String normalizar(String cpfCnpj) throws CpfInvalidoException {

        if (cpfCnpj == null) {
            throw new CpfInvalidoException("CPF/CNPJ não pode ser nulo");
        }

        return cpfCnpj.replaceAll("\\D", "");
    }

    private void validar(String documento) throws CpfInvalidoException {

        if (documento.length() != CPF_LENGTH
                && documento.length() != CNPJ_LENGTH) {
            throw new CpfInvalidoException("CPF/CNPJ inválido");
        }

        if (documento.matches("(\\d)\\1+")) {
            throw new CpfInvalidoException("CPF/CNPJ inválido");
        }

        boolean valido = documento.length() == CPF_LENGTH
                ? validarCpf(documento)
                : validarCnpj(documento);

        if (!valido) {
            throw new CpfInvalidoException("CPF/CNPJ inválido");
        }
    }

    private boolean validarCpf(String cpf) {

        int soma = 0;

        for (int i = 0; i < 9; i++) {
            soma += valorNumerico(cpf, i) * (10 - i);
        }

        int digito1 = 11 - (soma % 11);

        if (digito1 >= 10) digito1 = 0;

        if (digito1 != valorNumerico(cpf, 9)) {
            return false;
        }

        soma = 0;

        for (int i = 0; i < 10; i++) {
            soma += valorNumerico(cpf, i) * (11 - i);
        }

        int digito2 = 11 - (soma % 11);

        if (digito2 >= 10) digito2 = 0;

        return digito2 == valorNumerico(cpf, 10);
    }

    private boolean validarCnpj(String cnpj) {
        return calcularDigitoCnpj(cnpj, CNPJ_FIRST_WEIGHTS)
                == valorNumerico(cnpj, 12)
                && calcularDigitoCnpj(cnpj, CNPJ_SECOND_WEIGHTS)
                == valorNumerico(cnpj, 13);
    }

    private int calcularDigitoCnpj(String cnpj, int[] pesos) {
        int soma = 0;

        for (int i = 0; i < pesos.length; i++) {
            soma += valorNumerico(cnpj, i) * pesos[i];
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private int valorNumerico(String documento, int indice) {
        return documento.charAt(indice) - '0';
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof CpfCnpj cpfCnpj)) return false;

        return Objects.equals(valor, cpfCnpj.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
