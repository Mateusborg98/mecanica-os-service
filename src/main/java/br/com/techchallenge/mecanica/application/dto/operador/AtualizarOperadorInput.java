package br.com.techchallenge.mecanica.application.dto.operador;

public record AtualizarOperadorInput(
        String nome,
        String email,
        String contato,
        String cargo) {
}