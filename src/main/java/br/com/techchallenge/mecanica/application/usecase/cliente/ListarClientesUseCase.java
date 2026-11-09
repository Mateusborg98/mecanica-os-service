package br.com.techchallenge.mecanica.application.usecase.cliente;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.ClienteGateway;
import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListarClientesUseCase {

    private final ClienteGateway clienteGateway;

    public List<Cliente> executar() {

        return clienteGateway.listar();
    }
}