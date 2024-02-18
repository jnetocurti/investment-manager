package br.com.investmentmanager.portfolioevent.infrastructure.messagebroker.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortfolioEventRabbitMQConfig {

    @Value("${portfolioevent.infrastructure.rabbitmq.portfolio-event-exchange}")
    private String exchange;
    @Value("${portfolioevent.infrastructure.rabbitmq.portfolio-event-created-queue}")
    private String createdQueue;
    @Value("${portfolioevent.infrastructure.rabbitmq.portfolio-event-created-routing-key}")
    private String createdRoutingKey;

    @Bean
    public Queue portfolioEventCreatedQueue() {
        return new Queue(createdQueue, false);
    }

    @Bean
    public Exchange portfolioEventExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Binding portfolioEventBinding() {
        return BindingBuilder.bind(portfolioEventCreatedQueue())
                .to(portfolioEventExchange())
                .with(createdRoutingKey)
                .noargs();
    }
}
