package br.com.techchallenge.mecanica.infrastructure.gateway;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import br.com.techchallenge.mecanica.application.input.ReparoConcluidoInput;
class ConclusaoReparoJdbcGatewayTest {
    JdbcTemplate jdbc; TransactionTemplate tx; ConclusaoReparoJdbcGateway gateway;
    UUID ordem,reparo; ReparoConcluidoInput evento;
    @BeforeEach void setup() throws Exception {
        var ds=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL;DB_CLOSE_DELAY=-1","sa","");
        jdbc=new JdbcTemplate(ds);tx=new TransactionTemplate(new JdbcTransactionManager(ds));gateway=new ConclusaoReparoJdbcGateway(jdbc);
        jdbc.execute("CREATE TABLE ordem_de_servico_jpa_entity(id UUID PRIMARY KEY,status VARCHAR(32),dt_fim_os TIMESTAMP)");
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/migration/V7__historico_os.sql")));
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/migration/V4__solicitacoes_reparo.sql")));
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/migration/V5__conclusoes_reparo.sql")));
        ordem=UUID.randomUUID();reparo=UUID.randomUUID();evento=evento(reparo,Instant.parse("2026-10-27T18:00:00.123456Z"));
        jdbc.update("INSERT INTO ordem_de_servico_jpa_entity(id,status) VALUES (?,'RECEBIDA')",ordem);
        var antes=Timestamp.from(evento.ocorridoEm().minusSeconds(60));
        jdbc.update("INSERT INTO solicitacoes_reparo(ordem_id,reparo_id,ocorrido_em,tentativa_em) VALUES (?,?,?,?)",ordem,reparo,antes,antes);
    }
    ReparoConcluidoInput evento(UUID id,Instant data) {
        return new ReparoConcluidoInput(1,"ReparoConcluido",id,id,ordem,
            UUID.nameUUIDFromBytes(("mecanica:diagnostico:v1:"+id).getBytes(StandardCharsets.UTF_8)),data);
    }
    void registrar(ReparoConcluidoInput e) { tx.executeWithoutResult(s->gateway.registrar(e)); }
    String status() { return jdbc.queryForObject("SELECT status FROM ordem_de_servico_jpa_entity WHERE id=?",String.class,ordem); }
    @Test void finalizaComDataRecebidaEInbox() {
        registrar(evento); assertEquals("FINALIZADA",status());
        assertEquals(evento.ocorridoEm().atOffset(java.time.ZoneOffset.UTC).toLocalDateTime(),jdbc.queryForObject(
            "SELECT dt_fim_os FROM ordem_de_servico_jpa_entity WHERE id=?",(rs,n)->rs.getTimestamp(1).toLocalDateTime(),ordem));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM conclusoes_reparo_recebidas",Integer.class));
    }
    @Test void reentregaNaoRegrideOsEntregue() {
        registrar(evento);jdbc.update("UPDATE ordem_de_servico_jpa_entity SET status='ENTREGUE' WHERE id=?",ordem);
        registrar(evento);assertEquals("ENTREGUE",status());
    }
    @Test void rejeitaOutroReparoOuDataAnteriorAbertura() {
        assertThrows(IllegalArgumentException.class,()->registrar(evento(UUID.randomUUID(),evento.ocorridoEm())));
        assertThrows(IllegalArgumentException.class,()->registrar(evento(reparo,evento.ocorridoEm().minusSeconds(61))));
        assertEquals("RECEBIDA",status());
    }
    @Test void rejeitaRepeticaoComConteudoDiferente() {
        registrar(evento);assertThrows(IllegalArgumentException.class,()->registrar(evento(reparo,evento.ocorridoEm().plusSeconds(1))));
    }
    @Test void rollbackPreservaStatusEInbox() {
        tx.executeWithoutResult(s->{gateway.registrar(evento);s.setRollbackOnly();});
        assertEquals("RECEBIDA",status());assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM conclusoes_reparo_recebidas",Integer.class));
    }
    @Test void concorrenciaConfirmaUmaVez() throws Exception {
        try(var pool=java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var a=pool.submit(()->registrar(evento));var b=pool.submit(()->registrar(evento));
            a.get(10,java.util.concurrent.TimeUnit.SECONDS);b.get(10,java.util.concurrent.TimeUnit.SECONDS);
        }
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM conclusoes_reparo_recebidas",Integer.class));
    }
}
