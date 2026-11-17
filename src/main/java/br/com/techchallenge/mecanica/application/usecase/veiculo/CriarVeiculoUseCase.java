package br.com.techchallenge.mecanica.application.usecase.veiculo;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.dto.veiculo.CriarVeiculoInput;
import br.com.techchallenge.mecanica.application.gateway.ClienteGateway;
import br.com.techchallenge.mecanica.application.gateway.VeiculoGateway;
import br.com.techchallenge.mecanica.domain.exception.ClienteNaoEncontradoException;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import br.com.techchallenge.mecanica.domain.veiculo.valueObject.Placa;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CriarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;
    private final ClienteGateway clienteGateway;

    public Veiculo executar(
            CriarVeiculoInput input) {

        UUID clienteId = clienteGateway.buscarPorCpfCnpj(
                input.cpfCnpj())
                .orElseThrow(() -> new ClienteNaoEncontradoException(
                        "Cliente não encontrado")).getId();

        veiculoGateway.buscarPorPlaca(
                input.placa())
                .ifPresent(v -> {
                    throw new RegraNegocioException(
                            "Placa já cadastrada");
                });

        Veiculo veiculo = new Veiculo(
                new Placa(input.placa()),
                input.marca(),
                input.modelo(),
                input.ano(),
                clienteId);

        return veiculoGateway.salvar(
                veiculo);
    }
}