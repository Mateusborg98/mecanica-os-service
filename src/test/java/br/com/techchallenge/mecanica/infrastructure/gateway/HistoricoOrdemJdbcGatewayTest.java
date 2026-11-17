package br.com.techchallenge.mecanica.infrastructure.gateway;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import br.com.techchallenge.mecanica.application.usecase.ordemdeservico.BuscarHistoricoOrdemUseCase;
import br.com.techchallenge.mecanica.domain.exception.OrdemDeServicoNaoEncontradaException;
import static org.junit.jupiter.api.Assertions.*;

class HistoricoOrdemJdbcGatewayTest {
    JdbcTemplate jdbc; TransactionTemplate tx; HistoricoOrdemJdbcGateway gateway; UUID ordem;
    @BeforeEach void setup() throws Exception {
        var ds=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL;DB_CLOSE_DELAY=-1","sa","");
        jdbc=new JdbcTemplate(ds); tx=new TransactionTemplate(new JdbcTransactionManager(ds)); gateway=new HistoricoOrdemJdbcGateway(jdbc);
        jdbc.execute("CREATE TABLE ordem_de_servico_jpa_entity(id UUID PRIMARY KEY,status VARCHAR(32))");
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/migration/V7__historico_os.sql")));
        ordem=UUID.randomUUID();jdbc.update("INSERT INTO ordem_de_servico_jpa_entity VALUES (?,'RECEBIDA')",ordem);
    }
    @AfterEach void close(){jdbc.execute("SHUTDOWN");}
    void registrar(UUID evento,String tipo,Instant instante){HistoricoOrdemJdbcGateway.registrar(jdbc,ordem,tipo,evento,"EXECUTION","RECEBIDA","AGUARDANDO_APROVACAO",instante);}
    @Test void registraRecebimentoSemInventarOrdemCronologicaDosEventos() {
        var inicio=Instant.parse("2026-11-05T12:00:00Z");
        registrar(UUID.randomUUID(),"INICIO",inicio);registrar(UUID.randomUUID(),"DIAGNOSTICO",inicio.minusSeconds(30));
        var result=new BuscarHistoricoOrdemUseCase(gateway).executar(ordem,0);
        assertEquals(2,result.size());assertEquals("INICIO",result.getFirst().tipo());
        assertTrue(result.get(1).ocorridoEm().isBefore(result.getFirst().ocorridoEm()));
        assertNotNull(result.getFirst().registradoEm());
        assertEquals(1,gateway.buscar(ordem,result.getFirst().sequencia()).size());
    }
    @Test void reentregaNaoDuplicaHistorico() {
        var event=UUID.randomUUID();var now=Instant.now();registrar(event,"DIAGNOSTICO",now);registrar(event,"DIAGNOSTICO",now);
        assertEquals(1,gateway.buscar(ordem,0).size());
    }
    @Test void rollbackDesfazHistoricoEEstadoJuntos() {
        assertThrows(IllegalStateException.class,()->tx.executeWithoutResult(s->{
            jdbc.update("UPDATE ordem_de_servico_jpa_entity SET status='CANCELADA' WHERE id=?",ordem);
            registrar(UUID.randomUUID(),"CANCELAMENTO",Instant.now());throw new IllegalStateException("simulada");
        }));
        assertTrue(gateway.buscar(ordem,0).isEmpty());assertEquals("RECEBIDA",jdbc.queryForObject("SELECT status FROM ordem_de_servico_jpa_entity WHERE id=?",String.class,ordem));
    }
    @Test void migraOrdensAntigasComoMarcoSemInventarData() throws Exception {
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/migration/V8__marco_historico_legado.sql")));
        var event=gateway.buscar(ordem,0).getFirst();assertEquals("MARCO_LEGADO",event.tipo());assertNull(event.ocorridoEm());assertEquals("RECEBIDA",event.statusAtual());
    }
    @Test void limitaPaginaEValidaOrdemECursor() {
        for(int i=0;i<105;i++)registrar(UUID.randomUUID(),"DIAGNOSTICO",Instant.now());
        var primeira=gateway.buscar(ordem,0);assertEquals(100,primeira.size());assertEquals(5,gateway.buscar(ordem,primeira.getLast().sequencia()).size());
        assertThrows(OrdemDeServicoNaoEncontradaException.class,()->gateway.buscar(UUID.randomUUID(),0));
        assertThrows(IllegalArgumentException.class,()->new BuscarHistoricoOrdemUseCase(gateway).executar(ordem,-1));
        assertThrows(IllegalArgumentException.class,()->new BuscarHistoricoOrdemUseCase(gateway).executar(null,0));
    }
}
