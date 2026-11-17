package br.com.techchallenge.mecanica.presentation.mapper;

import org.springframework.stereotype.Component;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import br.com.techchallenge.mecanica.presentation.dto.ordemDeServico.OrdemDeServicoResponse;

@Component
public class OrdemDeServicoPresentationMapper {
    public OrdemDeServicoResponse toResponse(OrdemDeServico ordem) {
        return new OrdemDeServicoResponse(
                ordem.getId(),
                ordem.getStatus().name(),
                String.valueOf(ordem.getClienteId()),
                String.valueOf(ordem.getVeiculoId()),
                ordem.getValorTotalOs(),
                ordem.getDtInicioOs());
    }
}