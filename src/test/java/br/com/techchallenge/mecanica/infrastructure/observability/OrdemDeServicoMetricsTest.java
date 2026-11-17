package br.com.techchallenge.mecanica.infrastructure.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class OrdemDeServicoMetricsTest {

    private SimpleMeterRegistry registry;
    private AtomicLong nanoTime;
    private OrdemDeServicoMetrics metrics;

    @BeforeEach
    void preparar() {
        registry = new SimpleMeterRegistry();
        nanoTime = new AtomicLong();
        metrics = new OrdemDeServicoMetrics(registry, nanoTime::get);
    }








}
