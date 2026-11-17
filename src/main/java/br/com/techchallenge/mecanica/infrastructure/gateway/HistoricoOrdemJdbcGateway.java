package br.com.techchallenge.mecanica.infrastructure.gateway;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import br.com.techchallenge.mecanica.application.gateway.HistoricoOrdemGateway;
import br.com.techchallenge.mecanica.domain.exception.OrdemDeServicoNaoEncontradaException;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class HistoricoOrdemJdbcGateway implements HistoricoOrdemGateway {
    private final JdbcTemplate jdbc;
    /** Called inside the same transaction as the corresponding state/inbox change. */
    public static void registrar(JdbcTemplate jdbc, UUID ordem, String tipo, UUID eventId, String origem,
                                 String anterior, String atual, Instant ocorrido) {
        jdbc.update("""
            INSERT INTO historico_os(ordem_id,tipo,event_id,origem,status_anterior,status_atual,ocorrido_em)
            VALUES (?,?,?,?,?,?,?) ON CONFLICT DO NOTHING
            """, ordem,tipo,eventId,origem,anterior,atual,ocorrido == null ? null : Timestamp.from(ocorrido));
    }
    @Override
    @Transactional(readOnly = true)
    public List<Evento> buscar(UUID ordem, long apos) {
        if (jdbc.queryForList("SELECT id FROM ordem_de_servico_jpa_entity WHERE id=?", UUID.class, ordem).isEmpty())
            throw new OrdemDeServicoNaoEncontradaException("Ordem de serviço não encontrada");
        return jdbc.query("SELECT * FROM historico_os WHERE ordem_id=? AND sequencia>? ORDER BY sequencia LIMIT 100", (rs,n) -> {
            var ocorrido = rs.getTimestamp("ocorrido_em");
            return new Evento(rs.getLong("sequencia"),rs.getString("tipo"),rs.getObject("event_id",UUID.class),
                rs.getString("origem"),rs.getString("status_anterior"),rs.getString("status_atual"),
                ocorrido == null ? null : ocorrido.toInstant(),rs.getTimestamp("registrado_em").toInstant());
        },ordem,apos);
    }
}
