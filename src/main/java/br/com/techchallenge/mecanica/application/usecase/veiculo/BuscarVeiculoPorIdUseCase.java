package br.com.techchallenge.mecanica.application.usecase.veiculo;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.VeiculoGateway;
import br.com.techchallenge.mecanica.domain.exception.VeiculoNaoEncontradoException;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuscarVeiculoPorIdUseCase {

    private final VeiculoGateway gateway;

    public Veiculo executar(UUID id) {

        return gateway.buscarPorId(id).orElseThrow(() -> new VeiculoNaoEncontradoException("Veiculo não encontrado!"));
    }
}
