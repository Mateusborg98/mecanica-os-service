package br.com.techchallenge.mecanica.application.usecase.cliente;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.ClienteGateway;
import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.exception.ClienteNaoEncontradoException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InativarClienteUseCase {

    private final ClienteGateway clienteGateway;

    public void executar(String cpfCnpj) {

        Cliente cliente = clienteGateway
                .buscarPorCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpfCnpj));

        cliente.inativar();

        clienteGateway.salvar(cliente);
    }
}