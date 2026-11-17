package br.com.techchallenge.mecanica.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.techchallenge.mecanica.application.dto.veiculo.AtualizarVeiculoInput;
import br.com.techchallenge.mecanica.application.dto.veiculo.CriarVeiculoInput;
import br.com.techchallenge.mecanica.application.usecase.cliente.BuscarClientePorIdUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.AlterarClienteDoVeiculoUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.AtualizarVeiculoUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.BuscarVeiculoPorIdUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.BuscarVeiculoPorPlacaUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.CriarVeiculoUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.InativarVeiculoUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.ListarVeiculosUseCase;
import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import br.com.techchallenge.mecanica.presentation.dto.veiculo.AtualizarClienteDoVeiculoRequest;
import br.com.techchallenge.mecanica.presentation.dto.veiculo.AtualizarVeiculoRequest;
import br.com.techchallenge.mecanica.presentation.dto.veiculo.CriarVeiculoRequest;
import br.com.techchallenge.mecanica.presentation.dto.veiculo.VeiculoResponse;
import br.com.techchallenge.mecanica.presentation.mapper.VeiculoPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/veiculos")
@Tag(name = "Veículos", description = "Cadastro, consulta, atualização e inativação lógica de veículos.")
@RequiredArgsConstructor
public class VeiculoController {

    private final AtualizarVeiculoUseCase atualizarVeiculoUseCase;
    private final AlterarClienteDoVeiculoUseCase alterarClienteDoVeiculoUseCase;
    private final CriarVeiculoUseCase criarVeiculoUseCase;
    private final BuscarVeiculoPorIdUseCase buscarVeiculoPorIdUseCase;
    private final BuscarVeiculoPorPlacaUseCase buscarVeiculoPorPlacaUseCase;
    private final ListarVeiculosUseCase listarVeiculosUseCase;
    private final InativarVeiculoUseCase inativarVeiculoUseCase;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;

    private final VeiculoPresentationMapper veiculoPresentationMapper;

    @Operation(
            summary = "Atualizar veículo",
            description = "Atualiza placa, marca, modelo e ano do veículo identificado pelo ID.")
    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponse> atualizar(
            @PathVariable UUID id,
            @RequestBody AtualizarVeiculoRequest request) {

        AtualizarVeiculoInput input = veiculoPresentationMapper.toAtualizarVeiculoInput(request);

        Veiculo veiculo = atualizarVeiculoUseCase.executar(
                id,
                input);

        Cliente cliente = buscarClientePorIdUseCase.executar(veiculo.getClienteId());

        return ResponseEntity.ok(
                veiculoPresentationMapper.toResponse(veiculo, cliente));
    }

    @Operation(
            summary = "Alterar proprietário do veículo",
            description = "Associa o veículo a outro cliente ativo.")
    @PutMapping("/{id}/cliente")
    public ResponseEntity<VeiculoResponse> alterarCliente(
            @PathVariable UUID id,
            @RequestBody AtualizarClienteDoVeiculoRequest request) {

        Veiculo veiculo = alterarClienteDoVeiculoUseCase.executar(
                id,
                request.clienteId());

        Cliente cliente = buscarClientePorIdUseCase.executar(veiculo.getClienteId());

        return ResponseEntity.ok(
                veiculoPresentationMapper.toResponse(veiculo, cliente));
    }

    @Operation(
            summary = "Cadastrar veículo",
            description = "Cadastra um veículo e o associa ao cliente identificado pelo CPF ou CNPJ.")
    @PostMapping
    public ResponseEntity<VeiculoResponse> criar(
            @RequestBody CriarVeiculoRequest request) {

        CriarVeiculoInput input = veiculoPresentationMapper.toCriarVeiculoInput(request);

        Veiculo veiculo = criarVeiculoUseCase.executar(input);
        Cliente cliente = buscarClientePorIdUseCase.executar(veiculo.getClienteId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(veiculoPresentationMapper.toResponse(veiculo, cliente));
    }

    @Operation(
            summary = "Buscar veículo por ID",
            description = "Consulta um veículo ativo utilizando seu identificador UUID.")
    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponse> buscarPorId(@PathVariable UUID id) {

        Veiculo veiculo = buscarVeiculoPorIdUseCase.executar(id);
        Cliente cliente = buscarClientePorIdUseCase.executar(veiculo.getClienteId());

        return ResponseEntity.ok(
                veiculoPresentationMapper.toResponse(veiculo, cliente));
    }

    @Operation(
            summary = "Buscar veículo por placa",
            description = "Consulta um veículo ativo utilizando a placa.")
    @GetMapping("/placa/{placa}")
    public ResponseEntity<VeiculoResponse> buscarPorPlaca(@PathVariable String placa) {

        Veiculo veiculo = buscarVeiculoPorPlacaUseCase.executar(placa);
        Cliente cliente = buscarClientePorIdUseCase.executar(veiculo.getClienteId());

        return ResponseEntity.ok(
                veiculoPresentationMapper.toResponse(veiculo, cliente));
    }

    @Operation(
            summary = "Listar veículos ativos",
            description = "Retorna todos os veículos que não foram inativados.")
    @GetMapping
    public ResponseEntity<List<VeiculoResponse>> listar() {

        List<Veiculo> veiculos = listarVeiculosUseCase.executar();

        List<VeiculoResponse> responses = veiculos.stream()
                .map(veiculo -> {
                    Cliente cliente = buscarClientePorIdUseCase.executar(veiculo.getClienteId());
                    return veiculoPresentationMapper.toResponse(veiculo, cliente);
                })
                .toList();

        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "Inativar veículo",
            description = "Realiza a exclusão lógica do veículo, preservando o registro no banco de dados.")
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {

        inativarVeiculoUseCase.executar(id);

        return ResponseEntity.noContent().build();
    }
}
