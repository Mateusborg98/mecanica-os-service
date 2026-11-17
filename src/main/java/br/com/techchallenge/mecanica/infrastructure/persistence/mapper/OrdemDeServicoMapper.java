package br.com.techchallenge.mecanica.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.ClienteJpaEntity;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.OperadorJpaEntity;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.OrdemDeServicoJpaEntity;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.VeiculoJpaEntity;

@Component
public class OrdemDeServicoMapper {
    public OrdemDeServicoJpaEntity toEntity(OrdemDeServico ordem) {
        return OrdemDeServicoJpaEntity.builder().id(ordem.getId())
            .clienteJpaEntity(ClienteJpaEntity.builder().id(ordem.getClienteId()).build())
            .veiculoJpaEntity(VeiculoJpaEntity.builder().id(ordem.getVeiculoId()).build())
            .operadorJpaEntity(OperadorJpaEntity.builder().id(ordem.getOperadorId()).build())
            .status(ordem.getStatus()).dtInicioOs(ordem.getDtInicioOs()).dtFimOs(ordem.getDtFimOs())
            .valorTotalOs(ordem.getValorTotalOs()).build();
    }
    public OrdemDeServico toDomain(OrdemDeServicoJpaEntity entity) {
        return new OrdemDeServico(entity.getId(), entity.getClienteJpaEntity().getId(), entity.getVeiculoJpaEntity().getId(),
            entity.getOperadorJpaEntity().getId(), entity.getStatus(), entity.getDtInicioOs(), entity.getDtFimOs(), entity.getValorTotalOs());
    }
}
