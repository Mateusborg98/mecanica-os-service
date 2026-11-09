package br.com.techchallenge.mecanica.application.usecase.cliente;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.ClienteGateway;
import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.exception.ClienteNaoEncontradoException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuscarClientePorCpfCnpjUseCase {

    private final ClienteGateway clienteGateway;

    public Cliente executar(String cpfCnpj) {

        return clienteGateway.buscarPorCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new ClienteNaoEncontradoException(
                        "Cliente não encontrado"));
    }
}