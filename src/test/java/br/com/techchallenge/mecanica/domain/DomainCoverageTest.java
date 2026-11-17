package br.com.techchallenge.mecanica.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.cliente.valueobject.CpfCnpj;
import br.com.techchallenge.mecanica.domain.enums.StatusOrdemDeServicoEnum;
import br.com.techchallenge.mecanica.domain.exception.CpfInvalidoException;
import br.com.techchallenge.mecanica.domain.exception.PlacaInvalidaException;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import br.com.techchallenge.mecanica.domain.operador.Operador;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import br.com.techchallenge.mecanica.domain.veiculo.valueObject.Placa;

class DomainCoverageTest {

    private static final String CPF = "52998224725";

    @Test
    void clienteDeveCriarAtualizarInativarECompararPorId() {
        var id = UUID.randomUUID();
        var cpf = new CpfCnpj(CPF);
        var cliente = new Cliente(id, "Ana", cpf, "11999999999", "ana@email.com", true, null);

        assertEquals(id, cliente.getId());
        assertEquals(cpf, cliente.getCpfCnpj());
        assertTrue(cliente.isAtivo());
        assertNull(cliente.getDataInativacao());

        cliente.atualizarDados("Ana Maria", "11888888888", "ana.maria@email.com");
        assertEquals("Ana Maria", cliente.getNome());
        assertEquals("11888888888", cliente.getContato());
        assertEquals("ana.maria@email.com", cliente.getEmail());

        cliente.inativar();
        assertFalse(cliente.isAtivo());
        assertNotNull(cliente.getDataInativacao());
        assertThrows(RegraNegocioException.class, cliente::inativar);

        var mesmo = new Cliente(id, "Outra", cpf, "1", "outra@email.com", true, null);
        assertEquals(cliente, cliente);
        assertEquals(cliente, mesmo);
        assertNotEquals(cliente, new Cliente("Sem id", cpf, "1", "sem@email.com"));
        assertNotEquals(cliente, "cliente");
        assertEquals(cliente.hashCode(), mesmo.hashCode());
    }

    @Test
    void clienteDeveValidarNomeEEmailEmTodosOsFluxos() {
        var cpf = new CpfCnpj(CPF);
        assertThrows(RegraNegocioException.class, () -> new Cliente(" ", cpf, "1", "a@b.com"));
        assertThrows(RegraNegocioException.class, () -> new Cliente("Ana", cpf, "1", null));

        var cliente = new Cliente("Ana", cpf, "1", "a@b.com");
        assertThrows(RegraNegocioException.class, () -> cliente.atualizarDados(null, "2", "a@b.com"));
        assertThrows(RegraNegocioException.class, () -> cliente.atualizarDados("Ana", "2", "invalido"));
    }

    @Test
    void operadorDeveCobrirCicloDeVidaValidacoesEIgualdade() {
        var id = UUID.randomUUID();
        var operador = new Operador(id, "Bia", 10, "bia@email.com", "1199", "MECANICO", true, null);
        operador.atualizarDados("Beatriz", "beatriz@email.com", "1188", "CHEFE");

        assertEquals("Beatriz", operador.getNome());
        assertEquals(10, operador.getMatricula());
        assertEquals("CHEFE", operador.getCargo());
        assertTrue(operador.isAtivo());

        operador.inativar();
        assertFalse(operador.isAtivo());
        assertNotNull(operador.getDataInativacao());
        assertThrows(RegraNegocioException.class, operador::inativar);

        var mesmo = new Operador(id, "Outro", 11, "outro@email.com", "1", "CARGO", true, null);
        assertEquals(operador, operador);
        assertEquals(operador, mesmo);
        assertNotEquals(operador, new Operador("Sem id", 12, "sem@email.com", "1", "CARGO"));
        assertNotEquals(operador, new Object());
        assertEquals(operador.hashCode(), mesmo.hashCode());

        assertThrows(IllegalArgumentException.class,
                () -> new Operador("", 1, "a@b.com", "1", "CARGO"));
        assertThrows(IllegalArgumentException.class,
                () -> new Operador("Nome", 1, "email", "1", "CARGO"));
        assertThrows(IllegalArgumentException.class,
                () -> operador.atualizarDados(null, "a@b.com", "1", "CARGO"));
        assertThrows(IllegalArgumentException.class,
                () -> operador.atualizarDados("Nome", null, "1", "CARGO"));
    }





