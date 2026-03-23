package com.investmentmanager.app.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TRADINGNOTE_EXCHANGE = "tradingnote.exchange";
    public static final String TRADINGNOTE_CREATED_QUEUE = "tradingnote.created.queue";
    public static final String TRADINGNOTE_CREATED_ROUTING_KEY = "tradingnote.created";

    public static final String PORTFOLIOEVENT_EXCHANGE = "portfolioevent.exchange";
    public static final String PORTFOLIOEVENT_CREATED_QUEUE = "portfolioevent.created.queue";
    public static final String PORTFOLIOEVENT_CREATED_ROUTING_KEY = "portfolioevent.created";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // --- TradingNote exchange/queue/binding ---

    @Bean
    public TopicExchange tradingNoteExchange() {
        return new TopicExchange(TRADINGNOTE_EXCHANGE);
    }

    @Bean
    public Queue tradingNoteCreatedQueue() {
        return new Queue(TRADINGNOTE_CREATED_QUEUE, true);
    }

    @Bean
    public Binding tradingNoteCreatedBinding(Queue tradingNoteCreatedQueue,
                                              TopicExchange tradingNoteExchange) {
        return BindingBuilder.bind(tradingNoteCreatedQueue)
                .to(tradingNoteExchange)
                .with(TRADINGNOTE_CREATED_ROUTING_KEY);
    }

    // --- PortfolioEvent exchange/queue/binding ---

    @Bean
    public TopicExchange portfolioEventExchange() {
        return new TopicExchange(PORTFOLIOEVENT_EXCHANGE);
    }

    @Bean
    public Queue portfolioEventCreatedQueue() {
        return new Queue(PORTFOLIOEVENT_CREATED_QUEUE, true);
    }

    @Bean
    public Binding portfolioEventCreatedBinding(Queue portfolioEventCreatedQueue,
                                                 TopicExchange portfolioEventExchange) {
        return BindingBuilder.bind(portfolioEventCreatedQueue)
                .to(portfolioEventExchange)
                .with(PORTFOLIOEVENT_CREATED_ROUTING_KEY);
    }
}
