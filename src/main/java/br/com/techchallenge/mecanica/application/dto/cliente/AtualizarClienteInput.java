package br.com.techchallenge.mecanica.application.dto.cliente;

public record AtualizarClienteInput(
        String nome,
        String contato,
        String email) {
}