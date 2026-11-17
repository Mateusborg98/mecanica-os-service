package br.com.techchallenge.mecanica.presentation.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import br.com.techchallenge.mecanica.presentation.dto.cliente.ClienteResponseResumo;
import br.com.techchallenge.mecanica.presentation.dto.ordemDeServico.OrdemDeServicoResponse;
import br.com.techchallenge.mecanica.presentation.dto.ordemDeServico.OrdemDeServicoResponseResumo;

@Component
public class OrdemDeServicoPresentationMapper {

    VeiculoPresentationMapper veiculoPresentationMapper = new VeiculoPresentationMapper();

    public OrdemDeServicoResponse toResponse(OrdemDeServico ordemDeServico) {

        return new OrdemDeServicoResponse(
                ordemDeServico.getId(),
                ordemDeServico.getStatus().name(),
                String.valueOf(ordemDeServico.getClienteId()),
                String.valueOf(ordemDeServico.getVeiculoId()),
                ordemDeServico.getValorTotalOs(),
                ordemDeServico.getDtInicioOs());
    }

    public OrdemDeServicoResponseResumo toResumoResponse(OrdemDeServico ordemDeServico) {

        return new OrdemDeServicoResponseResumo(
                ordemDeServico.getId(),
                ordemDeServico.getStatus(),
                ordemDeServico.getDtInicioOs(),
                ordemDeServico.getClienteId(),
                ordemDeServico.getVeiculoId());
    }

    public List<ClienteResponseResumo> toResponse(List<Cliente> clientes) {

        List<ClienteResponseResumo> clienteResponses = new ArrayList<>();

        for (Cliente clienteList : clientes) {
            clienteResponses.add(new ClienteResponseResumo(
                    clienteList.getId(),
                    clienteList.getNome(),
                    clienteList.getContato(),
                    clienteList.getEmail()));
        }

        return clienteResponses;
    }

}
