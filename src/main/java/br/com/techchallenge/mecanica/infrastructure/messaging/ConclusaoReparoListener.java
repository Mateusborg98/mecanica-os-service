package br.com.techchallenge.mecanica.infrastructure.messaging;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.core.JacksonException;
import lombok.RequiredArgsConstructor;
import br.com.techchallenge.mecanica.application.input.ReparoConcluidoInput;
import br.com.techchallenge.mecanica.application.gateway.ConclusaoReparoGateway;
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name="app.messaging.enabled",havingValue="true")
public class ConclusaoReparoListener {
    private final ConclusaoReparoGateway gateway;
    private final JsonMapper json;
    @RabbitListener(queues="os.reparo-concluido.v1")
    public void receber(byte[] body) {
        try {
            var evento=json.readValue(body,ReparoConcluidoInput.class);
            if(evento==null) throw new IllegalArgumentException("Evento vazio");
            gateway.registrar(evento);
        } catch(JacksonException | IllegalArgumentException e) {
            throw new AmqpRejectAndDontRequeueException("Conclusão inválida ou incompatível",e);
        }
    }
}
