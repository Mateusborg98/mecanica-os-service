package br.com.techchallenge.mecanica.application.usecase.cliente;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.dto.cliente.CriarClienteInput;
import br.com.techchallenge.mecanica.application.gateway.ClienteGateway;
import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.cliente.valueobject.CpfCnpj;
import br.com.techchallenge.mecanica.domain.exception.CpfDuplicadoException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CriarClienteUseCase {

    private final ClienteGateway clienteGateway;

    public Cliente executar(CriarClienteInput input) {

        clienteGateway.buscarPorCpfCnpj(input.cpfCnpj())
                .ifPresent(cliente -> {
                    throw new CpfDuplicadoException(
                            "CPF/CNPJ já cadastrado");
                });

        Cliente cliente = new Cliente(
                input.nome(),
                new CpfCnpj(input.cpfCnpj()),
                input.contato(),
                input.email());

        return clienteGateway.salvar(cliente);
    }
}