package br.com.techchallenge.mecanica.presentation.dto.operador;

import java.time.LocalDateTime;
import java.util.UUID;

public record OperadorResponse(
        UUID id,
        String nome,
        Integer matricula,
        String email,
        String contato,
        String cargo,
        boolean ativo,
        LocalDateTime dataInativacao) {
}