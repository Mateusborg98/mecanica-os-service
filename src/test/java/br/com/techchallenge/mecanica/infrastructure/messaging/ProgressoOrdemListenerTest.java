package br.com.techchallenge.mecanica.infrastructure.messaging;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.dao.DataAccessResourceFailureException;
import br.com.techchallenge.mecanica.application.gateway.ProgressoOrdemGateway;
class ProgressoOrdemListenerTest {
    final ProgressoOrdemGateway gateway=mock(ProgressoOrdemGateway.class);
    final ProgressoOrdemListener listener=new ProgressoOrdemListener(gateway,new JsonMapper());
    byte[] fixture(String name) throws Exception { return getClass().getResourceAsStream("/contracts/"+name+".json").readAllBytes(); }
    @Test void contratosDosProdutoresAceitos() throws Exception {
        listener.decisao(fixture("orcamento-decidido-v2"));listener.inicio(fixture("reparo-iniciado-v1"));listener.cancelamento(fixture("reparo-cancelado-v1"));verify(gateway,times(3)).registrar(any());
    }
    @Test void invalidoOuVersaoIncorretaVaiParaDlq() throws Exception {
        assertThrows(AmqpRejectAndDontRequeueException.class,()->listener.decisao("{}".getBytes()));
        assertThrows(AmqpRejectAndDontRequeueException.class,()->listener.decisao(fixture("reparo-iniciado-v1")));
        verifyNoInteractions(gateway);
    }
    @Test void falhaBancoPermiteRetry() throws Exception {
        doThrow(new DataAccessResourceFailureException("offline")).when(gateway).registrar(any());
        assertThrows(DataAccessResourceFailureException.class,()->listener.decisao(fixture("orcamento-decidido-v2")));
    }
}
