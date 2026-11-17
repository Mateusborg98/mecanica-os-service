package br.com.techchallenge.mecanica.application.usecase.veiculo;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.VeiculoGateway;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuscarVeiculoPorPlacaUseCase {

    private final VeiculoGateway gateway;

    public Veiculo executar(String placa) {

        return gateway.buscarPorPlaca(placa).orElseThrow(() ->
                new RegraNegocioException("Veiculo não encontrado!"));

    }
}
