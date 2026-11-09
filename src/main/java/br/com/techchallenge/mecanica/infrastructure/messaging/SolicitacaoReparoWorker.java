package br.com.techchallenge.mecanica.infrastructure.messaging;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.scheduling.annotation.Scheduled;
public class SolicitacaoReparoWorker {
    private final SolicitacaoReparoOutbox outbox;
    private final Consumer<ReparoSolicitado> publisher;
    public SolicitacaoReparoWorker(SolicitacaoReparoOutbox outbox, Consumer<ReparoSolicitado> publisher) {
        this.outbox=outbox; this.publisher=publisher;
    }
    @Scheduled(fixedDelayString="${integration.solicitacao-reparo.delay-ms:2000}")
    public void publicar() {
        var item=outbox.reservar(Instant.now());
        if(item.isEmpty()) return;
        var p=item.get();
        try { publisher.accept(p.evento()); }
        catch(RuntimeException e) { outbox.reagendar(p,Instant.now()); return; }
        outbox.confirmar(p,Instant.now());
    }
}
