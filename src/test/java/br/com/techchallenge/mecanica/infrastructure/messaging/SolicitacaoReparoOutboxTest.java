package br.com.techchallenge.mecanica.infrastructure.messaging;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
class SolicitacaoReparoOutboxTest {
    JdbcTemplate jdbc; SolicitacaoReparoOutbox outbox; TransactionTemplate tx; UUID id;
    @BeforeEach void setup() throws Exception {
        var ds=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL;DB_CLOSE_DELAY=-1","sa","");
        jdbc=new JdbcTemplate(ds); var tm=new JdbcTransactionManager(ds); tx=new TransactionTemplate(tm);
        jdbc.execute("CREATE TABLE ordem_de_servico_jpa_entity(id UUID PRIMARY KEY)");
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/migration/V7__historico_os.sql")));
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/migration/V4__solicitacoes_reparo.sql")));
        outbox=new SolicitacaoReparoOutbox(jdbc,mock(EntityManager.class),tm);
        id=UUID.randomUUID(); jdbc.update("INSERT INTO ordem_de_servico_jpa_entity VALUES (?)",id);
    }
    @Test void rollbackDaAberturaRemoveSolicitacao() {
        tx.executeWithoutResult(s -> { outbox.solicitar(id); s.setRollbackOnly(); });
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM solicitacoes_reparo",Integer.class));
    }
    @Test void solicitaEPublicaComVinculoEstavel() {
        outbox.solicitar(id); var vinculo=outbox.buscar(id); assertFalse(vinculo.publicado());
        var item=outbox.reservar(Instant.now().plusSeconds(1)).orElseThrow();
        assertEquals(vinculo.reparoId(),item.evento().reparoId()); assertEquals(id,item.evento().eventId());
        outbox.confirmar(item,Instant.now()); assertTrue(outbox.buscar(id).publicado());
        assertTrue(outbox.reservar(Instant.now().plusSeconds(100)).isEmpty());
    }
    @Test void leaseImpedeDoisWorkersEExpiracaoPermiteRecuperacao() {
        outbox.solicitar(id); var now=Instant.now().plusSeconds(1);
        var primeiro=outbox.reservar(now).orElseThrow(); assertTrue(outbox.reservar(now).isEmpty());
        var segundo=outbox.reservar(now.plusSeconds(31)).orElseThrow();
        outbox.confirmar(primeiro,now); assertFalse(outbox.buscar(id).publicado());
        outbox.confirmar(segundo,now); assertTrue(outbox.buscar(id).publicado());
        assertEquals(primeiro.evento(),segundo.evento());
    }
    @Test void falhaDoBrokerReagendaSemPerderSolicitacao() {
        outbox.solicitar(id);
        new SolicitacaoReparoWorker(outbox, e -> { throw new IllegalStateException("offline"); }).publicar();
        assertFalse(outbox.buscar(id).publicado()); assertTrue(outbox.reservar(Instant.now()).isEmpty());
        assertTrue(outbox.reservar(Instant.now().plusSeconds(31)).isPresent());
    }
    @Test void workerConfirmaPublicacaoEIgnoraFilaVazia() {
        var count=new java.util.concurrent.atomic.AtomicInteger();
        var worker=new SolicitacaoReparoWorker(outbox,e->count.incrementAndGet());
        worker.publicar(); assertEquals(0,count.get()); outbox.solicitar(id);
        worker.publicar(); assertEquals(1,count.get()); assertTrue(outbox.buscar(id).publicado());
    }
}
