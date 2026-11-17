package br.com.techchallenge.mecanica.presentation.dto.cliente;

import java.util.UUID;

public record ClienteResponse(
                UUID id,
                String nome,
                String cpfCnpj,
                String contato,
                String email,
                boolean ativo) {
}
