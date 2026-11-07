package br.com.techchallenge.mecanica.application.usecase.ordemdeservico;

import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import br.com.techchallenge.mecanica.application.gateway.PagamentoOrdemGateway;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.OrdemDeServicoGateway;
import br.com.techchallenge.mecanica.domain.exception.OrdemDeServicoNaoEncontradaException;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EntregarOrdemDeServicoUseCase {

    private final OrdemDeServicoGateway ordemGateway;
    private final NotificarAlteracaoStatusOrdemUseCase notificarAlteracaoStatusOrdemUseCase;

    private final PagamentoOrdemGateway pagamentoGateway;

    @Transactional
    public OrdemDeServico executar(UUID ordemId) {
        pagamentoGateway.validarParaEntrega(ordemId);

        OrdemDeServico ordem = ordemGateway.buscarPorId(ordemId)
                .orElseThrow(() -> new OrdemDeServicoNaoEncontradaException(
                        "Ordem de serviço não encontrada"));

        ordem.entregar();

        OrdemDeServico ordemAtualizada = ordemGateway.salvar(ordem);
        pagamentoGateway.registrarEntrega(ordemId);

        notificarAlteracaoStatusOrdemUseCase.executar(ordemAtualizada);

        return ordemAtualizada;
    }
}