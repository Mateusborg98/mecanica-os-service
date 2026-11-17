package br.com.techchallenge.mecanica.application.usecase.veiculo;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.dto.veiculo.AtualizarVeiculoInput;
import br.com.techchallenge.mecanica.application.gateway.VeiculoGateway;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import br.com.techchallenge.mecanica.domain.veiculo.valueObject.Placa;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AtualizarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    public Veiculo executar(
            UUID id,
            AtualizarVeiculoInput input) {

        Veiculo veiculo = veiculoGateway.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException(
                        "Veículo não encontrado"));

        veiculo.atualizarDados(
                new Placa(input.placa()),
                input.marca(),
                input.modelo(),
                input.ano());

        return veiculoGateway.salvar(veiculo);
    }
}