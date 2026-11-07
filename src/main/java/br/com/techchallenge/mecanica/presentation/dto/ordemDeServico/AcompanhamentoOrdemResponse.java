package br.com.techchallenge.mecanica.presentation.dto.ordemDeServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AcompanhamentoOrdemResponse(
        UUID ordemDeServicoId,
        String status,
        BigDecimal valorTotal,
        LocalDateTime inicioExecucao,
        LocalDateTime fimExecucao) {
}
