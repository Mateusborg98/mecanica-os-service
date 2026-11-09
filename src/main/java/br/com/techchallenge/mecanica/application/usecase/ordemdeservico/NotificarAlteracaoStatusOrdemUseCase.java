package br.com.techchallenge.mecanica.application.usecase.ordemdeservico;

import org.springframework.stereotype.Service;

import br.com.techchallenge.mecanica.application.gateway.NotificacaoGateway;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificarAlteracaoStatusOrdemUseCase {

    private final NotificacaoGateway notificacaoGateway;

    public void executar(OrdemDeServico ordem) {
        if (ordem == null || ordem.getId() == null || ordem.getStatus() == null) {
            return;
        }

        String status = ordem.getStatus().name();

        String mensagem = "A ordem de serviço "
                + ordem.getId()
                + " teve o status alterado para "
                + status
                + ".";

        notificacaoGateway.notificarAlteracaoStatusOrdem(
                ordem.getId(),
                status,
                mensagem);
    }
}