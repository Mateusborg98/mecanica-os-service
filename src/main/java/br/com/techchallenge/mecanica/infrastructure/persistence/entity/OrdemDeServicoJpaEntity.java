package br.com.techchallenge.mecanica.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.techchallenge.mecanica.domain.enums.StatusOrdemDeServicoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.CreationTimestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemDeServicoJpaEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private ClienteJpaEntity clienteJpaEntity;

    @ManyToOne
    private VeiculoJpaEntity veiculoJpaEntity;

    @ManyToOne
    private OperadorJpaEntity operadorJpaEntity;

    @Enumerated(EnumType.STRING)
    private StatusOrdemDeServicoEnum status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dtCriacao;

    private LocalDateTime dtInicioOs;
    private LocalDateTime dtFimOs;

    private BigDecimal valorTotalOs;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OrdemDeServicoJpaEntity other))
            return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
