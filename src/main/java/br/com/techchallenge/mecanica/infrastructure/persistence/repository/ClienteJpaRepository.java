package br.com.techchallenge.mecanica.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.techchallenge.mecanica.infrastructure.persistence.entity.ClienteJpaEntity;

public interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, UUID> {

    Optional<ClienteJpaEntity> findByCpfCnpjAndAtivoTrue(String cpfCnpj);

    List<ClienteJpaEntity> findByAtivoTrue();

    Optional<ClienteJpaEntity> findByIdAndAtivoTrue(UUID clienteId);

}
