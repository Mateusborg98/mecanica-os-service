package br.com.techchallenge.mecanica.application.usecase.veiculo;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.ClienteGateway;
import br.com.techchallenge.mecanica.application.gateway.VeiculoGateway;
import br.com.techchallenge.mecanica.domain.exception.ClienteNaoEncontradoException;
import br.com.techchallenge.mecanica.domain.exception.VeiculoNaoEncontradoException;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlterarClienteDoVeiculoUseCase {

        private final VeiculoGateway veiculoGateway;
        private final ClienteGateway clienteGateway;

        public Veiculo executar(
                        UUID veiculoId,
                        UUID clienteId) {

                Veiculo veiculo = veiculoGateway
                                .buscarPorId(veiculoId)
                                .orElseThrow(() -> new VeiculoNaoEncontradoException(
                                                "Veículo não encontrado"));

                clienteGateway.buscarPorId(clienteId)
                                .orElseThrow(() -> new ClienteNaoEncontradoException(
                                                "Cliente não encontrado"));

                veiculo.alterarCliente(clienteId);

                return veiculoGateway.salvar(veiculo);
        }
}