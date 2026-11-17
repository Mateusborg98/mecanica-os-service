package br.com.techchallenge.mecanica.infrastructure.persistence.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.techchallenge.mecanica.application.gateway.ClienteGateway;
import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.enums.StatusOrdemDeServicoEnum;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import br.com.techchallenge.mecanica.domain.operador.Operador;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.ClienteJpaEntity;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.OperadorJpaEntity;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.OrdemDeServicoJpaEntity;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.VeiculoJpaEntity;
import br.com.techchallenge.mecanica.infrastructure.persistence.mapper.ClienteMapper;
import br.com.techchallenge.mecanica.infrastructure.persistence.mapper.OperadorMapper;
import br.com.techchallenge.mecanica.infrastructure.persistence.mapper.OrdemDeServicoMapper;
import br.com.techchallenge.mecanica.infrastructure.persistence.mapper.VeiculoMapper;
import br.com.techchallenge.mecanica.infrastructure.persistence.repository.ClienteJpaRepository;
import br.com.techchallenge.mecanica.infrastructure.persistence.repository.OperadorJpaRepository;
import br.com.techchallenge.mecanica.infrastructure.persistence.repository.OrdemDeServicoJpaRepository;
import br.com.techchallenge.mecanica.infrastructure.persistence.repository.VeiculoJpaRepository;

class PersistenceGatewayCoverageTest {

