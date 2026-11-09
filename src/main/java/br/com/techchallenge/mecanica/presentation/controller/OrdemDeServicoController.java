package br.com.techchallenge.mecanica.presentation.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import br.com.techchallenge.mecanica.application.dto.ordemdeservico.CriarOrdemDeServicoInput;
import br.com.techchallenge.mecanica.application.usecase.operador.BuscarOperadorPorMatriculaUseCase;
import br.com.techchallenge.mecanica.application.usecase.ordemdeservico.BuscarOrdemDeServicoPorIdUseCase;
import br.com.techchallenge.mecanica.application.usecase.ordemdeservico.CriarOrdemDeServicoUseCase;
import br.com.techchallenge.mecanica.application.usecase.ordemdeservico.EntregarOrdemDeServicoUseCase;
import br.com.techchallenge.mecanica.application.usecase.ordemdeservico.ListarOrdensDeServicoUseCase;
import br.com.techchallenge.mecanica.application.usecase.veiculo.BuscarVeiculoPorPlacaUseCase;
import br.com.techchallenge.mecanica.infrastructure.security.UsuarioAutenticadoService;
import br.com.techchallenge.mecanica.presentation.dto.ordemDeServico.AcompanhamentoOrdemResponse;
import br.com.techchallenge.mecanica.presentation.dto.ordemDeServico.CriarOrdemDeServicoRequest;
import br.com.techchallenge.mecanica.presentation.dto.ordemDeServico.OrdemDeServicoResponse;
import br.com.techchallenge.mecanica.presentation.mapper.OrdemDeServicoPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Ordem de Serviço", description = "Atendimento, acompanhamento e entrega; orçamento e execução pertencem aos serviços responsáveis.")
@RestController
@RequestMapping("/ordens-servico")
@RequiredArgsConstructor
public class OrdemDeServicoController {
    private final CriarOrdemDeServicoUseCase criar;
    private final UsuarioAutenticadoService usuario;
    private final BuscarVeiculoPorPlacaUseCase veiculos;
    private final BuscarOperadorPorMatriculaUseCase operadores;
    private final BuscarOrdemDeServicoPorIdUseCase buscar;
    private final ListarOrdensDeServicoUseCase listar;
    private final EntregarOrdemDeServicoUseCase entregar;
    private final OrdemDeServicoPresentationMapper mapper;

    @Operation(summary = "Abrir atendimento e solicitar reparo ao Execution")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdemDeServicoResponse criar(@RequestBody @Valid CriarOrdemDeServicoRequest request) {
        return mapper.toResponse(criar.executar(new CriarOrdemDeServicoInput(usuario.getClienteId(),
            veiculos.executar(request.placa()).getId(), operadores.executar(request.matriculaOperador()).getId())));
    }
    @Operation(summary = "Acompanhar publicamente o status projetado da OS")
    @GetMapping("/acompanhamento/{id}")
    public AcompanhamentoOrdemResponse acompanhar(@PathVariable UUID id) {
        var ordem = buscar.executar(id);
        return new AcompanhamentoOrdemResponse(ordem.getId(), ordem.getStatus().name(), ordem.getValorTotalOs(), ordem.getDtInicioOs(), ordem.getDtFimOs());
    }
    @Operation(summary = "Listar ordens em atendimento")
    @GetMapping
    public List<OrdemDeServicoResponse> listar() { return listar.executar().stream().map(mapper::toResponse).toList(); }
    @Operation(summary = "Entregar veículo após conclusão e pagamento confirmado")
    @PostMapping("/{id}/entregar")
    public OrdemDeServicoResponse entregar(@PathVariable UUID id) { return mapper.toResponse(entregar.executar(id)); }
}
