package br.com.techchallenge.mecanica.infrastructure.config;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Configuration
@ConditionalOnProperty(name="app.messaging.enabled", havingValue="true")
public class PagamentoMessagingConfig {
    @Bean
    Declarables pagamentoTopology() {
        var events = new DirectExchange("mecanica.events", true, false);
        var dlx = new DirectExchange("mecanica.dlx", true, false);
        var queue = QueueBuilder.durable("os.pagamento.v1").deadLetterExchange("mecanica.dlx")
                .deadLetterRoutingKey("os.pagamento.v1").build();
        var dlq = QueueBuilder.durable("os.pagamento.v1.dlq").build();
        return new Declarables(events, dlx, queue, dlq,
                BindingBuilder.bind(queue).to(events).with("pagamento.confirmado.v1"),
                BindingBuilder.bind(dlq).to(dlx).with("os.pagamento.v1"));
    }
}
