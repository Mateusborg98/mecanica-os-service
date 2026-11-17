package br.com.techchallenge.mecanica.presentation.dto.operador;

public record CriarOperadorRequest(
        String nome,
        Integer matricula,
        String email,
        String contato,
        String cargo
) {}