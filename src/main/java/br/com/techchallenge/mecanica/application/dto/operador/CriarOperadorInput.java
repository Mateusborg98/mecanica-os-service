package br.com.techchallenge.mecanica.application.dto.operador;

public record CriarOperadorInput(
        String nome,
        Integer matricula,
        String email,
        String contato,
        String cargo) {
}