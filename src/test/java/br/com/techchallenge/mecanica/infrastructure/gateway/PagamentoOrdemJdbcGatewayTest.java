package br.com.techchallenge.mecanica.infrastructure.gateway;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import br.com.techchallenge.mecanica.application.input.PagamentoConfirmadoInput;
import br.com.techchallenge.mecanica.domain.exception.*;
class PagamentoOrdemJdbcGatewayTest {
    com.zaxxer.hikari.HikariDataSource ds;
    String schema;
    @AfterEach void fecharBanco() {
        if(ds!=null) {
            try { if(schema!=null && jdbc!=null) jdbc.execute("DROP SCHEMA "+schema+" CASCADE"); }
            finally { ds.close(); }
        }
    }

    JdbcTemplate jdbc; PagamentoOrdemJdbcGateway gateway; TransactionTemplate tx;
    UUID ordem, reparo; PagamentoConfirmadoInput evento;
    @BeforeEach void setup() throws Exception {
        ds=new com.zaxxer.hikari.HikariDataSource();
        if("true".equals(System.getenv("RUN_OS_POSTGRES_TESTS"))) {
            String url="jdbc:postgresql://localhost:15434/mecanica_os";
            schema="test_"+UUID.randomUUID().toString().replace("-","");
            new JdbcTemplate(new DriverManagerDataSource(url,"mecanica_os","mecanica_os_local")).execute("CREATE SCHEMA "+schema);
            ds.setJdbcUrl(url+"?currentSchema="+schema);ds.setUsername("mecanica_os");ds.setPassword("mecanica_os_local");
        } else { ds.setJdbcUrl("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");ds.setUsername("sa");ds.setPassword(""); }
        jdbc = new JdbcTemplate(ds); tx = new TransactionTemplate(new JdbcTransactionManager(ds));
        jdbc.execute("CREATE TABLE ordem_de_servico_jpa_entity (id UUID PRIMARY KEY, valor_total_os NUMERIC(38,2), status VARCHAR(32) DEFAULT 'FINALIZADA')");
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/migration/V3__pagamento_ordem.sql")));
        for(String migration: new String[]{"V7__historico_os.sql","V4__solicitacoes_reparo.sql","V5__conclusoes_reparo.sql","V6__progresso_os.sql"})
            jdbc.execute(Files.readString(Path.of("src/main/resources/db/migration/"+migration)));
        gateway = new PagamentoOrdemJdbcGateway(jdbc); ordem=UUID.randomUUID(); reparo=UUID.randomUUID();
        jdbc.update("INSERT INTO ordem_de_servico_jpa_entity(id,valor_total_os) VALUES (?,51)", ordem);
        evento=evento(reparo, "51.00", Instant.parse("2026-10-27T15:00:00Z"));
        var data=java.sql.Timestamp.from(evento.confirmadoEm());
        jdbc.update("INSERT INTO solicitacoes_reparo(ordem_id,reparo_id,ocorrido_em,tentativa_em) VALUES (?,?,?,?)",ordem,reparo,data,data);
        jdbc.update("INSERT INTO progresso_os(ordem_id,reparo_id,orcamento_id,decisao,valor,moeda) VALUES (?,?,?,'APROVADO',51,'BRL')",ordem,reparo,evento.cobrancaId());
        jdbc.update("INSERT INTO conclusoes_reparo_recebidas(event_id,ordem_id,reparo_id,orcamento_id,ocorrido_em) VALUES (?,?,?,?,?)",reparo,ordem,reparo,evento.cobrancaId(),data);
    }
    PagamentoConfirmadoInput evento(UUID reparo, String valor, Instant data) {
        UUID id=UUID.nameUUIDFromBytes(("mecanica:diagnostico:v1:"+reparo).getBytes(StandardCharsets.UTF_8));
        return new PagamentoConfirmadoInput(1,"PagamentoConfirmado",id,id,ordem,reparo,new BigDecimal(valor),"BRL",data);
    }
    void registrar(PagamentoConfirmadoInput e) { tx.executeWithoutResult(s -> gateway.registrar(e)); }
    void validar(UUID id) { tx.executeWithoutResult(s -> gateway.validarParaEntrega(id)); }
    @Test void bloqueiaSemPagamentoELiberaAposRegistroIdempotente() {
        assertThrows(RegraNegocioException.class, () -> validar(ordem));
        registrar(evento); registrar(evento(reparo,"51.0",evento.confirmadoEm())); validar(ordem);
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM pagamento_ordem",Integer.class));
    }
    @Test void rejeitaValorDivergenteSemPersistir() {
        assertThrows(RegraNegocioException.class, () -> registrar(evento(reparo,"52",evento.confirmadoEm())));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM pagamento_ordem",Integer.class));
    }
    @Test void rejeitaConflitoSemSobrescrever() {
        registrar(evento);
        assertThrows(RegraNegocioException.class, () -> registrar(evento(UUID.randomUUID(),"51",evento.confirmadoEm())));
        assertThrows(RegraNegocioException.class, () -> registrar(evento(reparo,"51",evento.confirmadoEm().plusSeconds(1))));
        validar(ordem);
    }
    @Test void mudancaDeValorNaoLiberaEntrega() {
        registrar(evento); jdbc.update("UPDATE ordem_de_servico_jpa_entity SET valor_total_os=52 WHERE id=?",ordem);
        assertThrows(RegraNegocioException.class, () -> validar(ordem));
    }
    @Test void osAusenteRejeitaPagamentoEEntrega() {
        jdbc.update("DELETE FROM progresso_os");jdbc.update("DELETE FROM conclusoes_reparo_recebidas");jdbc.update("DELETE FROM solicitacoes_reparo");
        jdbc.update("DELETE FROM ordem_de_servico_jpa_entity");
        assertThrows(OrdemDeServicoNaoEncontradaException.class, () -> registrar(evento));
        assertThrows(OrdemDeServicoNaoEncontradaException.class, () -> validar(ordem));
    }
    @Test void rollbackNaoDeixaComprovante() {
        tx.executeWithoutResult(s -> { gateway.registrar(evento); s.setRollbackOnly(); });
        assertThrows(RegraNegocioException.class, () -> validar(ordem));
    }
    @Test void duasEntregasDoMesmoEventoSaoIdempotentes() throws Exception {
        try(var pool=java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var a=pool.submit(() -> registrar(evento)); var b=pool.submit(() -> registrar(evento));
            a.get(10,java.util.concurrent.TimeUnit.SECONDS); b.get(10,java.util.concurrent.TimeUnit.SECONDS);
        }
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM pagamento_ordem",Integer.class));
    }
    @Test void pagamentoAntesDaAprovacaoFicaPersistidoMasNaoLiberaEntrega() {
        jdbc.update("UPDATE progresso_os SET decisao=NULL,valor=NULL,moeda=NULL WHERE ordem_id=?",ordem);
        jdbc.update("UPDATE ordem_de_servico_jpa_entity SET valor_total_os=0 WHERE id=?",ordem);
        registrar(evento);assertThrows(RegraNegocioException.class,()->validar(ordem));
        jdbc.update("UPDATE progresso_os SET decisao='APROVADO',valor=51,moeda='BRL' WHERE ordem_id=?",ordem);
        jdbc.update("UPDATE ordem_de_servico_jpa_entity SET valor_total_os=51 WHERE id=?",ordem);
        validar(ordem);
    }
    @Test void semConclusaoOuReparoDiferenteNaoLibera() {
        assertThrows(RegraNegocioException.class,()->registrar(evento(UUID.randomUUID(),"51",evento.confirmadoEm())));
        registrar(evento);jdbc.update("DELETE FROM conclusoes_reparo_recebidas");
        assertThrows(RegraNegocioException.class,()->validar(ordem));
    }
}
