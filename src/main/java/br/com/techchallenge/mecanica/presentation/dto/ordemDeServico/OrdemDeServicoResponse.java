package br.com.techchallenge.mecanica.presentation.dto.ordemDeServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrdemDeServicoResponse(
        UUID id,
        String status,
        String cliente,
        String veiculo,
        BigDecimal valorTotal,
        LocalDateTime dtInicioOs
) {
}