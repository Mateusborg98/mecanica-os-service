package br.com.techchallenge.mecanica.infrastructure.messaging;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import br.com.techchallenge.mecanica.application.gateway.PagamentoOrdemGateway;
import br.com.techchallenge.mecanica.application.input.PagamentoConfirmadoInput;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import br.com.techchallenge.mecanica.domain.exception.OrdemDeServicoNaoEncontradaException;
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name="app.messaging.enabled", havingValue="true")
public class PagamentoConfirmadoListener {
    private final PagamentoOrdemGateway gateway;
    private final JsonMapper mapper;
    @RabbitListener(queues="os.pagamento.v1")
    public void receber(byte[] body) {
        final PagamentoConfirmadoInput evento;
        try { evento = mapper.readValue(body, PagamentoConfirmadoInput.class); }
        catch (RuntimeException e) { throw new AmqpRejectAndDontRequeueException("Evento inválido", e); }
        if (evento == null) throw new AmqpRejectAndDontRequeueException("Evento vazio");
        try { gateway.registrar(evento); }
        catch (RegraNegocioException | OrdemDeServicoNaoEncontradaException e) {
            throw new AmqpRejectAndDontRequeueException("Pagamento incompatível com a OS", e);
        }
    }
}
