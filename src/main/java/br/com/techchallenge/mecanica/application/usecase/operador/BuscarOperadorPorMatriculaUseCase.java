package br.com.techchallenge.mecanica.application.usecase.operador;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.OperadorGateway;
import br.com.techchallenge.mecanica.domain.operador.Operador;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuscarOperadorPorMatriculaUseCase {

    private final OperadorGateway operadorGateway;

    public Operador executar(Integer matricula) {

        return operadorGateway.buscarPorMatricula(matricula)
                .orElseThrow(() -> new RuntimeException("Operador não encontrado"));
    }
}