package br.com.techchallenge.mecanica.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.techchallenge.mecanica.infrastructure.persistence.entity.OperadorJpaEntity;

public interface OperadorJpaRepository extends JpaRepository<OperadorJpaEntity, UUID> {

    Optional<OperadorJpaEntity> findByMatriculaAndAtivoTrue(Integer matricula);
    Optional<OperadorJpaEntity> findByIdAndAtivoTrue(UUID id);
    List<OperadorJpaEntity> findByAtivoTrue();
}
