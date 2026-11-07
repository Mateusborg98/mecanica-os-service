package br.com.techchallenge.mecanica.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
public class ClienteJpaEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String cpfCnpj;

    @Column(nullable = false)
    private String contato;

    private String email;

    private boolean ativo;

    private LocalDateTime dataInativacao;

    @Builder.Default
    @OneToMany(mappedBy = "clienteJpaEntity")
    private List<VeiculoJpaEntity> veiculoJpaEntities = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "clienteJpaEntity")
    private List<OrdemDeServicoJpaEntity> ordemDeServicoJpaEntities = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ClienteJpaEntity other))
            return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
