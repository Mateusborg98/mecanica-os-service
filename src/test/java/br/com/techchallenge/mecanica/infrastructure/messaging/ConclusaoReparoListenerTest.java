package br.com.techchallenge.mecanica.infrastructure.messaging;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.dao.DataAccessResourceFailureException;
import br.com.techchallenge.mecanica.application.gateway.ConclusaoReparoGateway;
class ConclusaoReparoListenerTest {
    final ConclusaoReparoGateway gateway=mock(ConclusaoReparoGateway.class);
    final ConclusaoReparoListener listener=new ConclusaoReparoListener(gateway,new JsonMapper());
    final String json="""
    {"schemaVersion":1,"eventType":"ReparoConcluido","eventId":"11111111-1111-1111-1111-111111111111",
    "reparoId":"11111111-1111-1111-1111-111111111111","ordemId":"22222222-2222-2222-2222-222222222222",
    "orcamentoId":"1b1ef61f-d9f9-3bef-aaf5-308c8b215276","ocorridoEm":"2026-10-27T18:00:00.123456Z"}
    """;
    @Test void aceitaContrato() { listener.receber(json.getBytes());verify(gateway).registrar(any()); }
    @Test void rejeitaEnvelopeInvalido() {
        for(String body:new String[]{"{","null",json.replace("ReparoConcluido","Outro"),json.replace("schemaVersion\":1","schemaVersion\":2")})
            assertThrows(AmqpRejectAndDontRequeueException.class,()->listener.receber(body.getBytes()));
        verifyNoInteractions(gateway);
    }
    @Test void erroTransitorioPodeSerRetentado() {
        doThrow(new DataAccessResourceFailureException("offline")).when(gateway).registrar(any());
        assertThrows(DataAccessResourceFailureException.class,()->listener.receber(json.getBytes()));
    }
}
