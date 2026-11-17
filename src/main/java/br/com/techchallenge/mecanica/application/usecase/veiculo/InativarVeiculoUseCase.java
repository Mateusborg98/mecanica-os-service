package br.com.techchallenge.mecanica.application.usecase.veiculo;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.VeiculoGateway;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InativarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    public void executar(UUID id) {

        Veiculo veiculo = veiculoGateway.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException(
                        "Veículo não encontrado"));

        veiculo.inativar();

        veiculoGateway.salvar(veiculo);
    }
}