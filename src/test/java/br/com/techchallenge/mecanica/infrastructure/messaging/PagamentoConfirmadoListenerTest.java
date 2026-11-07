package br.com.techchallenge.mecanica.infrastructure.messaging;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.dao.DataAccessResourceFailureException;
import tools.jackson.databind.json.JsonMapper;
import br.com.techchallenge.mecanica.application.gateway.PagamentoOrdemGateway;
class PagamentoConfirmadoListenerTest {
    final PagamentoOrdemGateway gateway=mock(PagamentoOrdemGateway.class);
    final PagamentoConfirmadoListener listener=new PagamentoConfirmadoListener(gateway,JsonMapper.builder().findAndAddModules().build());
    final String json="""
    {"schemaVersion":1,"eventType":"PagamentoConfirmado","eventId":"1b1ef61f-d9f9-3bef-aaf5-308c8b215276",
    "cobrancaId":"1b1ef61f-d9f9-3bef-aaf5-308c8b215276","ordemId":"22222222-2222-2222-2222-222222222222",
    "reparoId":"11111111-1111-1111-1111-111111111111","valor":51.0,"moeda":"BRL","confirmadoEm":"2026-10-27T15:00:00Z"}
    """;
    @Test void contratoDoBillingAceito() { listener.receber(json.getBytes(java.nio.charset.StandardCharsets.UTF_8)); verify(gateway).registrar(any()); }
    @Test void malformadoEVersaoDesconhecidaRejeitados() {
        for(String s: new String[]{"{", "null", json.replace("schemaVersion\":1", "schemaVersion\":2"),json.replace("BRL","USD"),json.replace("51.0","-1")})
            assertThrows(AmqpRejectAndDontRequeueException.class,()->listener.receber(s.getBytes()));
        verifyNoInteractions(gateway);
    }
    @Test void erroTransitorioPermiteRetryDoContainer() {
        doThrow(new DataAccessResourceFailureException("offline")).when(gateway).registrar(any());
        assertThrows(DataAccessResourceFailureException.class,()->listener.receber(json.getBytes()));
    }
}
