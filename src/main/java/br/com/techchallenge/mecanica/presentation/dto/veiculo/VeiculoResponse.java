package br.com.techchallenge.mecanica.presentation.dto.veiculo;

import java.util.UUID;

import br.com.techchallenge.mecanica.presentation.dto.cliente.ClienteResponseResumo;

public record VeiculoResponse(
        UUID id,
        String placa,
        String marca,
        String modelo,
        Integer ano,
        boolean ativo,
        ClienteResponseResumo clienteResponseResumo
) {
}
