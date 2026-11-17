package br.com.techchallenge.mecanica.infrastructure.config;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Configuration
@ConditionalOnProperty(name="app.messaging.enabled",havingValue="true")
public class ProgressoOrdemConfig {
    @Bean public Declarables diagnosticoOs() {
        var ex=new DirectExchange("mecanica.events",true,false);var dlx=new DirectExchange("mecanica.dlx",true,false);
        var q=QueueBuilder.durable("os.diagnostico.v1").deadLetterExchange("mecanica.dlx").deadLetterRoutingKey("os.diagnostico.v1").build();
        var dlq=QueueBuilder.durable("os.diagnostico.v1.dlq").build();
        return new Declarables(ex,dlx,q,dlq,BindingBuilder.bind(q).to(ex).with("diagnostico.registrado.v1"),BindingBuilder.bind(dlq).to(dlx).with("os.diagnostico.v1"));
    }
    @Bean public Declarables decisaoOs() {
        var ex=new DirectExchange("mecanica.events",true,false);var dlx=new DirectExchange("mecanica.dlx",true,false);
        var q=QueueBuilder.durable("os.orcamento.v2").deadLetterExchange("mecanica.dlx").deadLetterRoutingKey("os.orcamento.v2").build();
        var dlq=QueueBuilder.durable("os.orcamento.v2.dlq").build();
        return new Declarables(ex,dlx,q,dlq,BindingBuilder.bind(q).to(ex).with("orcamento.decidido.v2"),BindingBuilder.bind(dlq).to(dlx).with("os.orcamento.v2"));
    }
    @Bean public Declarables inicioOs() {
        var ex=new DirectExchange("mecanica.events",true,false);var dlx=new DirectExchange("mecanica.dlx",true,false);
        var q=QueueBuilder.durable("os.reparo-iniciado.v1").deadLetterExchange("mecanica.dlx").deadLetterRoutingKey("os.reparo-iniciado.v1").build();
        var dlq=QueueBuilder.durable("os.reparo-iniciado.v1.dlq").build();
        return new Declarables(ex,dlx,q,dlq,BindingBuilder.bind(q).to(ex).with("reparo.iniciado.v1"),BindingBuilder.bind(dlq).to(dlx).with("os.reparo-iniciado.v1"));
    }
    @Bean public Declarables cancelamentoTopology() {
        var exchange=new DirectExchange("mecanica.events",true,false);
        var dlx=new DirectExchange("mecanica.dlx",true,false);
        var declarations=new java.util.ArrayList<Declarable>();
        declarations.add(exchange);declarations.add(dlx);
        for(String name:java.util.List.of("os.reparo-cancelado.v1")) {
            var queue=QueueBuilder.durable(name).deadLetterExchange("mecanica.dlx").deadLetterRoutingKey(name).build();
            var dlq=QueueBuilder.durable(name+".dlq").build();
            declarations.add(queue);declarations.add(dlq);
            declarations.add(BindingBuilder.bind(queue).to(exchange).with("reparo.cancelado.v1"));
            declarations.add(BindingBuilder.bind(dlq).to(dlx).with(name));
        }
        return new Declarables(declarations);
    }

}
