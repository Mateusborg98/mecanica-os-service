package br.com.techchallenge.mecanica.application.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;

public interface OrdemDeServicoGateway {

    OrdemDeServico salvar(OrdemDeServico ordem);

    Optional<OrdemDeServico> buscarPorId(UUID id);

    List<OrdemDeServico> listar();

}
