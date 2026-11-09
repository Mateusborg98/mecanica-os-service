package br.com.techchallenge.mecanica.application.input;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
public record ProgressoOrdemInput(Tipo tipo,UUID eventId,UUID ordemId,UUID reparoId,UUID orcamentoId,
        String decisao,BigDecimal valor,String moeda,Instant ocorridoEm) {
    public enum Tipo { DIAGNOSTICO, DECISAO, INICIO, CANCELAMENTO }
    public ProgressoOrdemInput {
        if(tipo==null || eventId==null || ordemId==null || reparoId==null || ocorridoEm==null
            || !ocorridoEm.equals(ocorridoEm.truncatedTo(java.time.temporal.ChronoUnit.MICROS)))
            throw new IllegalArgumentException("Progresso incompleto");
        var budget=UUID.nameUUIDFromBytes(("mecanica:diagnostico:v1:"+reparoId).getBytes(StandardCharsets.UTF_8));
        if(tipo==Tipo.DECISAO) {
            if(!budget.equals(orcamentoId) || !eventId.equals(orcamentoId) || !java.util.Set.of("APROVADO","RECUSADO").contains(decisao==null?"":decisao)
                || valor==null || valor.signum()<=0 || valor.stripTrailingZeros().scale()>2 || !"BRL".equals(moeda))
                throw new IllegalArgumentException("Decisão de orçamento inválida");
            valor=valor.setScale(2);
        } else {
            if(!eventId.equals(reparoId) || (tipo==Tipo.INICIO && !budget.equals(orcamentoId)))
                throw new IllegalArgumentException("Identificadores inconsistentes");
        }
    }
}
