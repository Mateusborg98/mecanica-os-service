package br.com.techchallenge.mecanica.infrastructure.persistence.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.techchallenge.mecanica.application.gateway.ClienteGateway;
import br.com.techchallenge.mecanica.application.gateway.VeiculoGateway;
import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.exception.CpfInvalidoException;
import br.com.techchallenge.mecanica.domain.exception.PlacaInvalidaException;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.VeiculoJpaEntity;
import br.com.techchallenge.mecanica.infrastructure.persistence.mapper.VeiculoMapper;
import br.com.techchallenge.mecanica.infrastructure.persistence.repository.VeiculoJpaRepository;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class VeiculoGatewayImpl implements VeiculoGateway {

    private final VeiculoJpaRepository repository;
    private final VeiculoMapper mapper;
    private final ClienteGateway clienteGateway;

    @Override
    public Veiculo salvar(Veiculo veiculo) throws PlacaInvalidaException, CpfInvalidoException {

        Cliente cliente = clienteGateway.buscarPorId(veiculo.getClienteId())
                .orElseThrow(() -> new RegraNegocioException(
                        "Cliente não encontrado: " + veiculo.getClienteId()));

        VeiculoJpaEntity entity = mapper.toJpaEntity(veiculo, cliente);

        VeiculoJpaEntity salvo = repository.save(entity);

        return mapper.toDomain(salvo);
    }

    @Override
    public Optional<Veiculo> buscarPorId(UUID id) {

        return repository.findByIdAndAtivoTrue(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Veiculo> buscarPorPlaca(String placa) {

        return repository.findByPlacaAndAtivoTrue(placa).map(mapper::toDomain);
    }

    @Override
    public List<Veiculo> listar() {

        return repository.findByAtivoTrue().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Veiculo> buscarPorClienteId(UUID id) {
        return repository.findByClienteJpaEntityIdAndAtivoTrue(id)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
