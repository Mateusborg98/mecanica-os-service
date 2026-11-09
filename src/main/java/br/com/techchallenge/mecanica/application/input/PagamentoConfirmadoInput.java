package br.com.techchallenge.mecanica.application.input;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
public record PagamentoConfirmadoInput(int schemaVersion, String eventType, UUID eventId,
        UUID cobrancaId, UUID ordemId, UUID reparoId, BigDecimal valor, String moeda, Instant confirmadoEm) {
    public PagamentoConfirmadoInput {
        if (schemaVersion != 1 || !"PagamentoConfirmado".equals(eventType)
                || eventId == null || cobrancaId == null || ordemId == null || reparoId == null
                || valor == null || valor.signum() <= 0 || valor.stripTrailingZeros().scale() > 2
                || !"BRL".equals(moeda) || confirmadoEm == null) {
            throw new RegraNegocioException("Evento de pagamento inválido");
        }
        UUID esperado = UUID.nameUUIDFromBytes(("mecanica:diagnostico:v1:" + reparoId).getBytes(StandardCharsets.UTF_8));
        if (!eventId.equals(cobrancaId) || !cobrancaId.equals(esperado)) {
            throw new RegraNegocioException("Identificadores de pagamento inconsistentes");
        }
        confirmadoEm = confirmadoEm.truncatedTo(ChronoUnit.MILLIS);
    }
}
