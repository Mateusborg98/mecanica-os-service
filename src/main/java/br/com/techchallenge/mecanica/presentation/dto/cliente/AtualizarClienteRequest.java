package br.com.techchallenge.mecanica.presentation.dto.cliente;

public record AtualizarClienteRequest(
        String nome,
        String contato,
        String email
) {
}
