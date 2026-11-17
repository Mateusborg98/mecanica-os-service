package br.com.techchallenge.mecanica.presentation.controller;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import br.com.techchallenge.mecanica.application.gateway.HistoricoOrdemGateway;
import br.com.techchallenge.mecanica.application.usecase.ordemdeservico.BuscarHistoricoOrdemUseCase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor
@RequestMapping("/ordens-servico")
public class HistoricoOrdemController {
    private final BuscarHistoricoOrdemUseCase useCase;
    @GetMapping("/{id}/historico")
    @Operation(summary = "Consultar até 100 eventos da OS em ordem de recebimento; use a última sequência como cursor apos")
    public List<HistoricoOrdemGateway.Evento> buscar(@PathVariable UUID id, @RequestParam(defaultValue = "0") long apos) {
        return useCase.executar(id,apos);
    }
}
