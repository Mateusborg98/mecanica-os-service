package br.com.techchallenge.mecanica.presentation.mapper;

import org.springframework.stereotype.Component;

import br.com.techchallenge.mecanica.application.dto.cliente.AtualizarClienteInput;
import br.com.techchallenge.mecanica.application.dto.cliente.CriarClienteInput;
import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.presentation.dto.cliente.AtualizarClienteRequest;
import br.com.techchallenge.mecanica.presentation.dto.cliente.ClienteResponse;
import br.com.techchallenge.mecanica.presentation.dto.cliente.ClienteResponseResumo;
import br.com.techchallenge.mecanica.presentation.dto.cliente.CriarClienteRequest;

@Component
public class ClientePresentationMapper {

        public CriarClienteInput toInput(
                        CriarClienteRequest request) {

                return new CriarClienteInput(
                                request.nome(),
                                request.cpfCnpj(),
                                request.contato(),
                                request.email());
        }

        public AtualizarClienteInput toInput(
                        AtualizarClienteRequest request) {

                return new AtualizarClienteInput(
                                request.nome(),
                                request.contato(),
                                request.email());
        }

        public ClienteResponse toResponse(
                        Cliente cliente) {

                return new ClienteResponse(
                                cliente.getId(),
                                cliente.getNome(),
                                cliente.getCpfCnpj().getValor(),
                                cliente.getContato(),
                                cliente.getEmail(),
                                cliente.isAtivo());
        }

        public ClienteResponseResumo toResumo(Cliente cliente) {
                return new ClienteResponseResumo(
                                cliente.getId(),
                                cliente.getNome(),
                                cliente.getContato(),
                                cliente.getEmail());
        }
}