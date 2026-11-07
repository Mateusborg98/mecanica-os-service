package br.com.techchallenge.mecanica.domain.ordemdeservico;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import br.com.techchallenge.mecanica.domain.enums.StatusOrdemDeServicoEnum;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;

class OrdemDeServicoTest {
    @Test void abreAtendimentoSemCalcularOrcamento() {
        var ordem=new OrdemDeServico(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID());
        assertEquals(StatusOrdemDeServicoEnum.RECEBIDA,ordem.getStatus()); assertEquals(BigDecimal.ZERO,ordem.getValorTotalOs());
        assertNull(ordem.getDtInicioOs()); assertNull(ordem.getDtFimOs());
    }
    @Test void exigeIdentificadoresObrigatorios() {
        assertThrows(RegraNegocioException.class,()->new OrdemDeServico(null,UUID.randomUUID(),UUID.randomUUID()));
        assertThrows(RegraNegocioException.class,()->new OrdemDeServico(UUID.randomUUID(),null,UUID.randomUUID()));
        assertThrows(RegraNegocioException.class,()->new OrdemDeServico(UUID.randomUUID(),UUID.randomUUID(),null));
    }
    @Test void entregaConclusaoProjetadaSemModificarValorOuDatas() {
        var now=LocalDateTime.now(); var id=UUID.randomUUID();
        var ordem=new OrdemDeServico(id,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),StatusOrdemDeServicoEnum.FINALIZADA,now.minusHours(1),now,BigDecimal.TEN);
        ordem.entregar(); ordem.entregar();
        assertEquals(StatusOrdemDeServicoEnum.ENTREGUE,ordem.getStatus()); assertEquals(BigDecimal.TEN,ordem.getValorTotalOs());
        assertEquals(now,ordem.getDtFimOs()); assertEquals(now.minusHours(1),ordem.getDtInicioOs());
        assertEquals(ordem,ordem); assertNotEquals(ordem,new Object());
        assertEquals(ordem,new OrdemDeServico(id,ordem.getClienteId(),ordem.getVeiculoId(),ordem.getOperadorId(),ordem.getStatus(),now,now,BigDecimal.TEN));
        assertEquals(ordem.getClass().hashCode(),ordem.hashCode());
    }
    @Test void naoEntregaAntesDaConclusao() {
        var ordem=new OrdemDeServico(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID());
        assertThrows(RegraNegocioException.class,ordem::entregar);
    }
}
