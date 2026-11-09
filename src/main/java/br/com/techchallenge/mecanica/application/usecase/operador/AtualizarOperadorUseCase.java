package br.com.techchallenge.mecanica.application.usecase.operador;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.dto.operador.AtualizarOperadorInput;
import br.com.techchallenge.mecanica.application.gateway.OperadorGateway;
import br.com.techchallenge.mecanica.domain.operador.Operador;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AtualizarOperadorUseCase {

    private final OperadorGateway operadorGateway;

    public Operador executar(Integer matricula,
            AtualizarOperadorInput input) {

        Operador operador = operadorGateway.buscarPorMatricula(matricula)
                .orElseThrow(() -> new RuntimeException("Operador não encontrado"));

        operador.atualizarDados(
                input.nome(),
                input.email(),
                input.contato(),
                input.cargo());

        return operadorGateway.salvar(operador);
    }
}