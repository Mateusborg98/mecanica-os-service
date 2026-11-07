package br.com.techchallenge.mecanica.domain.exception;

public class CpfInvalidoException extends RuntimeException {

    public CpfInvalidoException(String cpfNaoPodeSerNulo) {
        super(cpfNaoPodeSerNulo);
    }
}
