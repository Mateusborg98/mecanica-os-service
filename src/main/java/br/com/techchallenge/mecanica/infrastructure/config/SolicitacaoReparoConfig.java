package br.com.techchallenge.mecanica.infrastructure.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import br.com.techchallenge.mecanica.infrastructure.messaging.*;
@Configuration
@EnableScheduling
@ConditionalOnProperty(name="app.messaging.enabled",havingValue="true")
public class SolicitacaoReparoConfig {
    @Bean SolicitacaoReparoWorker solicitacaoReparoWorker(SolicitacaoReparoOutbox outbox,RabbitTemplate rabbit) {
        return new SolicitacaoReparoWorker(outbox,new RabbitSolicitacaoReparoPublisher(rabbit));
    }
    @Bean public Declarables solicitacaoReparoTopology() {
        var exchange=new DirectExchange("mecanica.events",true,false);
        var dlx=new DirectExchange("mecanica.dlx",true,false);
        var queue=QueueBuilder.durable("execution.reparo-solicitado.v1").deadLetterExchange("mecanica.dlx")
            .deadLetterRoutingKey("execution.reparo-solicitado.v1").build();
        var dlq=QueueBuilder.durable("execution.reparo-solicitado.v1.dlq").build();
        return new Declarables(exchange,dlx,queue,dlq,
            BindingBuilder.bind(queue).to(exchange).with("reparo.solicitado.v1"),
            BindingBuilder.bind(dlq).to(dlx).with("execution.reparo-solicitado.v1"));
    }
    @Bean public Declarables conclusaoOsTopology() {
        var exchange=new DirectExchange("mecanica.events",true,false);
        var dlx=new DirectExchange("mecanica.dlx",true,false);
        var queue=QueueBuilder.durable("os.reparo-concluido.v1").deadLetterExchange("mecanica.dlx")
            .deadLetterRoutingKey("os.reparo-concluido.v1").build();
        var dlq=QueueBuilder.durable("os.reparo-concluido.v1.dlq").build();
        return new Declarables(exchange,dlx,queue,dlq,
            BindingBuilder.bind(queue).to(exchange).with("reparo.concluido.v1"),
            BindingBuilder.bind(dlq).to(dlx).with("os.reparo-concluido.v1"));
    }
}
