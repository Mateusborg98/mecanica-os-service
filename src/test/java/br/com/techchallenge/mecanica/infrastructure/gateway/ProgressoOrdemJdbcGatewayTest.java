package br.com.techchallenge.mecanica.infrastructure.gateway;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import br.com.techchallenge.mecanica.application.input.*;
import static br.com.techchallenge.mecanica.application.input.ProgressoOrdemInput.Tipo.*;
class ProgressoOrdemJdbcGatewayTest {
    com.zaxxer.hikari.HikariDataSource ds;
    String schema;
    @AfterEach void fecharBanco() {
        if(ds!=null) {
            try { if(schema!=null && jdbc!=null) jdbc.execute("DROP SCHEMA "+schema+" CASCADE"); }
            finally { ds.close(); }
        }
    }

    JdbcTemplate jdbc;TransactionTemplate tx;ProgressoOrdemJdbcGateway gateway;
    UUID ordem,reparo,budget;Instant data=Instant.parse("2026-10-27T18:00:00Z");
    @BeforeEach void setup() throws Exception {
        ds=new com.zaxxer.hikari.HikariDataSource();
        if("true".equals(System.getenv("RUN_OS_POSTGRES_TESTS"))) {
            String url="jdbc:postgresql://localhost:15434/mecanica_os";
            schema="test_"+UUID.randomUUID().toString().replace("-","");
            new JdbcTemplate(new DriverManagerDataSource(url,"mecanica_os","mecanica_os_local")).execute("CREATE SCHEMA "+schema);
            ds.setJdbcUrl(url+"?currentSchema="+schema);ds.setUsername("mecanica_os");ds.setPassword("mecanica_os_local");
        } else { ds.setJdbcUrl("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");ds.setUsername("sa");ds.setPassword(""); }
        jdbc=new JdbcTemplate(ds);tx=new TransactionTemplate(new JdbcTransactionManager(ds));gateway=new ProgressoOrdemJdbcGateway(jdbc);
        jdbc.execute("CREATE TABLE ordem_de_servico_jpa_entity(id UUID PRIMARY KEY,status VARCHAR(32),valor_total_os NUMERIC(38,2),dt_inicio_os TIMESTAMP,dt_fim_os TIMESTAMP)");
        for(String migration:new String[]{"V7__historico_os.sql","V3__pagamento_ordem.sql","V4__solicitacoes_reparo.sql","V5__conclusoes_reparo.sql","V6__progresso_os.sql"})
            jdbc.execute(Files.readString(Path.of("src/main/resources/db/migration/"+migration)));
        ordem=UUID.randomUUID();reparo=UUID.randomUUID();budget=UUID.nameUUIDFromBytes(("mecanica:diagnostico:v1:"+reparo).getBytes(StandardCharsets.UTF_8));
        jdbc.update("INSERT INTO ordem_de_servico_jpa_entity(id,status,valor_total_os) VALUES (?,'RECEBIDA',0)",ordem);
        var antes=Timestamp.from(data.minusSeconds(60));
        jdbc.update("INSERT INTO solicitacoes_reparo(ordem_id,reparo_id,ocorrido_em,tentativa_em) VALUES (?,?,?,?)",ordem,reparo,antes,antes);
    }
    ProgressoOrdemInput evento(ProgressoOrdemInput.Tipo tipo) {
        return new ProgressoOrdemInput(tipo,tipo==DECISAO?budget:reparo,ordem,reparo,tipo==DIAGNOSTICO?null:budget,
            tipo==DECISAO?"APROVADO":null,tipo==DECISAO?new BigDecimal("51"):null,tipo==DECISAO?"BRL":null,
            data.plusSeconds(tipo==DIAGNOSTICO?0:tipo==DECISAO?10:20));
    }
    void receber(ProgressoOrdemInput e) { tx.executeWithoutResult(s->gateway.registrar(e)); }
    String status() { return jdbc.queryForObject("SELECT status FROM ordem_de_servico_jpa_entity WHERE id=?",String.class,ordem); }
    void concluir() { tx.executeWithoutResult(s->new ConclusaoReparoJdbcGateway(jdbc).registrar(new ReparoConcluidoInput(1,"ReparoConcluido",reparo,reparo,ordem,budget,data.plusSeconds(30)))); }
    @Test void percorreEstadosEAtualizaValorAutoritativo() {
        receber(evento(DIAGNOSTICO));assertEquals("AGUARDANDO_APROVACAO",status());
        receber(evento(DECISAO));assertEquals("NA_FILA",status());
        receber(evento(INICIO));assertEquals("EM_EXECUCAO",status());
        assertEquals(0,new BigDecimal("51").compareTo(jdbc.queryForObject("SELECT valor_total_os FROM ordem_de_servico_jpa_entity",BigDecimal.class)));
        concluir();assertEquals("FINALIZADA",status());
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"012","021","102","120","201","210"})
    void aceitaQualquerOrdemSemRegredirConclusao(String sequencia) {
        concluir();var tipos=List.of(DIAGNOSTICO,DECISAO,INICIO);
        for(char c:sequencia.toCharArray()) { var e=evento(tipos.get(c-'0'));receber(e);receber(e);assertEquals("FINALIZADA",status()); }
        assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM eventos_progresso_os",Integer.class));
        assertEquals(0,new BigDecimal("51").compareTo(jdbc.queryForObject("SELECT valor FROM progresso_os",BigDecimal.class)));
    }
    @Test void inicioAntesDaAprovacaoNaoRegrideParaFila() { receber(evento(INICIO));receber(evento(DECISAO));receber(evento(DIAGNOSTICO));assertEquals("EM_EXECUCAO",status()); }
    @Test void recusaCancelaESemExecucaoPosterior() {
        var d=evento(DECISAO);receber(new ProgressoOrdemInput(DECISAO,budget,ordem,reparo,budget,"RECUSADO",d.valor(),"BRL",d.ocorridoEm()));
        receber(evento(DIAGNOSTICO));assertEquals("CANCELADA",status());
        assertThrows(IllegalArgumentException.class,()->receber(evento(INICIO)));assertThrows(IllegalArgumentException.class,this::concluir);
    }
    @Test void conteudoConflitanteNaoAlteraValor() {
        receber(evento(DECISAO));var d=evento(DECISAO);
        assertThrows(IllegalArgumentException.class,()->receber(new ProgressoOrdemInput(DECISAO,budget,ordem,reparo,budget,"APROVADO",new BigDecimal("52"),"BRL",d.ocorridoEm())));
        assertEquals(0,new BigDecimal("51").compareTo(jdbc.queryForObject("SELECT valor FROM progresso_os",BigDecimal.class)));
    }
    @Test void rollbackReverteValorEInbox() {
        tx.executeWithoutResult(s->{gateway.registrar(evento(DECISAO));s.setRollbackOnly();});
        assertEquals("RECEBIDA",status());assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM eventos_progresso_os",Integer.class));
    }
    @Test void pagamentoAntecipadoSoLiberaComAprovacaoEConclusaoCorrelacionadas() {
        var pagamento=new PagamentoOrdemJdbcGateway(jdbc);
        tx.executeWithoutResult(s->pagamento.registrar(new PagamentoConfirmadoInput(1,"PagamentoConfirmado",budget,budget,ordem,reparo,new BigDecimal("51"),"BRL",data.plusSeconds(40))));
        assertThrows(br.com.techchallenge.mecanica.domain.exception.RegraNegocioException.class,()->tx.executeWithoutResult(s->pagamento.validarParaEntrega(ordem)));
        concluir();receber(evento(DECISAO));
        tx.executeWithoutResult(s->pagamento.validarParaEntrega(ordem));
    }
    ProgressoOrdemInput cancelamento() {
        return new ProgressoOrdemInput(CANCELAMENTO,reparo,ordem,reparo,null,null,null,null,data.plusSeconds(25));
    }
    @Test void cancelamentoAntesDosOutrosEventosPermaneceTerminal() {
        receber(cancelamento());receber(cancelamento());receber(evento(DECISAO));receber(evento(DIAGNOSTICO));
        assertEquals("CANCELADA",status());assertThrows(IllegalArgumentException.class,()->receber(evento(INICIO)));
        assertThrows(IllegalArgumentException.class,this::concluir);
        assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM eventos_progresso_os",Integer.class));
    }
    @Test void cancelamentoNaoDesfazExecucaoOuPagamento() {
        receber(evento(INICIO));assertThrows(IllegalArgumentException.class,()->receber(cancelamento()));
        assertEquals("EM_EXECUCAO",status());
        jdbc.update("UPDATE ordem_de_servico_jpa_entity SET status='NA_FILA'");jdbc.update("UPDATE progresso_os SET inicio_em=NULL");
        tx.executeWithoutResult(s->new PagamentoOrdemJdbcGateway(jdbc).registrar(new PagamentoConfirmadoInput(1,"PagamentoConfirmado",budget,budget,ordem,reparo,new BigDecimal("51"),"BRL",data.plusSeconds(40))));
        assertThrows(IllegalArgumentException.class,()->receber(cancelamento()));assertEquals("NA_FILA",status());
    }
    @Test void rollbackDoCancelamentoReverteEstadoERecibo() {
        tx.executeWithoutResult(s->{gateway.registrar(cancelamento());s.setRollbackOnly();});
        assertEquals("RECEBIDA",status());assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM eventos_progresso_os",Integer.class));
    }

}
