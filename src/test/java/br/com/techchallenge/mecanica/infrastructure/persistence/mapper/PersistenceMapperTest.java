package br.com.techchallenge.mecanica.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.techchallenge.mecanica.infrastructure.persistence.entity.ClienteJpaEntity;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.OperadorJpaEntity;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.VeiculoJpaEntity;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;

class PersistenceMapperTest {

    @Test
    void clienteDevePreservarIdentidadeEEstadoNoRoundTrip() {
        var id = UUID.randomUUID();
        var inativacao = LocalDateTime.of(2026, 7, 6, 10, 0);
        var entity = ClienteJpaEntity.builder()
                .id(id).nome("Cliente").cpfCnpj("52998224725")
                .contato("11999999999").email("cliente@email.com")
                .ativo(false).dataInativacao(inativacao).build();
        var mapper = new ClienteMapper();

        var domain = mapper.toDomain(entity);
        var novamente = mapper.toJpaEntity(domain);

        assertEquals(id, domain.getId());
        assertFalse(domain.isAtivo());
        assertEquals(inativacao, domain.getDataInativacao());
        assertEquals(id, novamente.getId());
        assertFalse(novamente.isAtivo());
    }

    @Test
    void operadorDevePreservarIdCargoEEstadoNoRoundTrip() {
        var id = UUID.randomUUID();
        var entity = OperadorJpaEntity.builder()
                .id(id).nome("Operador").matricula(10)
                .email("operador@email.com").contato("11999999999")
                .cargo("MECANICO").ativo(true).build();
        var mapper = new OperadorMapper();

        var domain = mapper.toDomain(entity);
        var novamente = mapper.toJpaEntity(domain);

        assertEquals(id, domain.getId());
        assertEquals("MECANICO", domain.getCargo());
        assertTrue(domain.isAtivo());
        assertEquals("MECANICO", novamente.getCargo());
        assertTrue(novamente.isAtivo());
    }

    @Test
    void veiculoDevePreservarIdClienteEEstadoNoRoundTrip() {
        var clienteId = UUID.randomUUID();
        var veiculoId = UUID.randomUUID();
        var cliente = ClienteJpaEntity.builder()
                .id(clienteId).nome("Cliente").cpfCnpj("52998224725")
                .contato("11999999999").email("cliente@email.com").ativo(true).build();
        var entity = VeiculoJpaEntity.builder()
                .id(veiculoId).placa("ABC1D23").marca("Honda")
                .modelo("Civic").ano(2020).clienteJpaEntity(cliente)
                .ativo(false).dataInativacao(LocalDateTime.now()).build();
        var mapper = new VeiculoMapper(new ClienteMapper());

        var domain = mapper.toDomain(entity);

        assertEquals(veiculoId, domain.getId());
        assertEquals(clienteId, domain.getClienteId());
        assertFalse(domain.isAtivo());
    }




}
