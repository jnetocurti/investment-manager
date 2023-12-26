package br.com.investmentmanager.tradingnote.infrastructure.messagebroker.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TradingNoteRabbitMQConfig {

    @Value("${tradingnote.infrastructure.rabbitmq.trading-note-exchange}")
    private String exchange;
    @Value("${tradingnote.infrastructure.rabbitmq.trading-note-created-queue}")
    private String createdQueue;
    @Value("${tradingnote.infrastructure.rabbitmq.trading-note-created-routing-key}")
    private String createdRoutingKey;

    @Bean
    public Queue queue() {
        return new Queue(createdQueue, false);
    }

    @Bean
    public Exchange exchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with(createdRoutingKey).noargs();
    }
}
