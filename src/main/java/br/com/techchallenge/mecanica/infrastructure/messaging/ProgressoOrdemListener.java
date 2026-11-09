package br.com.techchallenge.mecanica.infrastructure.messaging;
import java.util.UUID;
import java.time.Instant;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import br.com.techchallenge.mecanica.application.input.ProgressoOrdemInput;
import br.com.techchallenge.mecanica.application.gateway.ProgressoOrdemGateway;
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name="app.messaging.enabled",havingValue="true")
public class ProgressoOrdemListener {
    private final ProgressoOrdemGateway gateway;
    private final JsonMapper json;
    @RabbitListener(queues="os.diagnostico.v1") public void diagnostico(byte[] body) { receber(body,ProgressoOrdemInput.Tipo.DIAGNOSTICO); }
    @RabbitListener(queues="os.orcamento.v2") public void decisao(byte[] body) { receber(body,ProgressoOrdemInput.Tipo.DECISAO); }
    @RabbitListener(queues="os.reparo-iniciado.v1") public void inicio(byte[] body) { receber(body,ProgressoOrdemInput.Tipo.INICIO); }
    @RabbitListener(queues="os.reparo-cancelado.v1") public void cancelamento(byte[] body) { receber(body,ProgressoOrdemInput.Tipo.CANCELAMENTO); }
    private void receber(byte[] body,ProgressoOrdemInput.Tipo tipo) {
        final ProgressoOrdemInput evento;
        try {
            var n=json.readTree(body);boolean decisao=tipo==ProgressoOrdemInput.Tipo.DECISAO;
            String expected=switch(tipo) { case DIAGNOSTICO -> "DiagnosticoRegistrado";case DECISAO -> "OrcamentoDecidido";case INICIO -> "ReparoIniciado";case CANCELAMENTO -> "ReparoCancelado"; };
            if(n.get("schemaVersion").intValue()!=(decisao?2:1) || !expected.equals(n.get("eventType").stringValue())) throw new IllegalArgumentException("Contrato inválido");
            if(decisao && !n.get("valorTotal").isNumber()) throw new IllegalArgumentException("Valor inválido");
            evento=new ProgressoOrdemInput(tipo,UUID.fromString(n.get("eventId").stringValue()),UUID.fromString(n.get("ordemId").stringValue()),
                UUID.fromString(n.get("reparoId").stringValue()),(tipo==ProgressoOrdemInput.Tipo.DIAGNOSTICO || tipo==ProgressoOrdemInput.Tipo.CANCELAMENTO)?null:UUID.fromString(n.get("orcamentoId").stringValue()),
                decisao?n.get("decisao").stringValue():null,decisao?n.get("valorTotal").decimalValue():null,decisao?n.get("moeda").stringValue():null,
                Instant.parse(n.get("ocorridoEm").stringValue()));
        } catch(RuntimeException e) { throw new AmqpRejectAndDontRequeueException("Envelope inválido",e); }
        try { gateway.registrar(evento); }
        catch(IllegalArgumentException e) { throw new AmqpRejectAndDontRequeueException("Progresso incompatível com a OS",e); }
    }
}
