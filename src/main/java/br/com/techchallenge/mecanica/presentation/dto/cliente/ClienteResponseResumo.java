package br.com.techchallenge.mecanica.presentation.dto.cliente;

import java.util.UUID;

public record ClienteResponseResumo(
        UUID id,
        String nome,
        String contato,
        String email
) {
}
