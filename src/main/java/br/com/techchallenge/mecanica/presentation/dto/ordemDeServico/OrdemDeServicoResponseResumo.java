package br.com.techchallenge.mecanica.presentation.dto.ordemDeServico;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.techchallenge.mecanica.domain.enums.StatusOrdemDeServicoEnum;

public record OrdemDeServicoResponseResumo(
        UUID id,
        StatusOrdemDeServicoEnum status,
        LocalDateTime dtInicioOs,
        UUID clienteId,
        UUID veiculoId) {
}
