package br.com.techchallenge.mecanica.application.usecase.ordemdeservico;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.techchallenge.mecanica.application.dto.ordemdeservico.CriarOrdemDeServicoInput;
import br.com.techchallenge.mecanica.application.gateway.ClienteGateway;
import br.com.techchallenge.mecanica.application.gateway.VeiculoGateway;
import br.com.techchallenge.mecanica.application.gateway.OperadorGateway;
import br.com.techchallenge.mecanica.application.gateway.OrdemDeServicoGateway;
import br.com.techchallenge.mecanica.application.gateway.SolicitarReparoGateway;
import br.com.techchallenge.mecanica.domain.exception.ClienteNaoEncontradoException;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import br.com.techchallenge.mecanica.domain.exception.VeiculoNaoEncontradoException;
import br.com.techchallenge.mecanica.domain.exception.OperadorNaoEncontradoException;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CriarOrdemDeServicoUseCase {
    private final OrdemDeServicoGateway ordemGateway;
    private final ClienteGateway clienteGateway;
    private final VeiculoGateway veiculoGateway;
    private final OperadorGateway operadorGateway;
    private final NotificarAlteracaoStatusOrdemUseCase notificarAlteracaoStatusOrdemUseCase;
    private final SolicitarReparoGateway solicitarReparoGateway;

    @Transactional
    public OrdemDeServico executar(CriarOrdemDeServicoInput input) {
        clienteGateway.buscarPorId(input.clienteId()).orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado"));
        var veiculo = veiculoGateway.buscarPorId(input.veiculoId())
                .orElseThrow(() -> new VeiculoNaoEncontradoException("Veículo não encontrado"));
        if (!input.clienteId().equals(veiculo.getClienteId())) {
            throw new RegraNegocioException("Veículo não pertence ao cliente informado.");
        }
        operadorGateway.buscarPorId(input.operadorId()).orElseThrow(() -> new OperadorNaoEncontradoException("Operador não encontrado"));
        var ordem = ordemGateway.salvar(new OrdemDeServico(input.clienteId(), input.veiculoId(), input.operadorId()));
        solicitarReparoGateway.solicitar(ordem.getId());
        notificarAlteracaoStatusOrdemUseCase.executar(ordem);
        return ordem;
    }
}
