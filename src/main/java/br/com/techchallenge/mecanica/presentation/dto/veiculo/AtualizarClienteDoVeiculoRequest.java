package br.com.techchallenge.mecanica.presentation.dto.veiculo;

import java.util.UUID;

public record AtualizarClienteDoVeiculoRequest(
        UUID clienteId,
        UUID veiculoId
) {
}
