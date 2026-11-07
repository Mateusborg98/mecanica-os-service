package br.com.techchallenge.mecanica.presentation.controller;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import br.com.techchallenge.mecanica.application.gateway.SolicitarReparoGateway;
@RestController
@RequestMapping("/ordens-servico")
public class SolicitacaoReparoController {
    private final SolicitarReparoGateway outbox;
    public SolicitacaoReparoController(SolicitarReparoGateway outbox) { this.outbox=outbox; }
    @GetMapping("/{id}/reparo")
    public SolicitarReparoGateway.Vinculo buscar(@PathVariable UUID id) { return outbox.buscar(id); }
}
