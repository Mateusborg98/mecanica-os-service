package br.com.techchallenge.mecanica.application.usecase.ordemdeservico;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import br.com.techchallenge.mecanica.application.gateway.*;
import br.com.techchallenge.mecanica.application.dto.ordemdeservico.CriarOrdemDeServicoInput;
import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import br.com.techchallenge.mecanica.domain.operador.Operador;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import br.com.techchallenge.mecanica.domain.enums.StatusOrdemDeServicoEnum;
import br.com.techchallenge.mecanica.domain.exception.*;

class CriarOrdemDeServicoUseCaseTest {
    final OrdemDeServicoGateway ordens = mock(OrdemDeServicoGateway.class);
    final ClienteGateway clientes = mock(ClienteGateway.class);
    final VeiculoGateway veiculos = mock(VeiculoGateway.class);
    final OperadorGateway operadores = mock(OperadorGateway.class);
    final NotificarAlteracaoStatusOrdemUseCase notificacao = mock(NotificarAlteracaoStatusOrdemUseCase.class);
    final SolicitarReparoGateway reparos = mock(SolicitarReparoGateway.class);
    final CriarOrdemDeServicoUseCase useCase = new CriarOrdemDeServicoUseCase(ordens,clientes,veiculos,operadores,notificacao,reparos);
    final CriarOrdemDeServicoInput input = new CriarOrdemDeServicoInput(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID());

    @Test void aberturaSomenteCriaAtendimentoESolicitaReparo() {
        validarCadastros();
        var persistida = new OrdemDeServico(UUID.randomUUID(),input.clienteId(),input.veiculoId(),input.operadorId(),StatusOrdemDeServicoEnum.RECEBIDA,null,null,java.math.BigDecimal.ZERO);
        when(ordens.salvar(any())).thenReturn(persistida);
        assertSame(persistida,useCase.executar(input));
        var sequence=inOrder(ordens,reparos,notificacao);
        sequence.verify(ordens).salvar(argThat(o -> o.getStatus()==StatusOrdemDeServicoEnum.RECEBIDA && o.getValorTotalOs().signum()==0));
        sequence.verify(reparos).solicitar(persistida.getId()); sequence.verify(notificacao).executar(persistida);
    }
    @Test void clienteAusenteNaoAbreAtendimento() {
        assertThrows(ClienteNaoEncontradoException.class,()->useCase.executar(input)); verifyNoInteractions(ordens,reparos);
    }
    @Test void veiculoAusenteNaoAbreAtendimento() {
        when(clientes.buscarPorId(input.clienteId())).thenReturn(Optional.of(mock(Cliente.class)));
        assertThrows(VeiculoNaoEncontradoException.class,()->useCase.executar(input)); verifyNoInteractions(ordens,reparos);
    }
    @Test void operadorAusenteNaoAbreAtendimento() {
        when(clientes.buscarPorId(input.clienteId())).thenReturn(Optional.of(mock(Cliente.class)));
        var veiculo = mock(Veiculo.class);
        when(veiculo.getClienteId()).thenReturn(input.clienteId());
        when(veiculos.buscarPorId(input.veiculoId())).thenReturn(Optional.of(veiculo));
        assertThrows(OperadorNaoEncontradoException.class,()->useCase.executar(input)); verifyNoInteractions(ordens,reparos);
    }
    @Test
    void veiculoDeOutroClienteNaoAbreAtendimentoNemProduzEfeitos() {
        when(clientes.buscarPorId(input.clienteId())).thenReturn(Optional.of(mock(Cliente.class)));
        var veiculo = mock(Veiculo.class);
        when(veiculo.getClienteId()).thenReturn(UUID.randomUUID());
        when(veiculos.buscarPorId(input.veiculoId())).thenReturn(Optional.of(veiculo));

        assertThrows(RegraNegocioException.class, () -> useCase.executar(input));

        verifyNoInteractions(operadores, ordens, reparos, notificacao);
    }
    private void validarCadastros() {
        when(clientes.buscarPorId(input.clienteId())).thenReturn(Optional.of(mock(Cliente.class)));
        var veiculo = mock(Veiculo.class);
        when(veiculo.getClienteId()).thenReturn(input.clienteId());
        when(veiculos.buscarPorId(input.veiculoId())).thenReturn(Optional.of(veiculo));
        when(operadores.buscarPorId(input.operadorId())).thenReturn(Optional.of(mock(Operador.class)));
    }
}
