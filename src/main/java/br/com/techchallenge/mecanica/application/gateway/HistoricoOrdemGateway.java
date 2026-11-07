package br.com.techchallenge.mecanica.application.gateway;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
public interface HistoricoOrdemGateway {
    record Evento(long sequencia, String tipo, UUID eventoId, String origem, String statusAnterior,
                  String statusAtual, Instant ocorridoEm, Instant registradoEm) { }
    List<Evento> buscar(UUID ordemId, long apos);
}
