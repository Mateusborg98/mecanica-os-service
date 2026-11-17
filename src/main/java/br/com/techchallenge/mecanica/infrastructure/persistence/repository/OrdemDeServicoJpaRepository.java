package br.com.techchallenge.mecanica.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.techchallenge.mecanica.domain.enums.StatusOrdemDeServicoEnum;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.OrdemDeServicoJpaEntity;

public interface OrdemDeServicoJpaRepository extends JpaRepository<OrdemDeServicoJpaEntity, UUID> {

    List<OrdemDeServicoJpaEntity> findByStatus(StatusOrdemDeServicoEnum status);

}
