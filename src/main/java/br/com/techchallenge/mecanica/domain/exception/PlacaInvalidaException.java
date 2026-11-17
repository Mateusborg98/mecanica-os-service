package br.com.techchallenge.mecanica.domain.exception;

public class PlacaInvalidaException extends RuntimeException {

    public PlacaInvalidaException(String placaNaoPodeSerNula) {
        super(placaNaoPodeSerNula);
    }
}
