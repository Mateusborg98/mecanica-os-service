package br.com.techchallenge.mecanica.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.exception.CpfInvalidoException;
import br.com.techchallenge.mecanica.domain.exception.PlacaInvalidaException;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import br.com.techchallenge.mecanica.domain.veiculo.valueObject.Placa;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.ClienteJpaEntity;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.VeiculoJpaEntity;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VeiculoMapper {

    private final ClienteMapper clienteMapper;

    public VeiculoJpaEntity toJpaEntity(Veiculo veiculo, Cliente cliente) {
        ClienteJpaEntity clienteJpaEntity = clienteMapper.toJpaEntity(cliente);

        return VeiculoJpaEntity.builder()
                .id(veiculo.getId())
                .placa(veiculo.getPlaca().getValor())
                .marca(veiculo.getMarca())
                .modelo(veiculo.getModelo())
                .ano(veiculo.getAno())
                .clienteJpaEntity(clienteJpaEntity)
                .ativo(veiculo.isAtivo())
                .dataInativacao(veiculo.getDataInativacao())
                .build();
    }

    public Veiculo toDomain(VeiculoJpaEntity veiculoJpaEntity) throws PlacaInvalidaException, CpfInvalidoException {

        return new Veiculo(
                veiculoJpaEntity.getId(),
                new Placa(veiculoJpaEntity.getPlaca()),
                veiculoJpaEntity.getMarca(),
                veiculoJpaEntity.getModelo(),
                veiculoJpaEntity.getAno(),
                veiculoJpaEntity.getClienteJpaEntity().getId(),
                veiculoJpaEntity.isAtivo(),
                veiculoJpaEntity.getDataInativacao());
    }

}
