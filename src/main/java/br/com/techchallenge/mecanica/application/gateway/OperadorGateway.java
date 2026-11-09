package br.com.techchallenge.mecanica.application.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.techchallenge.mecanica.domain.operador.Operador;

public interface OperadorGateway {

    Operador salvar(Operador operador);

    Optional<Operador> buscarPorId(UUID id);

    Optional<Operador> buscarPorMatricula(Integer matricula);

    List<Operador> listar();
}