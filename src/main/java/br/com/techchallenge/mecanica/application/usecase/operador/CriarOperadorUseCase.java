package br.com.techchallenge.mecanica.application.usecase.operador;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.dto.operador.CriarOperadorInput;
import br.com.techchallenge.mecanica.application.gateway.OperadorGateway;
import br.com.techchallenge.mecanica.domain.operador.Operador;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CriarOperadorUseCase {

    private final OperadorGateway operadorGateway;

    public Operador executar(CriarOperadorInput input) {

        Operador operador = new Operador(
                input.nome(),
                input.matricula(),
                input.email(),
                input.contato(),
                input.cargo());

        return operadorGateway.salvar(operador);
    }
}