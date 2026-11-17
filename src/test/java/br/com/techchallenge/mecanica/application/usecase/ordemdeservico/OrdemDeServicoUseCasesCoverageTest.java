package br.com.techchallenge.mecanica.application.usecase.ordemdeservico;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import br.com.techchallenge.mecanica.application.gateway.*;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import br.com.techchallenge.mecanica.domain.exception.OrdemDeServicoNaoEncontradaException;

class OrdemDeServicoUseCasesCoverageTest {
    @Test void consultaAtendimentoEListaOrdens() {
        var gateway=mock(OrdemDeServicoGateway.class); var ordem=mock(OrdemDeServico.class);var id=UUID.randomUUID();
        when(gateway.buscarPorId(id)).thenReturn(Optional.of(ordem));when(gateway.listar()).thenReturn(List.of(ordem));
        assertSame(ordem,new BuscarOrdemDeServicoPorIdUseCase(gateway).executar(id));
        assertEquals(List.of(ordem),new ListarOrdensDeServicoUseCase(gateway).executar());
        assertThrows(OrdemDeServicoNaoEncontradaException.class,()->new BuscarOrdemDeServicoPorIdUseCase(gateway).executar(UUID.randomUUID()));
    }
}
