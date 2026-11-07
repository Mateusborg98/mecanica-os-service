package br.com.techchallenge.mecanica.application.usecase.veiculo;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.VeiculoGateway;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListarVeiculosUseCase {

    private final VeiculoGateway gateway;

    public List<Veiculo> executar() {
        return gateway.listar();
    }
}
