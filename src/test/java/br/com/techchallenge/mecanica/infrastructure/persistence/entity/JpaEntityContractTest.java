package br.com.techchallenge.mecanica.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class JpaEntityContractTest {

    @Test
    void clienteDeveCompararPorId() {
        var id = UUID.randomUUID();
        validarContrato(ClienteJpaEntity.builder().id(id).build(),
                ClienteJpaEntity.builder().id(id).build(),
                ClienteJpaEntity.builder().build());
    }



    @Test
    void operadorDeveCompararPorId() {
        var id = UUID.randomUUID();
        validarContrato(OperadorJpaEntity.builder().id(id).build(),
                OperadorJpaEntity.builder().id(id).build(),
                OperadorJpaEntity.builder().build());
    }

    @Test
    void ordemDeveCompararPorId() {
        var id = UUID.randomUUID();
        validarContrato(OrdemDeServicoJpaEntity.builder().id(id).build(),
                OrdemDeServicoJpaEntity.builder().id(id).build(),
                OrdemDeServicoJpaEntity.builder().build());
    }









    @Test
    void veiculoDeveCompararPorId() {
        var id = UUID.randomUUID();
        validarContrato(VeiculoJpaEntity.builder().id(id).build(),
                VeiculoJpaEntity.builder().id(id).build(),
                VeiculoJpaEntity.builder().build());
    }

    private void validarContrato(Object original, Object mesmoId, Object semId) {
        assertEquals(original, original);
        assertEquals(original, mesmoId);
        assertEquals(original.hashCode(), mesmoId.hashCode());
        assertNotEquals(original, semId);
        assertNotEquals(original, new Object());
    }
}
