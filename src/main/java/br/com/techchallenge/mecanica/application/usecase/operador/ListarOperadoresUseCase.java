package br.com.techchallenge.mecanica.application.usecase.operador;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.OperadorGateway;
import br.com.techchallenge.mecanica.domain.operador.Operador;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListarOperadoresUseCase {

    private final OperadorGateway operadorGateway;

    public List<Operador> executar() {
        return operadorGateway.listar();
    }
}