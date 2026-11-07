package br.com.techchallenge.mecanica.application.dto.cliente;

public record CriarClienteInput(
        String nome,
        String cpfCnpj,
        String contato,
        String email) {
}
