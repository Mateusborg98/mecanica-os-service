package br.com.techchallenge.mecanica.application.dto.ordemdeservico;
import java.util.UUID;
public record CriarOrdemDeServicoInput(UUID clienteId, UUID veiculoId, UUID operadorId) { }
