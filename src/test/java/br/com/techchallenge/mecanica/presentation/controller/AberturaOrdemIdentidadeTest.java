package br.com.techchallenge.mecanica.presentation.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import br.com.techchallenge.mecanica.application.dto.ordemdeservico.CriarOrdemDeServicoInput;
import br.com.techchallenge.mecanica.application.usecase.ordemdeservico.*;
import br.com.techchallenge.mecanica.application.usecase.veiculo.BuscarVeiculoPorPlacaUseCase;
import br.com.techchallenge.mecanica.application.usecase.operador.BuscarOperadorPorMatriculaUseCase;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import br.com.techchallenge.mecanica.domain.operador.Operador;
import br.com.techchallenge.mecanica.infrastructure.security.UsuarioAutenticadoService;
import br.com.techchallenge.mecanica.presentation.dto.ordemDeServico.CriarOrdemDeServicoRequest;
import br.com.techchallenge.mecanica.presentation.mapper.OrdemDeServicoPresentationMapper;

class AberturaOrdemIdentidadeTest {
    @AfterEach void limparContexto() { SecurityContextHolder.clearContext(); }

    @ParameterizedTest
    @ValueSource(strings={"CLIENTE","OPERADOR","ADMINISTRADOR"})
    void distingueIdentidadeDoAtendenteDaIdentidadeDoCliente(String role) {
        UUID usuarioId=UUID.randomUUID(), clienteId=UUID.randomUUID(), veiculoId=UUID.randomUUID(), operadorId=UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            usuarioId.toString(), null, List.of(new SimpleGrantedAuthority("ROLE_"+role))));
        var criar=mock(CriarOrdemDeServicoUseCase.class);
        var veiculos=mock(BuscarVeiculoPorPlacaUseCase.class);
        var operadores=mock(BuscarOperadorPorMatriculaUseCase.class);
        var veiculo=mock(Veiculo.class);
        when(veiculo.getId()).thenReturn(veiculoId);
        when(veiculo.getClienteId()).thenReturn(clienteId);
        when(veiculos.executar("ABC1D23")).thenReturn(veiculo);
        var operador=mock(Operador.class);
        when(operador.getId()).thenReturn(operadorId);
        when(operadores.executar(1)).thenReturn(operador);
        var controller=new OrdemDeServicoController(criar,new UsuarioAutenticadoService(),veiculos,operadores,
            mock(BuscarOrdemDeServicoPorIdUseCase.class),mock(ListarOrdensDeServicoUseCase.class),
            mock(EntregarOrdemDeServicoUseCase.class),mock(OrdemDeServicoPresentationMapper.class));
        controller.criar(new CriarOrdemDeServicoRequest("ABC1D23",1));
        var captor=ArgumentCaptor.forClass(CriarOrdemDeServicoInput.class);
        verify(criar).executar(captor.capture());
        assertEquals("CLIENTE".equals(role)?usuarioId:clienteId,captor.getValue().clienteId());
        assertEquals(veiculoId,captor.getValue().veiculoId());
        assertEquals(operadorId,captor.getValue().operadorId());
    }
}
