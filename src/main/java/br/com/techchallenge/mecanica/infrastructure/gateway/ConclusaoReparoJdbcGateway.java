package br.com.techchallenge.mecanica.infrastructure.gateway;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import br.com.techchallenge.mecanica.application.gateway.ConclusaoReparoGateway;
import br.com.techchallenge.mecanica.application.input.ReparoConcluidoInput;
@Repository
@RequiredArgsConstructor
public class ConclusaoReparoJdbcGateway implements ConclusaoReparoGateway {
    private final JdbcTemplate jdbc;
    @Transactional
    public void registrar(ReparoConcluidoInput e) {
        var estados=jdbc.queryForList("SELECT status FROM ordem_de_servico_jpa_entity WHERE id=? FOR UPDATE",String.class,e.ordemId());
        if(estados.isEmpty()) throw new IllegalArgumentException("OS não encontrada para conclusão");
        var vinculos=jdbc.query("SELECT reparo_id,ocorrido_em FROM solicitacoes_reparo WHERE ordem_id=?",
            (rs,n)->rs.getObject(1,UUID.class).equals(e.reparoId()) && !e.ocorridoEm().isBefore(rs.getTimestamp(2).toInstant()),e.ordemId());
        if(vinculos.isEmpty() || !vinculos.getFirst()) throw new IllegalArgumentException("Conclusão não corresponde ao reparo solicitado");
        var anterior=jdbc.query("SELECT * FROM conclusoes_reparo_recebidas WHERE ordem_id=?", (rs,n)->
            new ReparoConcluidoInput(1,"ReparoConcluido",rs.getObject("event_id",UUID.class),rs.getObject("reparo_id",UUID.class),
                e.ordemId(),rs.getObject("orcamento_id",UUID.class),rs.getTimestamp("ocorrido_em").toInstant()),e.ordemId());
        if(!anterior.isEmpty()) {
            if(anterior.getFirst().equals(e)) return;
            throw new IllegalArgumentException("Conclusão conflitante");
        }
        if("CANCELADA".equals(estados.getFirst())) throw new IllegalArgumentException("OS cancelada não pode ser finalizada");
        if("ENTREGUE".equals(estados.getFirst())) throw new IllegalArgumentException("OS já entregue sem esta conclusão");
        jdbc.update("INSERT INTO conclusoes_reparo_recebidas(event_id,ordem_id,reparo_id,orcamento_id,ocorrido_em) VALUES (?,?,?,?,?)",
            e.eventId(),e.ordemId(),e.reparoId(),e.orcamentoId(),Timestamp.from(e.ocorridoEm()));
        // This is a projection of the authoritative completion in Execution, not a command to execute repairs locally.
        jdbc.update("UPDATE ordem_de_servico_jpa_entity SET status='FINALIZADA',dt_fim_os=? WHERE id=?",
            e.ocorridoEm().atOffset(ZoneOffset.UTC).toLocalDateTime(),e.ordemId());
        HistoricoOrdemJdbcGateway.registrar(jdbc,e.ordemId(),"CONCLUSAO",e.eventId(),"EXECUTION",estados.getFirst(),"FINALIZADA",e.ocorridoEm());
    }
}
