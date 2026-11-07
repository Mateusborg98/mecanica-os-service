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

import br.com.techchallenge.mecanica.application.usecase.operador.AtualizarOperadorUseCase;
import br.com.techchallenge.mecanica.application.usecase.operador.BuscarOperadorPorMatriculaUseCase;
import br.com.techchallenge.mecanica.application.usecase.operador.CriarOperadorUseCase;
import br.com.techchallenge.mecanica.application.usecase.operador.InativarOperadorUseCase;
import br.com.techchallenge.mecanica.application.usecase.operador.ListarOperadoresUseCase;
import br.com.techchallenge.mecanica.domain.operador.Operador;
import br.com.techchallenge.mecanica.presentation.dto.operador.AtualizarOperadorRequest;
import br.com.techchallenge.mecanica.presentation.dto.operador.CriarOperadorRequest;
import br.com.techchallenge.mecanica.presentation.dto.operador.OperadorResponse;
import br.com.techchallenge.mecanica.presentation.mapper.OperadorPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Operadores", description = "Cadastro, consulta, atualização e inativação lógica de operadores.")
@RestController
@RequestMapping("/operadores")
@RequiredArgsConstructor
public class OperadorController {

    private final CriarOperadorUseCase criarOperadorUseCase;
    private final AtualizarOperadorUseCase atualizarOperadorUseCase;
    private final ListarOperadoresUseCase listarOperadoresUseCase;
    private final BuscarOperadorPorMatriculaUseCase buscarOperadorPorMatriculaUseCase;
    private final InativarOperadorUseCase inativarOperadorUseCase;

    private final OperadorPresentationMapper mapper;

    @Operation(
            summary = "Cadastrar operador",
            description = "Cadastra um operador responsável pelas atividades administrativas ou mecânicas da oficina.")
    @PostMapping
    public ResponseEntity<OperadorResponse> criar(
            @RequestBody CriarOperadorRequest request) {

        Operador operador = criarOperadorUseCase.executar(
                mapper.toInput(request));

        return ResponseEntity.ok(
                mapper.toResponse(operador));
    }

    @Operation(
            summary = "Atualizar operador",
            description = "Atualiza os dados de um operador identificado pela matrícula.")
    @PutMapping("/matricula/{matricula}")
    public ResponseEntity<OperadorResponse> atualizar(
            @PathVariable Integer matricula,
            @RequestBody AtualizarOperadorRequest request) {

        Operador operador = atualizarOperadorUseCase.executar(
                matricula,
                mapper.toInput(request));

        return ResponseEntity.ok(
                mapper.toResponse(operador));
    }

    @Operation(
            summary = "Listar operadores ativos",
            description = "Retorna todos os operadores que não foram inativados.")
    @GetMapping
    public ResponseEntity<List<OperadorResponse>> listar() {

        List<OperadorResponse> response = listarOperadoresUseCase.executar()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Buscar operador por matrícula",
            description = "Consulta um operador ativo utilizando sua matrícula.")
    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<OperadorResponse> buscarPorMatricula(
            @PathVariable Integer matricula) {

        Operador operador = buscarOperadorPorMatriculaUseCase.executar(matricula);

        return ResponseEntity.ok(
                mapper.toResponse(operador));
    }

    @Operation(
            summary = "Inativar operador",
            description = "Realiza a exclusão lógica do operador identificado pela matrícula.")
    @PatchMapping("/matricula/{matricula}/inativar")
    public ResponseEntity<Void> inativar(
            @PathVariable Integer matricula) {

        inativarOperadorUseCase.executar(matricula);

        return ResponseEntity.noContent().build();
    }
}
