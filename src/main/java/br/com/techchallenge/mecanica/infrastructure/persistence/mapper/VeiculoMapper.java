package br.com.techchallenge.mecanica.infrastructure.persistence.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.exception.CpfInvalidoException;
import br.com.techchallenge.mecanica.domain.exception.PlacaInvalidaException;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import br.com.techchallenge.mecanica.domain.veiculo.valueObject.Placa;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.ClienteJpaEntity;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.VeiculoJpaEntity;
import br.com.techchallenge.mecanica.presentation.dto.cliente.ClienteResponseResumo;
import br.com.techchallenge.mecanica.presentation.dto.veiculo.VeiculoResponse;
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

    public List<VeiculoJpaEntity> toListJpaEntity(List<Veiculo> veiculos, Cliente cliente) {

        List<VeiculoJpaEntity> veiculoJpaEntities = new ArrayList<>();

        for (Veiculo veiculo : veiculos) {
            ClienteJpaEntity clienteJpaEntity = clienteMapper.toJpaEntity(cliente);

            veiculoJpaEntities.add(VeiculoJpaEntity.builder()
                    .id(veiculo.getId())
                    .placa(veiculo.getPlaca().getValor())
                    .marca(veiculo.getMarca())
                    .modelo(veiculo.getModelo())
                    .ano(veiculo.getAno())
                    .clienteJpaEntity(clienteJpaEntity)
                    .ativo(veiculo.isAtivo())
                    .dataInativacao(veiculo.getDataInativacao())
                    .build());

        }
        return veiculoJpaEntities;
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

    public List<Veiculo> toListVeiculo(List<VeiculoJpaEntity> veiculosJpaEntities) {

        List<Veiculo> veiculosList = new ArrayList<>();

        for (VeiculoJpaEntity veiculo : veiculosJpaEntities) {
            Cliente cliente = clienteMapper.toDomain(veiculo.getClienteJpaEntity());

            veiculosList.add(new Veiculo(
                    veiculo.getId(),
                    new Placa(veiculo.getPlaca()),
                    veiculo.getMarca(),
                    veiculo.getModelo(),
                    veiculo.getAno(),
                    cliente.getId(),
                    veiculo.isAtivo(),
                    veiculo.getDataInativacao()));
        }
        return veiculosList;
    }

    public VeiculoResponse toResponse(Veiculo veiculo, Cliente cliente) {

        ClienteResponseResumo clienteResponseResumo = new ClienteResponseResumo(
                cliente.getId(),
                cliente.getNome(),
                cliente.getContato(),
                cliente.getEmail());

        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getPlaca().getValor(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.isAtivo(),
                clienteResponseResumo);
    }
}
