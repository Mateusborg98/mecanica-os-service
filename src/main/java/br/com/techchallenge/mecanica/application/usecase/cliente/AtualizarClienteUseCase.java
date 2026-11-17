package br.com.techchallenge.mecanica.application.usecase.cliente;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.dto.cliente.AtualizarClienteInput;
import br.com.techchallenge.mecanica.application.gateway.ClienteGateway;
import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.exception.ClienteNaoEncontradoException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AtualizarClienteUseCase {

    private final ClienteGateway clienteGateway;

    public Cliente executar(
            String cpfCnpj,
            AtualizarClienteInput input) {

        Cliente cliente = clienteGateway
                .buscarPorCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new ClienteNaoEncontradoException(
                        "Cliente não encontrado"));

        cliente.atualizarDados(
                input.nome(),
                input.contato(),
                input.email());

        return clienteGateway.salvar(cliente);
    }
}