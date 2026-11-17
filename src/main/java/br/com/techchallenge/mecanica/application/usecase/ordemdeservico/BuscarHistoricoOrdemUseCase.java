package br.com.techchallenge.mecanica.application.usecase.ordemdeservico;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import br.com.techchallenge.mecanica.application.gateway.HistoricoOrdemGateway;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class BuscarHistoricoOrdemUseCase {
    private final HistoricoOrdemGateway gateway;
    public List<HistoricoOrdemGateway.Evento> executar(UUID ordemId, long apos) {
        if (ordemId == null || apos < 0) throw new IllegalArgumentException("Informe OS e cursor não negativo");
        return gateway.buscar(ordemId, apos);
    }
}
