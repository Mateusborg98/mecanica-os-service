package br.com.techchallenge.mecanica.infrastructure.gateway;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import br.com.techchallenge.mecanica.application.gateway.ProgressoOrdemGateway;
import br.com.techchallenge.mecanica.application.input.ProgressoOrdemInput;
@Repository
@RequiredArgsConstructor
public class ProgressoOrdemJdbcGateway implements ProgressoOrdemGateway {
    private final JdbcTemplate jdbc;
    @Transactional
    public void registrar(ProgressoOrdemInput e) {
        var estados=jdbc.queryForList("SELECT status FROM ordem_de_servico_jpa_entity WHERE id=? FOR UPDATE",String.class,e.ordemId());
        if(estados.isEmpty()) throw new IllegalArgumentException("OS inexistente");
        String atual=estados.getFirst();
        String anterior=atual;
        var vinculos=jdbc.query("SELECT reparo_id,ocorrido_em FROM solicitacoes_reparo WHERE ordem_id=?",(rs,n)->
            rs.getObject(1,UUID.class).equals(e.reparoId()) && !e.ocorridoEm().isBefore(rs.getTimestamp(2).toInstant()),e.ordemId());
        if(vinculos.isEmpty() || !vinculos.getFirst()) throw new IllegalArgumentException("Evento não corresponde à solicitação da OS");
        var anteriores=jdbc.queryForList("SELECT conteudo FROM eventos_progresso_os WHERE tipo=? AND event_id=?",String.class,e.tipo().name(),e.eventId());
        if(!anteriores.isEmpty()) {
            if(anteriores.getFirst().equals(e.toString())) return;
            throw new IllegalArgumentException("Evento repetido com conteúdo diferente");
        }
        jdbc.update("INSERT INTO progresso_os(ordem_id,reparo_id) VALUES (?,?) ON CONFLICT DO NOTHING",e.ordemId(),e.reparoId());
        var estado=jdbc.queryForMap("SELECT * FROM progresso_os WHERE ordem_id=?",e.ordemId());
        if(e.tipo()==ProgressoOrdemInput.Tipo.CANCELAMENTO) {
            if(estado.get("inicio_em")!=null || java.util.Set.of("EM_EXECUCAO","FINALIZADA","ENTREGUE").contains(atual)
                || !jdbc.queryForList("SELECT event_id FROM pagamento_ordem WHERE ordem_id=?",e.ordemId()).isEmpty())
                throw new IllegalArgumentException("Cancelamento incompatível com execução ou pagamento");
            jdbc.update("UPDATE ordem_de_servico_jpa_entity SET status='CANCELADA' WHERE id=?",e.ordemId());
            atual="CANCELADA";
        } else if(e.tipo()==ProgressoOrdemInput.Tipo.DECISAO) {
            if("RECUSADO".equals(e.decisao()) && (estado.get("inicio_em")!=null || java.util.Set.of("EM_EXECUCAO","FINALIZADA","ENTREGUE").contains(atual)))
                throw new IllegalArgumentException("Recusa conflita com execução confirmada");
            if("ENTREGUE".equals(atual)) {
                var total=jdbc.queryForObject("SELECT valor_total_os FROM ordem_de_servico_jpa_entity WHERE id=?",java.math.BigDecimal.class,e.ordemId());
                if(total==null || total.compareTo(e.valor())!=0) throw new IllegalArgumentException("Valor de OS entregue é imutável");
            }
            jdbc.update("UPDATE progresso_os SET orcamento_id=?,decisao=?,valor=?,moeda=?,decisao_em=? WHERE ordem_id=?",
                e.orcamentoId(),e.decisao(),e.valor(),e.moeda(),Timestamp.from(e.ocorridoEm()),e.ordemId());
            jdbc.update("UPDATE ordem_de_servico_jpa_entity SET valor_total_os=? WHERE id=?",e.valor(),e.ordemId());
        } else if(e.tipo()==ProgressoOrdemInput.Tipo.INICIO) {
            if("RECUSADO".equals(estado.get("decisao")) || "CANCELADA".equals(atual)) throw new IllegalArgumentException("Orçamento recusado não pode iniciar");
            var fim=jdbc.query("SELECT dt_fim_os FROM ordem_de_servico_jpa_entity WHERE id=?",(rs,n)->rs.getTimestamp(1),e.ordemId()).getFirst();
            if(fim!=null && e.ocorridoEm().isAfter(fim.toLocalDateTime().toInstant(ZoneOffset.UTC))) throw new IllegalArgumentException("Início posterior à conclusão");
            jdbc.update("UPDATE progresso_os SET inicio_em=? WHERE ordem_id=?",Timestamp.from(e.ocorridoEm()),e.ordemId());
            jdbc.update("UPDATE ordem_de_servico_jpa_entity SET dt_inicio_os=? WHERE id=?",e.ocorridoEm().atOffset(ZoneOffset.UTC).toLocalDateTime(),e.ordemId());
        } else {
            jdbc.update("UPDATE progresso_os SET diagnostico_em=? WHERE ordem_id=?",Timestamp.from(e.ocorridoEm()),e.ordemId());
        }
        var fatos=jdbc.queryForMap("SELECT * FROM progresso_os WHERE ordem_id=?",e.ordemId());
        if(!java.util.Set.of("CANCELADA","FINALIZADA","ENTREGUE").contains(atual)) {
            String novo="RECUSADO".equals(fatos.get("decisao"))?"CANCELADA":
                (fatos.get("inicio_em")!=null || "EM_EXECUCAO".equals(atual))?"EM_EXECUCAO":
                "APROVADO".equals(fatos.get("decisao"))?"NA_FILA":
                fatos.get("diagnostico_em")!=null?"AGUARDANDO_APROVACAO":atual;
            jdbc.update("UPDATE ordem_de_servico_jpa_entity SET status=? WHERE id=?",novo,e.ordemId());
        }
        jdbc.update("INSERT INTO eventos_progresso_os(tipo,event_id,ordem_id,conteudo) VALUES (?,?,?,?)",e.tipo().name(),e.eventId(),e.ordemId(),e.toString());
        var finalStatus=jdbc.queryForObject("SELECT status FROM ordem_de_servico_jpa_entity WHERE id=?",String.class,e.ordemId());
        HistoricoOrdemJdbcGateway.registrar(jdbc,e.ordemId(),e.tipo().name(),e.eventId(),e.tipo()==ProgressoOrdemInput.Tipo.DECISAO?"BILLING":"EXECUTION",anterior,finalStatus,e.ocorridoEm());
    }
}
