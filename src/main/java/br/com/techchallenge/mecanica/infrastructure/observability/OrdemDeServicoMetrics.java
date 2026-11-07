package br.com.techchallenge.mecanica.infrastructure.observability;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class OrdemDeServicoMetrics {

    private static final String STATUS_RECEBIDA = "RECEBIDA";
    private static final String STATUS_DIAGNOSTICO = "EM_DIAGNOSTICO";
    private static final String STATUS_AGUARDANDO = "AGUARDANDO_APROVACAO";
    private static final String STATUS_EXECUCAO = "EM_EXECUCAO";
    private static final String STATUS_FINALIZADA = "FINALIZADA";
    private static final String STATUS_ENTREGUE = "ENTREGUE";
    private static final Pattern UUID_PATH = Pattern.compile(
            "/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}"
                    + "-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(?=/|$)");

    private final MeterRegistry registry;
    private final LongSupplier nanoTime;
    private final Map<UUID, EtapaEmExecucao> etapas = new ConcurrentHashMap<>();

    @Autowired
    public OrdemDeServicoMetrics(MeterRegistry registry) {
        this(registry, System::nanoTime);
    }

    OrdemDeServicoMetrics(MeterRegistry registry, LongSupplier nanoTime) {
        this.registry = registry;
        this.nanoTime = nanoTime;
    }

    public void registrarStatus(UUID ordemId, String status) {
        registry.counter(
                "mecanica.ordens.servico.status.transicoes",
                "status",
                status).increment();

        switch (status) {
            case STATUS_RECEBIDA -> {
                registry.counter("mecanica.ordens.servico.criadas").increment();
                etapas.remove(ordemId);
            }
            case STATUS_DIAGNOSTICO -> iniciar(ordemId, "diagnostico");
            case STATUS_AGUARDANDO -> finalizar(ordemId, "diagnostico");
            case STATUS_EXECUCAO -> iniciar(ordemId, "execucao");
            case STATUS_FINALIZADA -> {
                finalizar(ordemId, "execucao");
                iniciar(ordemId, "finalizacao");
            }
            case STATUS_ENTREGUE -> finalizar(ordemId, "finalizacao");
            default -> {
                // Status sem etapa cronometrada.
            }
        }
    }

    public void registrarFalha(String path, int statusHttp) {
        registry.counter(
                "mecanica.ordens.servico.processamento.falhas",
                "path",
                UUID_PATH.matcher(path).replaceAll("/{id}"),
                "status",
                Integer.toString(statusHttp)).increment();
    }

    private void iniciar(UUID ordemId, String etapa) {
        etapas.put(ordemId, new EtapaEmExecucao(etapa, nanoTime.getAsLong()));
    }

    private void finalizar(UUID ordemId, String etapaEsperada) {
        EtapaEmExecucao iniciada = etapas.remove(ordemId);
        if (iniciada == null || !iniciada.nome().equals(etapaEsperada)) {
            return;
        }

        long duracao = Math.max(0, nanoTime.getAsLong() - iniciada.inicio());
        registry.timer(
                "mecanica.ordens.servico.status.duracao",
                "etapa",
                etapaEsperada).record(Duration.ofNanos(duracao));
    }

    private record EtapaEmExecucao(String nome, long inicio) {
    }
}
