package br.com.techchallenge.mecanica.domain.exception;

public class OrdemDeServicoNaoEncontradaException extends RuntimeException {

    public OrdemDeServicoNaoEncontradaException(String message) {
        super(message);
    }
}
