package br.com.techchallenge.mecanica.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.techchallenge.mecanica.application.usecase.cliente.AtualizarClienteUseCase;
import br.com.techchallenge.mecanica.application.usecase.cliente.BuscarClientePorCpfCnpjUseCase;
import br.com.techchallenge.mecanica.application.usecase.cliente.CriarClienteUseCase;
import br.com.techchallenge.mecanica.application.usecase.cliente.InativarClienteUseCase;
import br.com.techchallenge.mecanica.application.usecase.cliente.ListarClientesUseCase;
import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.presentation.dto.cliente.AtualizarClienteRequest;
import br.com.techchallenge.mecanica.presentation.dto.cliente.ClienteResponse;
import br.com.techchallenge.mecanica.presentation.dto.cliente.CriarClienteRequest;
import br.com.techchallenge.mecanica.presentation.mapper.ClientePresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Clientes", description = "Cadastro, consulta, atualização e inativação lógica de clientes.")
@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final CriarClienteUseCase criarClienteUseCase;
    private final AtualizarClienteUseCase atualizarClienteUseCase;
    private final ListarClientesUseCase listarClientesUseCase;
    private final InativarClienteUseCase inativarClienteUseCase;
    private final BuscarClientePorCpfCnpjUseCase buscarPorCpfCnpjUseCase;

    private final ClientePresentationMapper mapper;

    @Operation(
            summary = "Cadastrar cliente",
            description = "Cadastra um cliente utilizando CPF ou CNPJ como documento identificador.")
    @PostMapping
    public ResponseEntity<ClienteResponse> criar(
            @RequestBody CriarClienteRequest request) {

        Cliente cliente = criarClienteUseCase.executar(
                mapper.toInput(request));

        return ResponseEntity.ok(
                mapper.toResponse(cliente));
    }

    /**
     * ATENÇÃO:
     * Aqui o identificador do seu sistema é CPF/CNPJ, não ID
     */
    @Operation(
            summary = "Atualizar cliente",
            description = "Atualiza os dados cadastrais de um cliente identificado pelo CPF ou CNPJ.")
    @PutMapping("/documento/{cpfCnpj}")
    public ResponseEntity<ClienteResponse> atualizar(
            @PathVariable String cpfCnpj,
            @RequestBody AtualizarClienteRequest request) {

        Cliente cliente = atualizarClienteUseCase.executar(
                cpfCnpj,
                mapper.toInput(request));

        return ResponseEntity.ok(
                mapper.toResponse(cliente));
    }

    @Operation(
            summary = "Listar clientes ativos",
            description = "Retorna todos os clientes que não foram inativados.")
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {

        List<ClienteResponse> response = listarClientesUseCase.executar()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Buscar cliente por documento",
            description = "Consulta um cliente ativo pelo CPF ou CNPJ.")
    @GetMapping("/documento/{cpfCnpj}")
    public ResponseEntity<ClienteResponse> buscarPorCpfCnpj(
            @PathVariable String cpfCnpj) {

        Cliente cliente = buscarPorCpfCnpjUseCase.executar(cpfCnpj);

        return ResponseEntity.ok(
                mapper.toResponse(cliente));
    }

    @Operation(
            summary = "Inativar cliente",
            description = "Realiza a exclusão lógica do cliente identificado pelo CPF ou CNPJ.")
    @PatchMapping("/documento/{cpfCnpj}/inativar")
    public ResponseEntity<Void> inativar(
            @PathVariable String cpfCnpj) {

        inativarClienteUseCase.executar(cpfCnpj);

        return ResponseEntity.noContent().build();
    }
}
