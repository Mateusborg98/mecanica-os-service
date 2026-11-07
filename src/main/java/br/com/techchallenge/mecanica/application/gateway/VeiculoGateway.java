package br.com.techchallenge.mecanica.application.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;

public interface VeiculoGateway {

    Veiculo salvar(Veiculo veiculo);

    Optional<Veiculo> buscarPorId(UUID id);

    Optional<Veiculo> buscarPorPlaca(String placa);

    List<Veiculo> listar();

    List<Veiculo> buscarPorClienteId(UUID clienteId);
}