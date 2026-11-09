package br.com.techchallenge.mecanica.application.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.exception.CpfInvalidoException;

public interface ClienteGateway {

    Cliente salvar(Cliente cliente) throws CpfInvalidoException;

    Optional<Cliente> buscarPorCpfCnpj(String cpfCnpj);

    List<Cliente> listar();

    Optional<Cliente> buscarPorId(UUID clienteId);

}