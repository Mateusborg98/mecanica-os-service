package br.com.techchallenge.mecanica.presentation.mapper;

import org.springframework.stereotype.Component;

import br.com.techchallenge.mecanica.application.dto.operador.AtualizarOperadorInput;
import br.com.techchallenge.mecanica.application.dto.operador.CriarOperadorInput;
import br.com.techchallenge.mecanica.domain.operador.Operador;
import br.com.techchallenge.mecanica.presentation.dto.operador.AtualizarOperadorRequest;
import br.com.techchallenge.mecanica.presentation.dto.operador.CriarOperadorRequest;
import br.com.techchallenge.mecanica.presentation.dto.operador.OperadorResponse;

@Component
public class OperadorPresentationMapper {

    public CriarOperadorInput toInput(CriarOperadorRequest request) {
        return new CriarOperadorInput(
                request.nome(),
                request.matricula(),
                request.email(),
                request.contato(),
                request.cargo());
    }

    public AtualizarOperadorInput toInput(AtualizarOperadorRequest request) {
        return new AtualizarOperadorInput(
                request.nome(),
                request.email(),
                request.contato(),
                request.cargo());
    }

    // DOMAIN -> RESPONSE

    public OperadorResponse toResponse(Operador operador) {
        return new OperadorResponse(
                operador.getId(),
                operador.getNome(),
                operador.getMatricula(),
                operador.getEmail(),
                operador.getContato(),
                operador.getCargo(),
                operador.isAtivo(),
                operador.getDataInativacao());
    }
}