package br.com.techchallenge.mecanica.application.usecase.ordemdeservico;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import br.com.techchallenge.mecanica.application.gateway.*;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import br.com.techchallenge.mecanica.domain.enums.StatusOrdemDeServicoEnum;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
class EntregaPagamentoTest {
    final OrdemDeServicoGateway ordens=mock(OrdemDeServicoGateway.class);
    final PagamentoOrdemGateway pagamentos=mock(PagamentoOrdemGateway.class);
    final NotificarAlteracaoStatusOrdemUseCase notificacao=mock(NotificarAlteracaoStatusOrdemUseCase.class);
    final EntregarOrdemDeServicoUseCase useCase=new EntregarOrdemDeServicoUseCase(ordens,notificacao,pagamentos);
    final UUID id=UUID.randomUUID();
    OrdemDeServico ordem(StatusOrdemDeServicoEnum status) {
        return new OrdemDeServico(id,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),status,null,null,
                new BigDecimal("51"));
    }
    @Test void pagamentoAusenteNaoAlteraNemNotifica() {
        doThrow(new RegraNegocioException("Não pago")).when(pagamentos).validarParaEntrega(id);
        assertThrows(RegraNegocioException.class,()->useCase.executar(id));
        verifyNoInteractions(ordens,notificacao);
    }
    @Test void pagoMasReparoNaoFinalizadoNaoEntrega() {
        when(ordens.buscarPorId(id)).thenReturn(Optional.of(ordem(StatusOrdemDeServicoEnum.EM_EXECUCAO)));
        assertThrows(RegraNegocioException.class,()->useCase.executar(id));
        verify(ordens,never()).salvar(any()); verifyNoInteractions(notificacao);
    }
    @Test void pagoEFinalizadoEntrega() {
        var ordem=ordem(StatusOrdemDeServicoEnum.FINALIZADA);
        when(ordens.buscarPorId(id)).thenReturn(Optional.of(ordem)); when(ordens.salvar(ordem)).thenReturn(ordem);
        assertEquals(StatusOrdemDeServicoEnum.ENTREGUE,useCase.executar(id).getStatus());
        var sequencia=inOrder(pagamentos,ordens,notificacao);
        sequencia.verify(pagamentos).validarParaEntrega(id); sequencia.verify(ordens).buscarPorId(id);
        sequencia.verify(ordens).salvar(ordem); sequencia.verify(notificacao).executar(ordem);
    }
}
