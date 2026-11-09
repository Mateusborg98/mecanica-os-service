package br.com.techchallenge.mecanica.application.gateway;
import java.util.UUID;
public interface SolicitarReparoGateway {
    record Vinculo(UUID ordemId, UUID reparoId, boolean publicado) {}
    void solicitar(UUID ordemId);
    Vinculo buscar(UUID ordemId);
}
