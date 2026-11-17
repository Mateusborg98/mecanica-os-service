package br.com.techchallenge.mecanica.infrastructure.messaging;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import br.com.techchallenge.mecanica.application.gateway.SolicitarReparoGateway;
import br.com.techchallenge.mecanica.domain.exception.OrdemDeServicoNaoEncontradaException;
@Repository
public class SolicitacaoReparoOutbox implements SolicitarReparoGateway {
    public record Pendente(ReparoSolicitado evento, UUID token) {}
    private final JdbcTemplate jdbc;
    private final EntityManager em;
    private final TransactionTemplate tx;
    public SolicitacaoReparoOutbox(JdbcTemplate jdbc, EntityManager em, PlatformTransactionManager manager) {
        this.jdbc=jdbc; this.em=em; this.tx=new TransactionTemplate(manager);
    }
    public void solicitar(UUID ordemId) {
        tx.executeWithoutResult(s -> {
            // Flush the OS INSERT before the JDBC foreign key is checked in this same transaction.
            em.flush();
            var now=Timestamp.from(Instant.now().truncatedTo(ChronoUnit.MILLIS));
            jdbc.update("INSERT INTO solicitacoes_reparo(ordem_id,reparo_id,ocorrido_em,tentativa_em) VALUES (?,?,?,?)",
                    ordemId, UUID.randomUUID(), now, now);
            br.com.techchallenge.mecanica.infrastructure.gateway.HistoricoOrdemJdbcGateway.registrar(jdbc,ordemId,"ABERTURA",ordemId,"OS",null,"RECEBIDA",now.toInstant());
        });
    }
    public Vinculo buscar(UUID ordemId) {
        return jdbc.query("SELECT * FROM solicitacoes_reparo WHERE ordem_id=?", (rs,n) ->
                new Vinculo(ordemId,rs.getObject("reparo_id",UUID.class),rs.getTimestamp("publicado_em")!=null),ordemId)
                .stream().findFirst().orElseThrow(() -> new OrdemDeServicoNaoEncontradaException("Solicitação de reparo não encontrada"));
    }
    public Optional<Pendente> reservar(Instant now) {
        return tx.execute(s -> {
            var itens=jdbc.query("""
                SELECT * FROM solicitacoes_reparo WHERE publicado_em IS NULL AND tentativa_em <= ?
                AND (lease_ate IS NULL OR lease_ate <= ?) ORDER BY ocorrido_em LIMIT 1 FOR UPDATE SKIP LOCKED
                """, (rs,n) -> new ReparoSolicitado(1,"ReparoSolicitado",rs.getObject("ordem_id",UUID.class),
                    rs.getObject("ordem_id",UUID.class),rs.getObject("reparo_id",UUID.class),rs.getTimestamp("ocorrido_em").toInstant()),
                    Timestamp.from(now),Timestamp.from(now));
            if(itens.isEmpty()) return Optional.empty();
            var evento=itens.getFirst(); var token=UUID.randomUUID();
            jdbc.update("UPDATE solicitacoes_reparo SET lease_token=?, lease_ate=?, tentativas=tentativas+1 WHERE ordem_id=?",
                    token,Timestamp.from(now.plusSeconds(30)),evento.ordemId());
            return Optional.of(new Pendente(evento,token));
        });
    }
    public void confirmar(Pendente p, Instant now) {
        jdbc.update("UPDATE solicitacoes_reparo SET publicado_em=?,lease_token=NULL,lease_ate=NULL WHERE ordem_id=? AND lease_token=?",
                Timestamp.from(now),p.evento().ordemId(),p.token());
    }
    public void reagendar(Pendente p, Instant now) {
        jdbc.update("UPDATE solicitacoes_reparo SET tentativa_em=?,lease_token=NULL,lease_ate=NULL WHERE ordem_id=? AND lease_token=?",
                Timestamp.from(now.plusSeconds(30)),p.evento().ordemId(),p.token());
    }
}
