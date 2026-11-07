package br.com.techchallenge.mecanica.infrastructure.persistence.gateway;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.techchallenge.mecanica.application.gateway.OrdemDeServicoGateway;
import br.com.techchallenge.mecanica.domain.enums.StatusOrdemDeServicoEnum;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.OrdemDeServicoJpaEntity;
import br.com.techchallenge.mecanica.infrastructure.persistence.mapper.OrdemDeServicoMapper;
import br.com.techchallenge.mecanica.infrastructure.persistence.repository.OrdemDeServicoJpaRepository;
import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class OrdemDeServicoGatewayImpl implements OrdemDeServicoGateway {

    private static final Map<StatusOrdemDeServicoEnum, Integer> PRIORIDADE_STATUS = Map.of(
            StatusOrdemDeServicoEnum.EM_EXECUCAO, 1,
            StatusOrdemDeServicoEnum.NA_FILA, 2,
            StatusOrdemDeServicoEnum.AGUARDANDO_APROVACAO, 3,
            StatusOrdemDeServicoEnum.EM_DIAGNOSTICO, 4,
            StatusOrdemDeServicoEnum.RECEBIDA, 5);

    private static final Comparator<OrdemDeServicoJpaEntity> ORDENACAO_LISTAGEM = Comparator
            .comparingInt((OrdemDeServicoJpaEntity ordem) -> PRIORIDADE_STATUS.get(ordem.getStatus()))
            .thenComparing(
                    OrdemDeServicoJpaEntity::getDtCriacao,
                    Comparator.nullsLast(Comparator.naturalOrder()));

    private final OrdemDeServicoJpaRepository ordemRepository;
    private final OrdemDeServicoMapper ordemDeServicoMapper;

    @Override
    public OrdemDeServico salvar(OrdemDeServico ordemDeServico) {

        OrdemDeServicoJpaEntity entity = ordemDeServicoMapper.toEntity(ordemDeServico);

        OrdemDeServicoJpaEntity salvo = ordemRepository.save(entity);

        return ordemDeServicoMapper.toDomain(salvo);
    }

    @Override
    public Optional<OrdemDeServico> buscarPorId(UUID id) {
        return ordemRepository.findById(id).map(ordemDeServicoMapper::toDomain);
    }

    @Override
    public List<OrdemDeServico> listar() {
        return ordemRepository.findAll()
                .stream()
                .filter(ordem -> PRIORIDADE_STATUS.containsKey(ordem.getStatus()))
                .sorted(ORDENACAO_LISTAGEM)
                .map(ordemDeServicoMapper::toDomain)
                .toList();
    }

}