    @Test
    void clienteGatewayDeveSalvarBuscarEListar() {
        var repository = mock(ClienteJpaRepository.class);
        var mapper = mock(ClienteMapper.class);
        var domain = mock(Cliente.class);
        var entity = mock(ClienteJpaEntity.class);
        var id = UUID.randomUUID();
        when(mapper.toJpaEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(repository.findByCpfCnpjAndAtivoTrue("doc")).thenReturn(Optional.of(entity));
        when(repository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(entity));
        when(repository.findByAtivoTrue()).thenReturn(List.of(entity));
        var gateway = new ClienteGatewayImpl(repository, mapper);

        assertEquals(domain, gateway.salvar(domain));
        assertEquals(Optional.of(domain), gateway.buscarPorCpfCnpj("doc"));
        assertEquals(Optional.of(domain), gateway.buscarPorId(id));
        assertEquals(List.of(domain), gateway.listar());
    }



    @Test
    void operadorGatewayDeveSalvarBuscarEListar() {
        var repository = mock(OperadorJpaRepository.class);
        var mapper = mock(OperadorMapper.class);
        var domain = mock(Operador.class);
        var entity = mock(OperadorJpaEntity.class);
        var id = UUID.randomUUID();
        when(mapper.toJpaEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(repository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(entity));
        when(repository.findByMatriculaAndAtivoTrue(10)).thenReturn(Optional.of(entity));
        when(repository.findByAtivoTrue()).thenReturn(List.of(entity));
        var gateway = new OperadorGatewayImpl(repository, mapper);

        assertEquals(domain, gateway.salvar(domain));
        assertEquals(Optional.of(domain), gateway.buscarPorId(id));
        assertEquals(Optional.of(domain), gateway.buscarPorMatricula(10));
        assertEquals(List.of(domain), gateway.listar());
    }





    @Test
    void veiculoGatewayDeveSalvarBuscarEListar() {
        var repository = mock(VeiculoJpaRepository.class);
        var mapper = mock(VeiculoMapper.class);
        var clienteGateway = mock(ClienteGateway.class);
        var domain = mock(Veiculo.class);
        var cliente = mock(Cliente.class);
        var entity = mock(VeiculoJpaEntity.class);
        var id = UUID.randomUUID();
        var clienteId = UUID.randomUUID();
        when(domain.getClienteId()).thenReturn(clienteId);
        when(clienteGateway.buscarPorId(clienteId)).thenReturn(Optional.of(cliente));
        when(mapper.toJpaEntity(domain, cliente)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(repository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(entity));
        when(repository.findByPlacaAndAtivoTrue("ABC1D23")).thenReturn(Optional.of(entity));
        when(repository.findByAtivoTrue()).thenReturn(List.of(entity));
        when(repository.findByClienteJpaEntityIdAndAtivoTrue(clienteId)).thenReturn(List.of(entity));
        var gateway = new VeiculoGatewayImpl(repository, mapper, clienteGateway);

        assertEquals(domain, gateway.salvar(domain));
        assertEquals(Optional.of(domain), gateway.buscarPorId(id));
        assertEquals(Optional.of(domain), gateway.buscarPorPlaca("ABC1D23"));
        assertEquals(List.of(domain), gateway.listar());
        assertEquals(List.of(domain), gateway.buscarPorClienteId(clienteId));
    }

    @Test
    void veiculoGatewayDeveFalharAoSalvarSemCliente() {
        var repository = mock(VeiculoJpaRepository.class);
        var mapper = mock(VeiculoMapper.class);
        var clienteGateway = mock(ClienteGateway.class);
        var domain = mock(Veiculo.class);
        var clienteId = UUID.randomUUID();
        when(domain.getClienteId()).thenReturn(clienteId);
        when(clienteGateway.buscarPorId(clienteId)).thenReturn(Optional.empty());

        assertThrows(RegraNegocioException.class,
                () -> new VeiculoGatewayImpl(repository, mapper, clienteGateway).salvar(domain));
    }

    @Test
    void ordemGatewayDeveSalvarBuscarEListar() {
        var repository = mock(OrdemDeServicoJpaRepository.class);
        var mapper = mock(OrdemDeServicoMapper.class);
        var domain = mock(OrdemDeServico.class);
        var entity = mock(OrdemDeServicoJpaEntity.class);
        var id = UUID.randomUUID();
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.findAll()).thenReturn(List.of(entity));
        when(entity.getStatus()).thenReturn(StatusOrdemDeServicoEnum.RECEBIDA);
        when(entity.getDtCriacao()).thenReturn(LocalDateTime.of(2026, 7, 10, 9, 0));
        var gateway = new OrdemDeServicoGatewayImpl(repository, mapper);

        assertEquals(domain, gateway.salvar(domain));
        assertEquals(Optional.of(domain), gateway.buscarPorId(id));
        assertEquals(List.of(domain), gateway.listar());
    }

    @Test
    void ordemGatewayDeveFiltrarEOrdenarOrdensEmAndamento() {
        var repository = mock(OrdemDeServicoJpaRepository.class);
        var mapper = mock(OrdemDeServicoMapper.class);
        var referencia = LocalDateTime.of(2026, 7, 10, 10, 0);

        var recebida = ordemCom(StatusOrdemDeServicoEnum.RECEBIDA, referencia.minusHours(5));
        var diagnostico = ordemCom(StatusOrdemDeServicoEnum.EM_DIAGNOSTICO, referencia.minusHours(4));
        var aguardando = ordemCom(StatusOrdemDeServicoEnum.AGUARDANDO_APROVACAO, referencia.minusHours(3));
        var execucaoNova = ordemCom(StatusOrdemDeServicoEnum.EM_EXECUCAO, referencia.minusHours(1));
        var execucaoAntiga = ordemCom(StatusOrdemDeServicoEnum.EM_EXECUCAO, referencia.minusHours(2));
        var finalizada = ordemCom(StatusOrdemDeServicoEnum.FINALIZADA, referencia.minusHours(8));
        var entregue = ordemCom(StatusOrdemDeServicoEnum.ENTREGUE, referencia.minusHours(9));

        var recebidaDomain = mock(OrdemDeServico.class);
        var diagnosticoDomain = mock(OrdemDeServico.class);
        var aguardandoDomain = mock(OrdemDeServico.class);
        var execucaoNovaDomain = mock(OrdemDeServico.class);
        var execucaoAntigaDomain = mock(OrdemDeServico.class);

        when(repository.findAll()).thenReturn(List.of(
                recebida,
                finalizada,
                execucaoNova,
                diagnostico,
                entregue,
                execucaoAntiga,
                aguardando));
        when(mapper.toDomain(recebida)).thenReturn(recebidaDomain);
        when(mapper.toDomain(diagnostico)).thenReturn(diagnosticoDomain);
        when(mapper.toDomain(aguardando)).thenReturn(aguardandoDomain);
        when(mapper.toDomain(execucaoNova)).thenReturn(execucaoNovaDomain);
        when(mapper.toDomain(execucaoAntiga)).thenReturn(execucaoAntigaDomain);

        var resultado = new OrdemDeServicoGatewayImpl(repository, mapper).listar();

        assertEquals(List.of(
                execucaoAntigaDomain,
                execucaoNovaDomain,
                aguardandoDomain,
                diagnosticoDomain,
                recebidaDomain), resultado);
    }



    private OrdemDeServicoJpaEntity ordemCom(
            StatusOrdemDeServicoEnum status,
            LocalDateTime dtCriacao) {
        return OrdemDeServicoJpaEntity.builder()
                .id(UUID.randomUUID())
                .status(status)
                .dtCriacao(dtCriacao)
                .build();
    }
}
