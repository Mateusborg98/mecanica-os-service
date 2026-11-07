package br.com.techchallenge.mecanica.presentation.dto.operador;

public record AtualizarOperadorRequest(
        String nome,
        String email,
        String contato,
        String cargo) {
}