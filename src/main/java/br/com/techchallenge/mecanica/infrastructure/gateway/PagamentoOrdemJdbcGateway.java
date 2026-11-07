package br.com.techchallenge.mecanica.infrastructure.gateway;
import java.sql.Timestamp;
import java.util.UUID;
import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import br.com.techchallenge.mecanica.application.gateway.PagamentoOrdemGateway;
import br.com.techchallenge.mecanica.application.input.PagamentoConfirmadoInput;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import br.com.techchallenge.mecanica.domain.exception.OrdemDeServicoNaoEncontradaException;
@Repository
@RequiredArgsConstructor
@Transactional
public class PagamentoOrdemJdbcGateway implements PagamentoOrdemGateway {
    private final JdbcTemplate jdbc;
    private BigDecimal bloquearOrdem(UUID id) {
        var valores = jdbc.query("SELECT valor_total_os FROM ordem_de_servico_jpa_entity WHERE id = ? FOR UPDATE",
                (rs, n) -> rs.getBigDecimal(1), id);
        if (valores.isEmpty()) throw new OrdemDeServicoNaoEncontradaException("Ordem de serviço não encontrada");
        return valores.getFirst();
    }
    @Override
    public void registrar(PagamentoConfirmadoInput p) {
        BigDecimal total = bloquearOrdem(p.ordemId());
        var vinculos=jdbc.queryForList("SELECT reparo_id FROM solicitacoes_reparo WHERE ordem_id=?",UUID.class,p.ordemId());
        if(vinculos.isEmpty() || !p.reparoId().equals(vinculos.getFirst()))
            throw new RegraNegocioException("Pagamento não corresponde ao reparo da OS");
        var anteriores = jdbc.query("SELECT * FROM pagamento_ordem WHERE ordem_id = ?", (rs, n) ->
                new PagamentoConfirmadoInput(1, "PagamentoConfirmado", rs.getObject("event_id", UUID.class),
                    rs.getObject("cobranca_id", UUID.class), rs.getObject("ordem_id", UUID.class),
                    rs.getObject("reparo_id", UUID.class), rs.getBigDecimal("valor"), rs.getString("moeda"),
                    rs.getTimestamp("confirmado_em").toInstant()), p.ordemId());
        if (!anteriores.isEmpty()) {
            var a = anteriores.getFirst();
            if (a.eventId().equals(p.eventId()) && a.reparoId().equals(p.reparoId())
                    && a.valor().compareTo(p.valor()) == 0 && a.moeda().equals(p.moeda())
                    && a.confirmadoEm().equals(p.confirmadoEm())) return;
            throw new RegraNegocioException("Pagamento conflitante para a OS");
        }
        var aprovacao=jdbc.queryForList("SELECT decisao,valor FROM progresso_os WHERE ordem_id=? AND decisao IS NOT NULL",p.ordemId());
        if(!aprovacao.isEmpty()) {
            var a=aprovacao.getFirst();
            if(!"APROVADO".equals(a.get("decisao")) || ((BigDecimal)a.get("valor")).compareTo(p.valor())!=0
                || total==null || total.compareTo(p.valor())!=0)
                throw new RegraNegocioException("Pagamento diverge do orçamento aprovado");
        }
        // A confirmed payment may arrive before approval projection. Keep its receipt;
        // delivery still requires all correlated facts and matching amounts below.
        jdbc.update("INSERT INTO pagamento_ordem (event_id,cobranca_id,ordem_id,reparo_id,valor,moeda,confirmado_em) VALUES (?,?,?,?,?,?,?)",
                p.eventId(), p.cobrancaId(), p.ordemId(), p.reparoId(), p.valor(), p.moeda(), Timestamp.from(p.confirmadoEm()));
        var status=jdbc.queryForObject("SELECT status FROM ordem_de_servico_jpa_entity WHERE id=?",String.class,p.ordemId());
        HistoricoOrdemJdbcGateway.registrar(jdbc,p.ordemId(),"PAGAMENTO",p.eventId(),"BILLING",status,status,p.confirmadoEm());
    }
    @Override
    public void validarParaEntrega(UUID id) {
        BigDecimal total = bloquearOrdem(id);
        Integer pagos = jdbc.queryForObject("""
            SELECT COUNT(*) FROM pagamento_ordem p
            JOIN solicitacoes_reparo s ON s.ordem_id=p.ordem_id AND s.reparo_id=p.reparo_id
            JOIN progresso_os g ON g.ordem_id=p.ordem_id AND g.reparo_id=p.reparo_id AND g.orcamento_id=p.cobranca_id
            JOIN conclusoes_reparo_recebidas c ON c.ordem_id=p.ordem_id AND c.reparo_id=p.reparo_id AND c.orcamento_id=p.cobranca_id
            WHERE p.ordem_id=? AND p.valor=? AND p.moeda='BRL' AND g.decisao='APROVADO'
                AND g.valor=p.valor AND g.moeda=p.moeda
            """,
                Integer.class, id, total);
        if (pagos == null || pagos != 1) throw new RegraNegocioException("Entrega exige pagamento confirmado do valor integral da OS");
    }
    @Override public void registrarEntrega(UUID id) {
        HistoricoOrdemJdbcGateway.registrar(jdbc,id,"ENTREGA",id,"OS","FINALIZADA","ENTREGUE",java.time.Instant.now());
    }
}