    @Test
    void veiculoDeveCobrirCicloDeVidaValidacoesEIgualdade() {
        var id = UUID.randomUUID();
        var clienteId = UUID.randomUUID();
        var veiculo = new Veiculo(id, new Placa("ABC1D23"), "Honda", "Civic", 2020,
                clienteId, true, null);

        veiculo.atualizarDados(new Placa("DEF-1234"), "Toyota", "Corolla", 2021);
        var novoCliente = UUID.randomUUID();
        veiculo.alterarCliente(novoCliente);

        assertEquals("DEF1234", veiculo.getPlaca().getValor());
        assertEquals("Toyota", veiculo.getMarca());
        assertEquals("Corolla", veiculo.getModelo());
        assertEquals(2021, veiculo.getAno());
        assertEquals(novoCliente, veiculo.getClienteId());

        veiculo.inativar();
        assertFalse(veiculo.isAtivo());
        assertNotNull(veiculo.getDataInativacao());
        assertThrows(RegraNegocioException.class, veiculo::inativar);

        var mesmo = new Veiculo(id, new Placa("GHI1J23"), "M", "X", 2020, clienteId);
        assertEquals(veiculo, veiculo);
        assertEquals(veiculo, mesmo);
        assertNotEquals(veiculo, new Veiculo(new Placa("JKL1M23"), "M", "X", 2020, clienteId));
        assertNotEquals(veiculo, "veiculo");
        assertEquals(veiculo.hashCode(), mesmo.hashCode());

        assertThrows(RegraNegocioException.class,
                () -> new Veiculo(new Placa("ABC1D23"), "", "X", 2020, clienteId));
        assertThrows(RegraNegocioException.class,
                () -> new Veiculo(new Placa("ABC1D23"), "M", null, 2020, clienteId));
        assertThrows(RegraNegocioException.class,
                () -> new Veiculo(new Placa("ABC1D23"), "M", "X", 1899, clienteId));
        assertThrows(RegraNegocioException.class,
                () -> new Veiculo(new Placa("ABC1D23"), "M", "X", LocalDate.now().getYear() + 2, clienteId));
        assertThrows(RegraNegocioException.class,
                () -> new Veiculo(new Placa("ABC1D23"), "M", "X", null, clienteId));
        assertThrows(RegraNegocioException.class,
                () -> new Veiculo(new Placa("ABC1D23"), "M", "X", 2020, null));
        assertThrows(RegraNegocioException.class, () -> veiculo.alterarCliente(null));
        assertThrows(RegraNegocioException.class,
                () -> veiculo.atualizarDados(new Placa("ABC1D23"), null, "X", 2020));
    }

    @Test
    void cpfCnpjDeveNormalizarValidarEImplementarContratoDeValor() {
        var formatado = new CpfCnpj("529.982.247-25");
        var igual = new CpfCnpj(CPF);
        var cnpj = new CpfCnpj("11.222.333/0001-81");

        assertEquals(CPF, formatado.getValor());
        assertEquals(CPF, formatado.toString());
        assertEquals(formatado, formatado);
        assertEquals(formatado, igual);
        assertEquals(formatado.hashCode(), igual.hashCode());
        assertEquals("11222333000181", cnpj.getValor());
        assertNotEquals(formatado, cnpj);
        assertNotEquals(formatado, "cpf");

        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj(null));
        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj("123"));
        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj("11111111111"));
        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj("52998224724"));
        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj("52998224715"));
        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj("11111111111111"));
        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj("11222333000180"));
    }

    @Test
    void placaDeveNormalizarValidarEImplementarContratoDeValor() {
        var placa = new Placa(" abc-1234 ");
        var igual = new Placa("ABC1234");
        var mercosul = new Placa("abc1d23");

        assertEquals("ABC1234", placa.getValor());
        assertEquals("ABC1234", placa.toString());
        assertEquals(placa, placa);
        assertEquals(placa, igual);
        assertEquals(placa.hashCode(), igual.hashCode());
        assertNotEquals(placa, mercosul);
        assertNotEquals(placa, "placa");

        assertThrows(PlacaInvalidaException.class, () -> new Placa(null));
        assertThrows(PlacaInvalidaException.class, () -> new Placa("INVALIDA"));
    }








}
