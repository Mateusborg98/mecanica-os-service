package br.com.techchallenge.mecanica.application.input;
import java.time.Instant;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
public record ReparoConcluidoInput(int schemaVersion,String eventType,UUID eventId,UUID reparoId,
        UUID ordemId,UUID orcamentoId,Instant ocorridoEm) {
    public ReparoConcluidoInput {
        if(schemaVersion!=1 || !"ReparoConcluido".equals(eventType) || eventId==null || reparoId==null
            || ordemId==null || orcamentoId==null || ocorridoEm==null || !eventId.equals(reparoId)
            || !orcamentoId.equals(UUID.nameUUIDFromBytes(("mecanica:diagnostico:v1:"+reparoId).getBytes(StandardCharsets.UTF_8)))
            || !ocorridoEm.equals(ocorridoEm.truncatedTo(java.time.temporal.ChronoUnit.MICROS)))
            throw new IllegalArgumentException("Evento de conclusão inválido");
    }
}
