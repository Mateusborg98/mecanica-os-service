package br.com.techchallenge.mecanica.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.techchallenge.mecanica.application.dto.cliente.AtualizarClienteInput;
import br.com.techchallenge.mecanica.application.dto.cliente.CriarClienteInput;
import br.com.techchallenge.mecanica.application.dto.operador.AtualizarOperadorInput;
import br.com.techchallenge.mecanica.application.dto.operador.CriarOperadorInput;
import br.com.techchallenge.mecanica.application.dto.veiculo.AtualizarVeiculoInput;
import br.com.techchallenge.mecanica.application.dto.veiculo.CriarVeiculoInput;
import br.com.techchallenge.mecanica.application.gateway.ClienteGateway;
import br.com.techchallenge.mecanica.application.gateway.OperadorGateway;
import br.com.techchallenge.mecanica.application.gateway.VeiculoGateway;
import br.com.techchallenge.mecanica.application.usecase.cliente.AtualizarClienteUseCase;
import br.com.techchallenge.mecanica.application.usecase.cliente.BuscarClientePorCpfCnpjUseCase;
import br.com.techchallenge.mecanica.application.usecase.cliente.BuscarClientePorIdUseCase;
import br.com.techchallenge.mecanica.application.usecase.cliente.CriarClienteUseCase;
import br.com.techchallenge.mecanica.application.usecase.cliente.InativarClienteUseCase;
import br.com.techchallenge.mecanica.application.usecase.cliente.ListarClientesUseCase;
import br.com.techchallenge.mecanica.application.usecase.operador.AtualizarOperadorUseCase;
import br.com.techchallenge.mecanica.application.usecase.operador.BuscarOperadorPorMatriculaUseCase;
import br.com.techchallenge.mecanica.application.usecase.operador.CriarOperadorUseCase;
import br.com.techchallenge.mecanica.application.usecase.operador.InativarOperadorUseCase;
import br.com.techchallenge.mecanica.application.usecase.operador.ListarOperadoresUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.AlterarClienteDoVeiculoUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.AtualizarVeiculoUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.BuscarVeiculoPorIdUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.BuscarVeiculoPorPlacaUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.CriarVeiculoUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.InativarVeiculoUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.ListarVeiculosUseCase;
import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.cliente.valueobject.CpfCnpj;
import br.com.techchallenge.mecanica.domain.exception.ClienteNaoEncontradoException;
import br.com.techchallenge.mecanica.domain.exception.CpfDuplicadoException;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import br.com.techchallenge.mecanica.domain.exception.VeiculoNaoEncontradoException;
import br.com.techchallenge.mecanica.domain.operador.Operador;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import br.com.techchallenge.mecanica.domain.veiculo.valueObject.Placa;

class ApplicationCrudCoverageTest {

    private static final String CPF = "52998224725";

