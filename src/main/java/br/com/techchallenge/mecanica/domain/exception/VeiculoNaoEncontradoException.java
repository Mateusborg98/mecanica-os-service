package br.com.techchallenge.mecanica.domain.exception;

public class VeiculoNaoEncontradoException extends RuntimeException {

    public VeiculoNaoEncontradoException(String message) {
        super(message);
    }
}
