package br.com.techchallenge.mecanica.application.usecase.ordemdeservico;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.OrdemDeServicoGateway;
import br.com.techchallenge.mecanica.domain.exception.OrdemDeServicoNaoEncontradaException;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuscarOrdemDeServicoPorIdUseCase {

    private final OrdemDeServicoGateway gateway;

    public OrdemDeServico executar(UUID id) {

        return gateway.buscarPorId(id)
                .orElseThrow(() -> new OrdemDeServicoNaoEncontradaException(
                        "Ordem de serviço não encontrada"));
    }
}