    @Test
    void clienteDeveExecutarFluxosCrudComSucesso() {
        var gateway = mock(ClienteGateway.class);
        when(gateway.buscarPorCpfCnpj(CPF)).thenReturn(Optional.empty());
        when(gateway.salvar(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var criado = new CriarClienteUseCase(gateway).executar(
                new CriarClienteInput("Ana", CPF, "1199", "ana@email.com"));
        assertEquals("Ana", criado.getNome());

        var id = UUID.randomUUID();
        var persistido = new Cliente(id, "Ana", new CpfCnpj(CPF), "1199", "ana@email.com", true, null);
        when(gateway.buscarPorCpfCnpj(CPF)).thenReturn(Optional.of(persistido));
        when(gateway.buscarPorId(id)).thenReturn(Optional.of(persistido));
        when(gateway.listar()).thenReturn(List.of(persistido));

        var atualizado = new AtualizarClienteUseCase(gateway).executar(CPF,
                new AtualizarClienteInput("Ana Maria", "1188", "maria@email.com"));
        assertEquals("Ana Maria", atualizado.getNome());
        assertEquals(persistido, new BuscarClientePorCpfCnpjUseCase(gateway).executar(CPF));
        assertEquals(persistido, new BuscarClientePorIdUseCase(gateway).executar(id));
        assertEquals(List.of(persistido), new ListarClientesUseCase(gateway).executar());

        new InativarClienteUseCase(gateway).executar(CPF);
        assertFalse(persistido.isAtivo());
        verify(gateway, org.mockito.Mockito.times(2)).salvar(persistido);
    }

    @Test
    void clienteDeveCobrirCaminhosDeErro() {
        var gateway = mock(ClienteGateway.class);
        var existente = cliente(UUID.randomUUID());
        when(gateway.buscarPorCpfCnpj(CPF)).thenReturn(Optional.of(existente));
        assertThrows(CpfDuplicadoException.class, () -> new CriarClienteUseCase(gateway).executar(
                new CriarClienteInput("Ana", CPF, "1", "ana@email.com")));

        when(gateway.buscarPorCpfCnpj("ausente")).thenReturn(Optional.empty());
        when(gateway.buscarPorId(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ClienteNaoEncontradoException.class,
                () -> new AtualizarClienteUseCase(gateway).executar("ausente",
                        new AtualizarClienteInput("Ana", "1", "a@b.com")));
        assertThrows(ClienteNaoEncontradoException.class,
                () -> new BuscarClientePorCpfCnpjUseCase(gateway).executar("ausente"));
        assertThrows(ClienteNaoEncontradoException.class,
                () -> new BuscarClientePorIdUseCase(gateway).executar(UUID.randomUUID()));
        assertThrows(ClienteNaoEncontradoException.class,
                () -> new InativarClienteUseCase(gateway).executar("ausente"));
    }

    @Test
    void operadorDeveExecutarFluxosCrudComSucesso() {
        var gateway = mock(OperadorGateway.class);
        when(gateway.salvar(any(Operador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var criado = new CriarOperadorUseCase(gateway).executar(
                new CriarOperadorInput("Bia", 10, "bia@email.com", "1199", "MECANICO"));
        assertEquals(10, criado.getMatricula());

        var persistido = operador(UUID.randomUUID());
        when(gateway.buscarPorMatricula(10)).thenReturn(Optional.of(persistido));
        when(gateway.listar()).thenReturn(List.of(persistido));

        var atualizado = new AtualizarOperadorUseCase(gateway).executar(10,
                new AtualizarOperadorInput("Beatriz", "beatriz@email.com", "1188", "CHEFE"));
        assertEquals("CHEFE", atualizado.getCargo());
        assertEquals(persistido, new BuscarOperadorPorMatriculaUseCase(gateway).executar(10));
        assertEquals(List.of(persistido), new ListarOperadoresUseCase(gateway).executar());

        new InativarOperadorUseCase(gateway).executar(10);
        assertFalse(persistido.isAtivo());
    }

    @Test
    void operadorDeveCobrirCaminhosDeErro() {
        var gateway = mock(OperadorGateway.class);
        when(gateway.buscarPorMatricula(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> new AtualizarOperadorUseCase(gateway).executar(99,
                        new AtualizarOperadorInput("N", "n@email.com", "1", "C")));
        assertThrows(RuntimeException.class,
                () -> new BuscarOperadorPorMatriculaUseCase(gateway).executar(99));
        assertThrows(RuntimeException.class,
                () -> new InativarOperadorUseCase(gateway).executar(99));
    }









    @Test
    void veiculoDeveExecutarFluxosCrudComSucesso() {
        var veiculoGateway = mock(VeiculoGateway.class);
        var clienteGateway = mock(ClienteGateway.class);
        var cliente = cliente(UUID.randomUUID());
        when(clienteGateway.buscarPorCpfCnpj(CPF)).thenReturn(Optional.of(cliente));
        when(veiculoGateway.buscarPorPlaca("ABC1D23")).thenReturn(Optional.empty());
        when(veiculoGateway.salvar(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var criado = new CriarVeiculoUseCase(veiculoGateway, clienteGateway).executar(
                new CriarVeiculoInput("ABC1D23", "Honda", "Civic", 2020, CPF));
        assertEquals(cliente.getId(), criado.getClienteId());

        var id = UUID.randomUUID();
        var persistido = veiculo(id, cliente.getId());
        when(veiculoGateway.buscarPorId(id)).thenReturn(Optional.of(persistido));
        when(veiculoGateway.buscarPorPlaca("DEF1234")).thenReturn(Optional.of(persistido));
        when(veiculoGateway.listar()).thenReturn(List.of(persistido));

        var atualizado = new AtualizarVeiculoUseCase(veiculoGateway).executar(id,
                new AtualizarVeiculoInput("DEF1234", "Toyota", "Corolla", 2021));
        assertEquals("Toyota", atualizado.getMarca());
        assertEquals(persistido, new BuscarVeiculoPorIdUseCase(veiculoGateway).executar(id));
        assertEquals(persistido, new BuscarVeiculoPorPlacaUseCase(veiculoGateway).executar("DEF1234"));
        assertEquals(List.of(persistido), new ListarVeiculosUseCase(veiculoGateway).executar());

        var novoCliente = cliente(UUID.randomUUID());
        when(clienteGateway.buscarPorId(novoCliente.getId())).thenReturn(Optional.of(novoCliente));
        new AlterarClienteDoVeiculoUseCase(veiculoGateway, clienteGateway)
                .executar(id, novoCliente.getId());
        assertEquals(novoCliente.getId(), persistido.getClienteId());

        new InativarVeiculoUseCase(veiculoGateway).executar(id);
        assertFalse(persistido.isAtivo());
    }

    @Test
    void veiculoDeveCobrirCaminhosDeErro() {
        var veiculoGateway = mock(VeiculoGateway.class);
        var clienteGateway = mock(ClienteGateway.class);
        when(clienteGateway.buscarPorCpfCnpj(CPF)).thenReturn(Optional.empty());
        assertThrows(ClienteNaoEncontradoException.class,
                () -> new CriarVeiculoUseCase(veiculoGateway, clienteGateway).executar(
                        new CriarVeiculoInput("ABC1D23", "M", "X", 2020, CPF)));

        var cliente = cliente(UUID.randomUUID());
        var existente = veiculo(UUID.randomUUID(), cliente.getId());
        when(clienteGateway.buscarPorCpfCnpj(CPF)).thenReturn(Optional.of(cliente));
        when(veiculoGateway.buscarPorPlaca("ABC1D23")).thenReturn(Optional.of(existente));
        assertThrows(RegraNegocioException.class,
                () -> new CriarVeiculoUseCase(veiculoGateway, clienteGateway).executar(
                        new CriarVeiculoInput("ABC1D23", "M", "X", 2020, CPF)));

        var ausente = UUID.randomUUID();
        when(veiculoGateway.buscarPorId(ausente)).thenReturn(Optional.empty());
        when(veiculoGateway.buscarPorPlaca("AUSENTE")).thenReturn(Optional.empty());
        assertThrows(RegraNegocioException.class,
                () -> new AtualizarVeiculoUseCase(veiculoGateway).executar(ausente,
                        new AtualizarVeiculoInput("DEF1234", "M", "X", 2020)));
        assertThrows(VeiculoNaoEncontradoException.class,
                () -> new BuscarVeiculoPorIdUseCase(veiculoGateway).executar(ausente));
        assertThrows(RegraNegocioException.class,
                () -> new BuscarVeiculoPorPlacaUseCase(veiculoGateway).executar("AUSENTE"));
        assertThrows(RegraNegocioException.class,
                () -> new InativarVeiculoUseCase(veiculoGateway).executar(ausente));
        assertThrows(VeiculoNaoEncontradoException.class,
                () -> new AlterarClienteDoVeiculoUseCase(veiculoGateway, clienteGateway)
                        .executar(ausente, cliente.getId()));

        var id = existente.getId();
        when(veiculoGateway.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(clienteGateway.buscarPorId(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ClienteNaoEncontradoException.class,
                () -> new AlterarClienteDoVeiculoUseCase(veiculoGateway, clienteGateway)
                        .executar(id, UUID.randomUUID()));
    }

    private Cliente cliente(UUID id) {
        return new Cliente(id, "Cliente", new CpfCnpj(CPF), "1199", "cliente@email.com", true, null);
    }

    private Operador operador(UUID id) {
        return new Operador(id, "Operador", 10, "operador@email.com", "1199", "MECANICO", true, null);
    }

    private Veiculo veiculo(UUID id, UUID clienteId) {
        return new Veiculo(id, new Placa("ABC1D23"), "Honda", "Civic", 2020, clienteId);
    }
}
