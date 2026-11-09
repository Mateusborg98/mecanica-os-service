package br.com.techchallenge.mecanica.domain.ordemdeservico;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import br.com.techchallenge.mecanica.domain.enums.StatusOrdemDeServicoEnum;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import lombok.Getter;

/** Atendimento e projeção dos fatos dos serviços responsáveis por execução e cobrança. */
@Getter
public class OrdemDeServico {
    private final UUID id;
    private final UUID clienteId;
    private final UUID veiculoId;
    private final UUID operadorId;
    private StatusOrdemDeServicoEnum status;
    private final LocalDateTime dtInicioOs;
    private final LocalDateTime dtFimOs;
    private final BigDecimal valorTotalOs;

    public OrdemDeServico(UUID clienteId, UUID veiculoId, UUID operadorId) {
        this(null, clienteId, veiculoId, operadorId, StatusOrdemDeServicoEnum.RECEBIDA, null, null, BigDecimal.ZERO);
    }

    public OrdemDeServico(UUID id, UUID clienteId, UUID veiculoId, UUID operadorId, StatusOrdemDeServicoEnum status,
                          LocalDateTime inicio, LocalDateTime fim, BigDecimal valor) {
        if (clienteId == null || veiculoId == null || operadorId == null || status == null)
            throw new RegraNegocioException("Cliente, veículo, operador e status são obrigatórios");
        this.id = id; this.clienteId = clienteId; this.veiculoId = veiculoId; this.operadorId = operadorId;
        this.status = status; this.dtInicioOs = inicio; this.dtFimOs = fim; this.valorTotalOs = valor;
    }

    public void entregar() {
        if (status == StatusOrdemDeServicoEnum.ENTREGUE) return;
        if (status != StatusOrdemDeServicoEnum.FINALIZADA)
            throw new RegraNegocioException("Entrega exige reparo finalizado");
        status = StatusOrdemDeServicoEnum.ENTREGUE;
    }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof OrdemDeServico ordem && id != null && id.equals(ordem.id);
    }
    @Override public int hashCode() { return getClass().hashCode(); }
}
