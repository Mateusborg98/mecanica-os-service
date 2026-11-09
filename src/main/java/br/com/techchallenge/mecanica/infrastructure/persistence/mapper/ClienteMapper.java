package br.com.techchallenge.mecanica.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.cliente.valueobject.CpfCnpj;
import br.com.techchallenge.mecanica.domain.exception.CpfInvalidoException;
import br.com.techchallenge.mecanica.infrastructure.persistence.entity.ClienteJpaEntity;

@Component
public class ClienteMapper {

    public ClienteJpaEntity toJpaEntity(Cliente cliente) {

        return ClienteJpaEntity.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .cpfCnpj(cliente.getCpfCnpj().getValor())
                .contato(cliente.getContato())
                .email(cliente.getEmail())
                .ativo(cliente.isAtivo())
                .dataInativacao(cliente.getDataInativacao())
                .build();
    }

    public Cliente toDomain(ClienteJpaEntity clienteJpaEntity) throws CpfInvalidoException {

        return new Cliente(
                clienteJpaEntity.getId(),
                clienteJpaEntity.getNome(),
                new CpfCnpj(clienteJpaEntity.getCpfCnpj()),
                clienteJpaEntity.getContato(),
                clienteJpaEntity.getEmail(),
                clienteJpaEntity.isAtivo(),
                clienteJpaEntity.getDataInativacao());
    }
}
