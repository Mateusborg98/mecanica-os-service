package br.com.techchallenge.mecanica.application.gateway;

import java.util.UUID;

public interface NotificacaoGateway {

    void notificarAlteracaoStatusOrdem(
            UUID ordemDeServicoId,
            String status,
            String mensagem);
}