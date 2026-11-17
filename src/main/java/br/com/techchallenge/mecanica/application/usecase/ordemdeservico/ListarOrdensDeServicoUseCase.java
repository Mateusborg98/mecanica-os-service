package br.com.techchallenge.mecanica.application.usecase.ordemdeservico;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.OrdemDeServicoGateway;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListarOrdensDeServicoUseCase {

    private final OrdemDeServicoGateway gateway;

    public List<OrdemDeServico> executar() {
        return gateway.listar();
    }
}